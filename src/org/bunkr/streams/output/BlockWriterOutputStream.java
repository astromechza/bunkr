package org.bunkr.streams.output;

import org.bunkr.MetadataWriter;
import org.bunkr.IBlockAllocationManager;
import org.bunkr.inventory.FileInventoryItem;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.Arrays;

public class BlockWriterOutputStream extends OutputStream
{
    private final File filePath;
    private final int blockSize;
    private final FileInventoryItem target;
    private final IBlockAllocationManager blockAllocMan;
    private final byte[] buffer;

    private int blockCursor;
    private long bytesWritten;
    private boolean partiallyFlushed;

    public BlockWriterOutputStream(File path, int blockSize, FileInventoryItem target, IBlockAllocationManager blockAllocMan)
    {
        super();
        this.filePath = path;
        this.blockSize = blockSize;
        this.target = target;
        this.blockAllocMan = blockAllocMan;
        this.blockAllocMan.clearAllocation();

        this.buffer = new byte[this.blockSize];
        this.blockCursor = 0;
        this.bytesWritten = 0;
        this.partiallyFlushed = false;

    }

    @Override
    public void write(int b) throws IOException
    {
        if (this.blockCursor == this.blockSize) this.flush();
        this.buffer[this.blockCursor++] = (byte) b;
        bytesWritten += 1;
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        int srcCursor = 0;
        while(srcCursor < len)
        {
            if (this.blockCursor == this.blockSize) this.flush();
            int readAmnt = Math.min(len - srcCursor, this.blockSize);
            readAmnt = Math.min(readAmnt, this.blockSize - this.blockCursor);
            System.arraycopy(b, off + srcCursor, this.buffer, this.blockCursor, readAmnt);
            this.blockCursor += readAmnt;
            srcCursor += readAmnt;
            bytesWritten += readAmnt;
        }
    }

    @Override
    public void flush() throws IOException
    {
        if (this.blockCursor > 0)
        {
            if (partiallyFlushed) throw new RuntimeException(
                    "Block stream has already been partially flushed on a partial block. Flushing again would cause " +
                    "stream damage.");
            if (this.blockCursor < this.blockSize)
            {
                SecureRandom r = new SecureRandom();
                byte[] remaining = new byte[this.blockSize - this.blockCursor];
                r.nextBytes(remaining);
                this.write(remaining);
                this.bytesWritten -= remaining.length;
                partiallyFlushed = true;
            }

            long blockId = this.blockAllocMan.allocateNextBlock();

            long writePosition = blockId * this.blockSize + MetadataWriter.DBL_DATA_POS + Long.BYTES;
            try(RandomAccessFile raf = new RandomAccessFile(this.filePath, "rw"))
            {
                try(FileChannel fc = raf.getChannel())
                {
                    ByteBuffer buf = fc.map(FileChannel.MapMode.READ_WRITE, writePosition, this.blockSize);
                    buf.put(this.buffer, 0, blockCursor);
                }
            }
            this.blockCursor = 0;
        }
    }

    @Override
    public void close() throws IOException
    {
        this.flush();

        Arrays.fill(this.buffer, (byte) 0);

        long newDataBlocksLength = this.blockAllocMan.getTotalBlocks() * this.blockSize;

        try(RandomAccessFile raf = new RandomAccessFile(this.filePath, "rw"))
        {
            try (FileChannel fc = raf.getChannel())
            {
                ByteBuffer buf = fc.map(FileChannel.MapMode.READ_WRITE, MetadataWriter.DBL_DATA_POS, Long.BYTES);
                buf.putLong(newDataBlocksLength);
            }
        }

        target.setBlocks(this.blockAllocMan.getCurrentAllocation());
        target.setSizeOnDisk(this.bytesWritten);
    }

}
