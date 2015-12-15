package org.bunkr.streams.output;

import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bunkr.MetadataWriter;
import org.bunkr.IBlockAllocationManager;
import org.bunkr.inventory.FileInventoryItem;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.Arrays;

public class BlockWriterOutputStream extends OutputStream
{
    private final File filePath;
    private final int blockSize;
    private final FileInventoryItem target;
    private final IBlockAllocationManager blockAllocMan;
    private final byte[] buffer;
    private final GeneralDigest digest;

    private int blockCursor;
    private long bytesWritten;
    private boolean partiallyFlushed;


    public BlockWriterOutputStream(File path, int blockSize, FileInventoryItem target, IBlockAllocationManager blockAllocMan)
    {
        super();
        this.filePath = path;
        this.blockSize = blockSize;
        this.target = target;
        this.blockAllocMan = blockAllocMan;
        this.blockAllocMan.clearAllocation();

        this.buffer = new byte[this.blockSize];
        this.blockCursor = 0;
        this.bytesWritten = 0;
        this.partiallyFlushed = false;

        this.digest = new SHA1Digest();
    }

    @Override
    public void write(int b) throws IOException
    {
        if (this.blockCursor == this.blockSize) this.flush();
        this.buffer[this.blockCursor++] = (byte) b;
        bytesWritten += 1;
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        int srcCursor = 0;
        while(srcCursor < len)
        {
            if (this.blockCursor == this.blockSize) this.flush();
            int readAmnt = Math.min(len - srcCursor, this.blockSize);
            readAmnt = Math.min(readAmnt, this.blockSize - this.blockCursor);
            System.arraycopy(b, off + srcCursor, this.buffer, this.blockCursor, readAmnt);
            this.blockCursor += readAmnt;
            srcCursor += readAmnt;
            bytesWritten += readAmnt;
        }
    }

    @Override
    /**
     * This method writes the buffer out into the file.
     */
    public void flush() throws IOException
    {
        // only flush if the block buffer contains data
        if (this.blockCursor > 0)
        {
            // only allowed to flush a partial block once, because doing it more would break up the stream entirely.
            if (partiallyFlushed) throw new RuntimeException(
                    "Block stream has already been partially flushed on a partial block. Flushing again would cause " +
                    "stream damage.");

            // fill the end of the block with random data if needed
            if (this.blockCursor < this.blockSize)
            {
                SecureRandom r = new SecureRandom();
                byte[] remaining = new byte[this.blockSize - this.blockCursor];
                r.nextBytes(remaining);
                // write the bytes to the buffer
                this.write(remaining);
                // remove the increment added by write()
                this.bytesWritten -= remaining.length;
                partiallyFlushed = true;
            }

            // identify which block the data will be written too
            long blockId = this.blockAllocMan.allocateNextBlock();
            long writePosition = blockId * this.blockSize + MetadataWriter.DBL_DATA_POS + Long.BYTES;

            // open up the file and write it!
            try(RandomAccessFile raf = new RandomAccessFile(this.filePath, "rw"))
            {
                try(FileChannel fc = raf.getChannel())
                {
                    ByteBuffer buf = fc.map(FileChannel.MapMode.READ_WRITE, writePosition, this.blockSize);
                    buf.put(this.buffer, 0, blockCursor);
                    this.digest.update(this.buffer, 0, blockCursor);
                }
            }
            // reset the cursor
            this.blockCursor = 0;
        }
    }

    @Override
    public void close() throws IOException
    {
        // flush the last block if needed
        this.flush();

        // clear the temporary buffer
        Arrays.fill(this.buffer, (byte) 0);

        // now because we've written new data to the file, we need to update the block data length
        // by opening the file and inserting the data back at the beginning of the file.
        long newDataBlocksLength = this.blockAllocMan.getTotalBlocks() * this.blockSize;
        try(RandomAccessFile raf = new RandomAccessFile(this.filePath, "rw"))
        {
            try (FileChannel fc = raf.getChannel())
            {
                ByteBuffer buf = fc.map(FileChannel.MapMode.READ_WRITE, MetadataWriter.DBL_DATA_POS, Long.BYTES);
                buf.putLong(newDataBlocksLength);
            }
        }

        // retrieve hash from digest
        byte[] digest = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(digest, 0);

        // set attributes on output file
        target.setBlocks(this.blockAllocMan.getCurrentAllocation());
        target.setSizeOnDisk(this.bytesWritten);
        target.setIntegrityHash(digest);
    }

}
