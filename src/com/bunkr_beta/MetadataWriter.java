package com.bunkr_beta;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class MetadataWriter
{
    public static final long DBL_DATA_POS = (
            ArchiveBuilder.FORMAT_SIG.length +
            ArchiveBuilder.VERSION_BYTES.length +
            Integer.BYTES
    );

    public static void write(ArchiveInfoContext context) throws IOException
    {
        try(RandomAccessFile raf = new RandomAccessFile(context.filePath, "rw"))
        {
            try(FileChannel fc = raf.getChannel())
            {
                long dataBlocksLength;
                ByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY, DBL_DATA_POS, Long.BYTES);
                dataBlocksLength = buf.getLong();

                String inventoryJson = IO.convertToJson(context.getArchiveInventory());
                String descriptorJson = IO.convertToJson(context.getArchiveDescriptor());

                long metaLength = Integer.BYTES + inventoryJson.length() + Integer.BYTES + descriptorJson.length();


                buf = fc.map(FileChannel.MapMode.READ_WRITE, DBL_DATA_POS + Long.BYTES + dataBlocksLength, metaLength);
                buf.putInt(inventoryJson.length());
                buf.put(inventoryJson.getBytes());
                buf.putInt(descriptorJson.length());
                buf.put(descriptorJson.getBytes());
            }
        }
    }
}
