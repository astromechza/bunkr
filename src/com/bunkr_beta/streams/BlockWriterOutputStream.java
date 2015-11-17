package com.bunkr_beta.streams;

import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.interfaces.IBlockAllocationManager;
import com.bunkr_beta.inventory.FileInventoryItem;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;

public class BlockWriterOutputStream extends OutputStream
{
    private final File filePath;
    private final int blockSize;
    private final FileInventoryItem target;
    private final IBlockAllocationManager blockAllocMan;

    private byte[] buffer;
    private int cursor;
    private long bytesWritten;
    private boolean partiallyFlushed;

    public BlockWriterOutputStream(File path, int blockSize, FileInventoryItem target, IBlockAllocationManager blockAllocMan) throws IOException
    {
        super();
        this.filePath = path;
        this.blockSize = blockSize;
        this.target = target;
        this.blockAllocMan = blockAllocMan;

        this.buffer = new byte[this.blockSize];
        this.cursor = 0;
        this.bytesWritten = 0;
        this.partiallyFlushed = false;

        this.blockAllocMan.clearAllocation();
    }

    @Override
    public void write(int b) throws IOException
    {
        if (this.cursor == this.blockSize) this.flush();
        this.buffer[this.cursor++] = (byte) b;
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
            if (this.cursor == this.blockSize) this.flush();
            int readAmnt = (len - srcCursor) % this.blockSize;
            readAmnt = Math.min(readAmnt, this.blockSize - this.cursor);
            System.arraycopy(b, off + srcCursor, this.buffer, this.cursor, readAmnt);
            this.cursor += readAmnt;
            srcCursor += readAmnt;
        }
    }

    @Override
    public void flush() throws IOException
    {
        if (this.cursor > 0)
        {
            if (partiallyFlushed) throw new RuntimeException(
                    "Block stream has already been partially flushed on a partial block. Flushing again would cause " +
                    "stream damage.");
            if (this.cursor < this.blockSize)
            {
                SecureRandom r = new SecureRandom();
                byte[] remaining = new byte[this.blockSize - this.cursor];
                r.nextBytes(remaining);
                this.write(remaining);
                partiallyFlushed = true;
            }

            int blockId = this.blockAllocMan.allocateNextBlock();

            long writePosition = blockId * this.blockSize + MetadataWriter.DBL_DATA_POS + Long.BYTES;
            try(RandomAccessFile raf = new RandomAccessFile(this.filePath, "rw"))
            {
                try(FileChannel fc = raf.getChannel())
                {
                    ByteBuffer buf = fc.map(FileChannel.MapMode.READ_WRITE, writePosition, this.blockSize);
                    buf.put(this.buffer, 0, cursor);
                }
            }
            bytesWritten += this.cursor;
            this.cursor = 0;
        }
    }

    @Override
    public void close() throws IOException
    {
        this.flush();

        long newDataBlocksLength = this.blockAllocMan.getTotalBlocks() * this.blockSize;

        try(RandomAccessFile raf = new RandomAccessFile(this.filePath, "rw"))
        {
            try (FileChannel fc = raf.getChannel())
            {
                ByteBuffer buf = fc.map(FileChannel.MapMode.READ_WRITE, MetadataWriter.DBL_DATA_POS, Long.BYTES);
                buf.putLong(newDataBlocksLength);
            }
        }

        target.setBlocks(this.blockAllocMan.getAllocation());
        target.setSize(this.bytesWritten);
    }

}
