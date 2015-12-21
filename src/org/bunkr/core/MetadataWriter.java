package org.bunkr.core;

import org.bunkr.descriptor.DescriptorBuilder;
import org.bunkr.descriptor.IDescriptor;
import org.bunkr.exceptions.BaseBunkrException;
import org.bunkr.inventory.Inventory;
import org.bouncycastle.crypto.CryptoException;

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

    public static void write(ArchiveInfoContext context, UserSecurityProvider uic) throws IOException, CryptoException, BaseBunkrException
    {
        write(context.filePath, context.getInventory(), context.getDescriptor(), uic, context.getBlockSize());
    }

    public static void write(File filePath, Inventory inventory, IDescriptor descriptor, UserSecurityProvider uic, int blockSize)
            throws IOException, CryptoException, BaseBunkrException
    {
        try(RandomAccessFile raf = new RandomAccessFile(filePath, "rw"))
        {
            try(FileChannel fc = raf.getChannel())
            {
                byte[] inventoryJsonBytes = descriptor.writeInventoryToBytes(inventory, uic);
                byte[] descriptorJsonBytes = DescriptorBuilder.toJSON(descriptor).getBytes();

                long metaLength = Integer.BYTES + inventoryJsonBytes.length + Integer.BYTES + descriptorJsonBytes.length;

                // When writing metadata we need to be able to truncate unused blocks off of the end of the file after
                // files are deleted.
                long dataBlocksLength = BlockAllocationManager.calculateUsedBlocks(inventory) * blockSize;

                // also means we need to rewrite this value at the beginning of the file
                ByteBuffer buf = fc.map(FileChannel.MapMode.READ_WRITE, DBL_DATA_POS, Long.BYTES);
                buf.putLong(dataBlocksLength);

                // now map the metadata section
                buf = fc.map(
                        FileChannel.MapMode.READ_WRITE,
                        DBL_DATA_POS + Long.BYTES + dataBlocksLength,
                        metaLength
                );
                // write plaintext descriptor
                buf.putInt(descriptorJsonBytes.length);
                buf.put(descriptorJsonBytes);

                // now write inventory
                buf.putInt(inventoryJsonBytes.length);
                buf.put(inventoryJsonBytes);

                // truncate file if required
                raf.setLength(DBL_DATA_POS + Long.BYTES + dataBlocksLength + metaLength);
            }
        }
    }
}