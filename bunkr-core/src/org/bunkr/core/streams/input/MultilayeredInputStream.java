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

package org.bunkr.core.streams.input;

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.crypto.CipherBuilder;
import org.bunkr.core.inventory.Algorithms;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.io.CipherInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

/**
 * Creator: benmeier
 * Created At: 2015-11-21
 */
public class MultilayeredInputStream extends InputStream
{
    private final boolean emptyFile;

    private BlockReaderInputStream baseStream;
    private InputStream topstream = null;

    public MultilayeredInputStream(ArchiveInfoContext context, FileInventoryItem target)
    {
        this.emptyFile = target.getActualSize() == 0;
        if (! emptyFile)
        {
            this.baseStream = new BlockReaderInputStream(context.filePath, context.getBlockSize(), target);
            this.topstream = this.baseStream;

            if (! target.getEncryptionAlgorithm().equals(Algorithms.Encryption.NONE))
            {
                this.topstream = new CipherInputStream(
                        this.topstream, new BufferedBlockCipher(CipherBuilder.buildCipherForFile(target, false))
                );
            }

            this.topstream = new InflaterInputStream(this.topstream);
        }
    }

    @Override
    /**
     * NOTE: this will return some value in bytes, not necessarily the total length of the stream, just a number of
     * bytes that can be read immediately without blocking.
     */
    public int available() throws IOException
    {
        if(this.emptyFile) return -1;
        return this.topstream.available();
    }

    @Override
    public long skip(long n) throws IOException
    {
        if(this.emptyFile) return -1;
        return this.topstream.skip(n);
    }

    @Override
    public int read() throws IOException
    {
        if(this.emptyFile) return -1;
        return this.topstream.read();
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        if(this.emptyFile) return -1;
        return this.topstream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if(this.emptyFile) return -1;
        return this.topstream.read(b, off, len);
    }

    @Override
    public void close() throws IOException
    {
        if (topstream != null) topstream.close();
    }

    @Override
    public boolean markSupported()
    {
        return false;
    }

    public void setCheckHashOnFinish(boolean b)
    {
        if (this.baseStream != null) this.baseStream.setCheckHashOnFinish(b);
    }
}
