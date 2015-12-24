package org.bunkr.core.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class IO
{

    public static byte[] readNBytes(InputStream dis, int n) throws IOException
    {
        byte[] buffer = new byte[n];
        int r = dis.read(buffer);
        if (r != n) throw new IOException(String.format("Expected to read %d bytes, only read %d", n, r));
        return buffer;
    }

    public static String readNByteString(InputStream dis, int n) throws IOException
    {
        assert n < 0xFFFFFF;
        byte[] buffer = new byte[n];
        int r = dis.read(buffer);
        return new String(buffer).substring(0, r);
    }

    public static String readString(DataInputStream dis) throws IOException
    {
        return readNByteString(dis, dis.readInt());
    }

    public static void reliableSkip(InputStream is, long n) throws IOException
    {
        long stillToSkip = n;
        while (stillToSkip > 0)
        {
            stillToSkip -= is.skip(stillToSkip);
        }
    }

    public static int reliableRead(InputStream is, byte[] dst) throws IOException
    {
        return reliableRead(is, dst, 0, dst.length);
    }

    public static int reliableRead(InputStream is, byte[] dst, int off, int n) throws IOException
    {
        int stillToRead = n;
        int successfullyRead = 0;
        while (stillToRead > 0)
        {
            int read = is.read(dst, off + successfullyRead, stillToRead);
            if (read == -1) break;
            stillToRead -= read;
            successfullyRead += read;
        }
        return successfullyRead;
    }
}
