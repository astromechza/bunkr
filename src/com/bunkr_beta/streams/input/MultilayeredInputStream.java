package com.bunkr_beta.streams.input;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.ArrayStack;
import com.bunkr_beta.inventory.FileInventoryItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.InflaterInputStream;

/**
 * Creator: benmeier
 * Created At: 2015-11-21
 */
public class MultilayeredInputStream extends InputStream
{
    private ArrayStack<InputStream> streams = new ArrayStack<>();

    public MultilayeredInputStream(ArchiveInfoContext context, FileInventoryItem target)
    {
        this.streams.push(
            new BlockReaderInputStream(
                    context.filePath,
                    context.getBlockSize(),
                    target.getBlocks(),
                    target.getSizeOnDisk()
            )
        );
        if (context.getArchiveDescriptor().encryption != null)
        {
            throw new RuntimeException("Not implemented");
        }
        if (context.getArchiveDescriptor().compression != null)
        {
            this.streams.push(new InflaterInputStream(this.streams.peek()));
        }
    }

    @Override
    public int available() throws IOException
    {
        return this.streams.peek().available();
    }

    @Override
    public long skip(long n) throws IOException
    {
        return this.streams.peek().skip(n);
    }

    @Override
    public int read() throws IOException
    {
        return this.streams.peek().read();
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        return this.streams.peek().read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        return this.streams.peek().read(b, off, len);
    }

    @Override
    public void close() throws IOException
    {
        Iterator<InputStream> i = this.streams.topToBottom();
        while(i.hasNext())
        {
            i.next().close();
        }
    }

    @Override
    public boolean markSupported()
    {
        return false;
    }
}
