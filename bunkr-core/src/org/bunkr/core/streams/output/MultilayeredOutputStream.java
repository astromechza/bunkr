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

package org.bunkr.core.streams.output;

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.BlockAllocationManager;
import org.bunkr.core.crypto.CipherBuilder;
import org.bunkr.core.inventory.Algorithms.Encryption;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.io.CipherOutputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public class MultilayeredOutputStream extends OutputStream
{
    private final FileInventoryItem target;

    private OutputStream topstream;
    private long writtenBytes = 0;

    public MultilayeredOutputStream(ArchiveInfoContext context, FileInventoryItem target) throws FileNotFoundException
    {
        this.target = target;
        this.topstream = new BlockWriterOutputStream(
                    context.filePath,
                    context.getBlockSize(),
                    target,
                    new BlockAllocationManager(context.getInventory(), target.getBlocks())
        );

        target.setEncryptionAlgorithm(context.getInventory().getDefaultEncryption());

        if (! target.getEncryptionAlgorithm().equals(Encryption.NONE))
        {
            this.topstream = new CipherOutputStream(
                    this.topstream, new BufferedBlockCipher(CipherBuilder.buildCipherForFile(target, true))
            );
        }
        else
        {
            target.setEncryptionData(null);
        }

        this.topstream = new DeflaterOutputStream(this.topstream, new Deflater(Deflater.BEST_SPEED));
    }

    @Override
    public void write(int b) throws IOException
    {
        this.topstream.write(b);
        writtenBytes += 1;
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        this.topstream.write(b);
        writtenBytes += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        this.topstream.write(b, off, len);
        writtenBytes += len;
    }

    @Override
    public void flush() throws IOException
    {
        this.topstream.flush();
    }

    @Override
    public void close() throws IOException
    {
        this.topstream.close();
        target.setActualSize(this.writtenBytes);
    }
}
