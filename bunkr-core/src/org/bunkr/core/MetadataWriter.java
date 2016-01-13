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

package org.bunkr.core;

import org.bunkr.core.descriptor.DescriptorBuilder;
import org.bunkr.core.descriptor.IDescriptor;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.inventory.Inventory;
import org.bunkr.core.usersec.UserSecurityProvider;

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
            3 +
            Integer.BYTES
    );

    public static void write(ArchiveInfoContext context, UserSecurityProvider uic) throws IOException, BaseBunkrException
    {
        write(context.filePath, context.getInventory(), context.getDescriptor(), uic, context.getBlockSize());
    }

    public static void write(File filePath, Inventory inventory, IDescriptor descriptor, UserSecurityProvider uic, int blockSize)
            throws IOException, BaseBunkrException
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
