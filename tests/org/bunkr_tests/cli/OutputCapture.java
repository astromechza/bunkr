package org.bunkr_tests.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * Creator: benmeier
 * Created At: 2015-12-12
 */
public class OutputCapture implements AutoCloseable
{
    private final PrintStream original;
    private final ByteArrayOutputStream stream;

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

    public List<String> getLines()
    {
        String content = this.getContent();
        content = content.replace("\r", "");
        return Arrays.asList(content.split("\n"));
    }

    @Override
    public void close() throws Exception
    {
        System.setOut(original);
    }
}
