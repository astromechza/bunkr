package org.bunkr.core.operations;

import org.bunkr.core.MetadataWriter;
import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.utils.RandomMaker;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created At: 2016-10-09
 */
public class WipeFileOp
{
    /**
     * Given the archive path and a range of blocks, wipe the contents of those blocks.
     *
     * @param path the path to the bunkr file
     * @param blockSize the block size used by the archive
     * @param blockRange the range of blocks to wipe
     * @param fillRandom whether or not to fill the blocks with random, otherwise it will be wiped with zeroes
     * @throws IOException if there was an error while opening and writing the file.
     */
    public static void wipe(File path, int blockSize, FragmentedRange blockRange, boolean fillRandom) throws IOException
    {
        RandomAccessFile inputFile = new RandomAccessFile(path, "rw");
        FileChannel inputFileChannel = inputFile.getChannel();

        byte[] buffer = new byte[blockSize];

        for (int b : blockRange.toList())
        {
            if (fillRandom) RandomMaker.fill(buffer);
            long writePosition = b * blockSize + MetadataWriter.DBL_DATA_POS + Long.BYTES;
            MappedByteBuffer bb = inputFileChannel.map(FileChannel.MapMode.READ_WRITE, writePosition, buffer.length);
            bb.put(buffer, 0, buffer.length);
        }
    }
}
