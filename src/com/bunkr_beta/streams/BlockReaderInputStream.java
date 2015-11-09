package com.bunkr_beta.streams;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.fragmented_range.FragmentedRange;
import com.bunkr_beta.inventory.FileInventoryItem;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.EmptyStackException;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public class BlockReaderInputStream extends InputStream
{
    private final ArchiveInfoContext context;
    private final FileInventoryItem target;
    private final byte[] buffer;
    private final FragmentedRange blocks;
    private long bytesRead;
    private int cursor;

    public BlockReaderInputStream(ArchiveInfoContext context, FileInventoryItem target)
    {
        super();
        this.context = context;
        this.target = target;

        this.buffer = new byte[this.context.getBlockSize()];
        this.cursor = 0;

        this.blocks = target.blocks.copy();
        this.bytesRead = 0;
    }


    @Override
    public int read() throws IOException
    {
        if (bytesRead >= target.size) return -1;
        if (cursor == this.context.getBlockSize())
        {
            loadNextBlock();
        }
        int v = (int) this.buffer[cursor];
        cursor += 1;
        bytesRead += 1;
        return v;
    }


    @Override
    public int read(byte[] b) throws IOException
    {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (bytesRead >= target.size) return -1;
        int remainingBytesToRead = len;
        int destCursor = 0;
        try
        {
            while (remainingBytesToRead > 0)
            {
                int bytesThatCanBeReadFromBuffer = Math.min(remainingBytesToRead, context.getBlockSize() - cursor);
                if (bytesThatCanBeReadFromBuffer > 0)
                {
                    System.arraycopy(this.buffer, cursor, b, off + destCursor, bytesThatCanBeReadFromBuffer);
                    cursor += bytesThatCanBeReadFromBuffer;
                    bytesRead += bytesThatCanBeReadFromBuffer;
                    remainingBytesToRead -= bytesThatCanBeReadFromBuffer;
                    destCursor += bytesThatCanBeReadFromBuffer;
                }
                if (cursor == this.context.getBlockSize())
                {
                    loadNextBlock();
                }
            }
        }
        catch (EmptyStackException ignored) {}

        return destCursor;
    }

    @Override
    public long skip(long n) throws IOException
    {
        if (bytesRead >= target.size) return -1;
        long remainingBytesToSkip = n;
        try
        {
            while (remainingBytesToSkip > 0)
            {
                long bytesThatCanBeSkippedFromBuffer = Math.min(remainingBytesToSkip, context.getBlockSize() - cursor);
                if (bytesThatCanBeSkippedFromBuffer > 0)
                {
                    cursor += bytesThatCanBeSkippedFromBuffer;
                    bytesRead += bytesThatCanBeSkippedFromBuffer;
                    remainingBytesToSkip -= bytesThatCanBeSkippedFromBuffer;
                }
                if (cursor == this.context.getBlockSize())
                {
                    loadNextBlock();
                }
            }
        }
        catch (EmptyStackException ignored) {}

        return n - remainingBytesToSkip;
    }

    @Override
    public int available() throws IOException
    {
        long v = target.size - bytesRead;
        if (v > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) v;
    }

    @Override
    public boolean markSupported()
    {
        return false;
    }

    private void loadNextBlock() throws IOException
    {
        if (this.blocks.isEmpty()) throw new EmptyStackException();
        int blockId = this.blocks.popMin();
        long filePosition = MetadataWriter.DBL_DATA_POS + Long.BYTES + blockId * this.context.getBlockSize();

        try(RandomAccessFile raf = new RandomAccessFile(context.filePath, "r"))
        {
            try(FileChannel fc = raf.getChannel())
            {
                ByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY, filePosition, context.getBlockSize());
                buf.get(this.buffer);
                this.bytesRead += (context.getBlockSize() - this.cursor);
                this.cursor = 0;
            }
        }
    }
}
