package com.bunkr_beta.streams.output;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NonClosableOutputStream extends FilterOutputStream
{
    public NonClosableOutputStream(OutputStream out)
    {
        super(out);
    }

    @Override
    public void close() throws IOException
    {
        // do nothing
    }
}
