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

import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.exceptions.IntegrityHashError;
import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.inventory.FileInventoryItem;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

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
    private final GeneralDigest digest;
    private final byte[] expectedDigest;

    private boolean checkHashOnFinish;
    private long bytesConsumed;
    private int cursor;
    private byte[] finishedDigest = null;

    public BlockReaderInputStream(File path, int blockSize, FileInventoryItem target)
    {
        super();
        this.filePath = path;
        this.blockSize = blockSize;
        this.dataLength = target.getSizeOnDisk();
        this.blocks = target.getBlocks().copy();
        this.buffer = new byte[blockSize];
        this.checkHashOnFinish = true;
        this.expectedDigest = target.getIntegrityHash();
        this.digest = new SHA1Digest();

        long blockDataLength = ((long) blocks.size()) * blockSize;
        if (dataLength > blockDataLength)
            throw new IllegalArgumentException("File dataLength is greater than block count * block size");

        this.cursor = this.blockSize;
        this.bytesConsumed = 0;
    }

    @Override
    public int read() throws IOException
    {
        if (bytesConsumed >= dataLength) return -1;
        if (cursor == blockSize) loadNextBlock();
        bytesConsumed++;
        return (int) this.buffer[cursor++];
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (bytesConsumed >= dataLength) return -1;
        int remainingBytesToRead = len;
        int read = 0;
        while (remainingBytesToRead > 0)
        {
            if (bytesConsumed >= dataLength) break;
            if (cursor == blockSize) loadNextBlock();
            int readable = (int) Math.min(remainingBytesToRead, Math.min(blockSize - cursor, dataLength - bytesConsumed));
            System.arraycopy(this.buffer, cursor, b, off + read, readable);
            cursor += readable;
            bytesConsumed += readable;
            remainingBytesToRead -= readable;
            read += readable;
        }

        return read;
    }

    @Override
    public long skip(long len) throws IOException
    {
        if (bytesConsumed >= dataLength) return -1;
        long remainingBytesToSkip = len;
        long skipped = 0;
        while (remainingBytesToSkip > 0)
        {
            if (bytesConsumed >= dataLength) break;
            if (cursor == blockSize) loadNextBlock();
            long skippable = Math.min(remainingBytesToSkip, Math.min(blockSize - cursor, dataLength - bytesConsumed));
            cursor += skippable;
            bytesConsumed += skippable;
            remainingBytesToSkip -= skippable;
            skipped += skippable;
        }

        return skipped;
    }

    @Override
    public int available() throws IOException
    {
        long v = dataLength - bytesConsumed;
        if (v > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) v;
    }

    @Override
    public boolean markSupported()
    {
        return false;
    }

    @Override
    public void close() throws IOException
    {
        Arrays.fill(this.buffer, (byte) 0);
        super.close();
    }

    public boolean doesHashMatch() throws IOException
    {
        if (finishedDigest == null) throw new IOException("Cannot check hash before entire file has been read or skipped");
        return Arrays.equals(expectedDigest, finishedDigest);
    }

    public void setCheckHashOnFinish(boolean b)
    {
        this.checkHashOnFinish = b;
    }

    private void loadNextBlock() throws IOException
    {
        if (this.blocks.isEmpty()) throw new IOException("No more blocks to load");
        int blockId = this.blocks.popMin();

        try(RandomAccessFile raf = new RandomAccessFile(filePath, "r"))
        {
            try(FileChannel fc = raf.getChannel())
            {
                long position = DATABLOCKS_START + ((long) blockId) * blockSize;
                ByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY, position, blockSize);
                buf.get(this.buffer);
                digest.update(this.buffer, 0, this.buffer.length);
                this.bytesConsumed += (blockSize - this.cursor);
                this.cursor = 0;
            }
        }

        if (this.checkHashOnFinish && this.blocks.isEmpty())
        {
            finishedDigest = new byte[digest.getDigestSize()];
            digest.doFinal(finishedDigest, 0);
            if (! this.doesHashMatch()) throw new IntegrityHashError("Integrity hash did not match!");
        }
    }


}
