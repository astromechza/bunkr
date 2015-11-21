package com.bunkr_beta.streams.input;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Creator: benmeier
 * Created At: 2015-11-21
 */
public class NonClosableInputStream extends FilterInputStream
{
    protected NonClosableInputStream(InputStream in)
    {
        super(in);
    }

    @Override
    public void close() throws IOException
    {
        // do nothing
    }
}
