/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
