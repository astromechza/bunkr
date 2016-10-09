package org.bunkr.core.operations;

import org.bunkr.core.MetadataWriter;
import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.utils.RandomMaker;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.Consumer;

/**
 * Created At: 2016-10-09
 */
public class WipeFileOp
{
    private final File path;
    private final int blockSize;
    private final FragmentedRange blockRange;
    private final boolean fillRandom;
    private boolean cancelled = false;
    private Consumer<WipeFileOp> progressUpdate = null;
    private final int totalBlocks;
    private int blocksWiped = 0;

    /**
     * Given the archive path and a range of blocks, wipe the contents of those blocks.
     *
     * @param path the path to the bunkr file
     * @param blockSize the block size used by the archive
     * @param blockRange the range of blocks to wipe
     * @param fillRandom whether or not to fill the blocks with random, otherwise it will be wiped with zeroes
     */
    public WipeFileOp(File path, int blockSize, FragmentedRange blockRange, boolean fillRandom)
    {
        this.path = path;
        this.blockSize = blockSize;
        this.blockRange = blockRange;
        this.fillRandom = fillRandom;

        this.totalBlocks = blockRange.size();
    }

    /**
     * Set an update function that will be called after each block of the file has been wiped.
     *
     * The function will be called with the current class instance.
     *
     * @param update a consumer
     */
    public void setProgressUpdate(Consumer<WipeFileOp> update)
    {
        this.progressUpdate = update;
    }

    /**
     * Cancel the wipe operation if it is long running.
     *
     * This will interrupt the run after the next block is wiped and cause it to return false.
     */
    public void cancel()
    {
        this.cancelled = true;
    }

    public int getBlocksWiped()
    {
        return blocksWiped;
    }

    public int getTotalBlocks()
    {
        return totalBlocks;
    }

    public int getBlockSize()
    {
        return blockSize;
    }

    /**
     * Run the wipe operation.
     *
     * @return whether the wipe operation wiped all blocks
     * @throws IOException if there was an error opening and writing the file
     */
    public boolean run() throws IOException
    {
        RandomAccessFile inputFile = new RandomAccessFile(path, "rw");
        FileChannel inputFileChannel = inputFile.getChannel();

        byte[] buffer = new byte[blockSize];

        for (int b : blockRange.toList())
        {
            if (cancelled) return false;
            if (fillRandom) RandomMaker.fill(buffer);
            long writePosition = b * blockSize + MetadataWriter.DBL_DATA_POS + Long.BYTES;
            MappedByteBuffer bb = inputFileChannel.map(FileChannel.MapMode.READ_WRITE, writePosition, buffer.length);
            bb.put(buffer, 0, buffer.length);
            blocksWiped++;
            progressUpdate.accept(this);
        }
        return true;
    }

    /**
     * Given the archive path and a range of blocks, wipe the contents of those blocks.
     *
     * @param path the path to the bunkr file
     * @param blockSize the block size used by the archive
     * @param blockRange the range of blocks to wipe
     * @param fillRandom whether or not to fill the blocks with random, otherwise it will be wiped with zeroes
     * @throws IOException if there was an error while opening and writing the file.
     */
    public static boolean wipe(File path, int blockSize, FragmentedRange blockRange, boolean fillRandom) throws IOException
    {
        return new WipeFileOp(path, blockSize, blockRange, fillRandom).run();
    }
}
