package com.bunkr_beta.streams.input;

import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.fragmented_range.FragmentedRange;

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
    public static final long DATABLOCKS_START = MetadataWriter.DBL_DATA_POS + Long.BYTES;

    private final File filePath;
    private final int blockSize;
    private final long dataLength;
    private final byte[] buffer;
    private final FragmentedRange blocks;
    private long bytesRead;
    private int cursor;

    public BlockReaderInputStream(File path, int blockSize, FragmentedRange blocks, long dataLength)
    {
        super();
        this.filePath = path;
        this.blockSize = blockSize;
        this.dataLength = dataLength;

        if (dataLength > blocks.size() * blockSize)
            throw new IllegalArgumentException("File dataLength is greater than block count * block size");

        this.buffer = new byte[blockSize];
        this.cursor = this.blockSize;

        this.blocks = blocks.copy();
        this.bytesRead = 0;
    }


    @Override
    public int read() throws IOException
    {
        if (bytesRead >= dataLength) return -1;
        if (cursor == blockSize)
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
        if (bytesRead >= dataLength) return -1;
        int remainingBytesToRead = len;
        int destCursor = 0;
        while (remainingBytesToRead > 0)
        {
            int bytesThatCanBeReadFromBuffer = (int) Math.min(remainingBytesToRead, Math.min(blockSize - cursor, dataLength - bytesRead));
            if (bytesThatCanBeReadFromBuffer > 0)
            {
                System.arraycopy(this.buffer, cursor, b, off + destCursor, bytesThatCanBeReadFromBuffer);
                cursor += bytesThatCanBeReadFromBuffer;
                bytesRead += bytesThatCanBeReadFromBuffer;
                remainingBytesToRead -= bytesThatCanBeReadFromBuffer;
                destCursor += bytesThatCanBeReadFromBuffer;
            }
            else if (dataLength - bytesRead <= 0)
            {
                break;
            }
            if (cursor == blockSize)
            {
                loadNextBlock();
            }
        }

        return destCursor;
    }

    @Override
    public long skip(long n) throws IOException
    {
        if (bytesRead >= dataLength) return -1;
        long remainingBytesToSkip = n;
        try
        {
            while (remainingBytesToSkip > 0)
            {
                long bytesThatCanBeSkippedFromBuffer = Math.min(remainingBytesToSkip, Math.min(blockSize - cursor, dataLength - bytesRead));
                if (bytesThatCanBeSkippedFromBuffer > 0)
                {
                    cursor += bytesThatCanBeSkippedFromBuffer;
                    bytesRead += bytesThatCanBeSkippedFromBuffer;
                    remainingBytesToSkip -= bytesThatCanBeSkippedFromBuffer;
                }
                else if (dataLength - bytesRead <= 0)
                {
                    break;
                }
                if (cursor == blockSize)
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
        long v = dataLength - bytesRead;
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
        if (this.blocks.isEmpty()) throw new IOException("No more blocks to load");
        int blockId = this.blocks.popMin();

        try(RandomAccessFile raf = new RandomAccessFile(filePath, "r"))
        {
            try(FileChannel fc = raf.getChannel())
            {
                ByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY, DATABLOCKS_START + blockId * blockSize, blockSize);
                buf.get(this.buffer);
                this.bytesRead += (blockSize - this.cursor);
                this.cursor = 0;
            }
        }
    }
}
