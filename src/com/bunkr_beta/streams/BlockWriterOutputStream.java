package com.bunkr_beta.streams;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.BlockAllocationManager;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.inventory.FileInventoryItem;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;

public class BlockWriterOutputStream extends OutputStream
{

    private final ArchiveInfoContext context;
    private final FileInventoryItem target;
    private final BlockAllocationManager blockAllocMan;

    private byte[] buffer;
    private int cursor;
    private long bytesWritten;
    private boolean partiallyFlushed;

    public BlockWriterOutputStream(ArchiveInfoContext context, FileInventoryItem target) throws IOException
    {
        super();
        this.context = context;
        this.target = target;
        this.blockAllocMan = new BlockAllocationManager(context, target);

        this.buffer = new byte[this.context.getBlockSize()];
        this.cursor = 0;
        this.bytesWritten = 0;
        this.partiallyFlushed = false;
    }

    @Override
    public void write(int b) throws IOException
    {
        if (this.cursor == this.context.getBlockSize()) this.flush();
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
            if (this.cursor == this.context.getBlockSize()) this.flush();
            int readAmnt = (len - srcCursor) % this.context.getBlockSize();
            readAmnt = Math.min(readAmnt, this.context.getBlockSize() - this.cursor);
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
            if (this.cursor < this.context.getBlockSize())
            {
                SecureRandom r = new SecureRandom();
                byte[] remaining = new byte[context.getBlockSize() - this.cursor];
                r.nextBytes(remaining);
                this.write(remaining);
                partiallyFlushed = true;
            }

            int blockId = this.blockAllocMan.consumeBlock();

            long writePosition = blockId * context.getBlockSize() + MetadataWriter.DBL_DATA_POS + Long.BYTES;
            try(RandomAccessFile raf = new RandomAccessFile(context.filePath, "rw"))
            {
                try(FileChannel fc = raf.getChannel())
                {
                    ByteBuffer buf = fc.map(FileChannel.MapMode.READ_WRITE, writePosition, context.getBlockSize());
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

        long newDataBlocksLength = Math.max(
                context.getBlockDataLength(),
                (this.blockAllocMan.getNewAllocatedBlocks().getMax() + 1) * context.getBlockSize()
        );

        try(RandomAccessFile raf = new RandomAccessFile(context.filePath, "rw"))
        {
            try (FileChannel fc = raf.getChannel())
            {
                ByteBuffer buf = fc.map(FileChannel.MapMode.READ_WRITE, MetadataWriter.DBL_DATA_POS, Long.BYTES);
                buf.putLong(newDataBlocksLength);
            }
        }

        context.invalidate();
        target.blocks = this.blockAllocMan.getNewAllocatedBlocks();
        target.size = this.bytesWritten;
    }

}
