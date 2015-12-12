package com.bunkr_beta_tests.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Creator: benmeier
 * Created At: 2015-12-12
 */
public class OutputCapture implements AutoCloseable
{
    private PrintStream original;
    private ByteArrayOutputStream stream;

    public OutputCapture()
    {
        original = System.out;
        stream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stream));
    }

    public String getContent()
    {
        return stream.toString();
    }

    public byte[] getBytes()
    {
        return stream.toByteArray();
    }

    @Override
    public void close() throws Exception
    {
        System.setOut(original);
    }
}
