package com.bunkr_beta;

import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.descriptor.Descriptor;
import com.bunkr_beta.inventory.Inventory;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

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

    public static void write(ArchiveInfoContext context, PasswordProvider uic) throws IOException, CryptoException
    {
        write(context.filePath, context.getInventory(), context.getDescriptor(), uic, context.getBlockSize());
    }

    public static void write(File filePath, Inventory inventory, Descriptor descriptor, PasswordProvider uic, int blockSize)
            throws IOException, CryptoException
    {
        try(RandomAccessFile raf = new RandomAccessFile(filePath, "rw"))
        {
            try(FileChannel fc = raf.getChannel())
            {
                byte[] inventoryJsonBytes = JSONHelper.stringify(inventory).getBytes();
                byte[] descriptorJsonBytes = JSONHelper.stringify(descriptor).getBytes();

                if (descriptor.getEncryption() != null)
                {
                    // otherwise, do encryption
                    PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator();
                    g.init(uic.getHashedArchivePassword(), descriptor.getEncryption().pbkdf2Salt,
                           descriptor.getEncryption().pbkdf2Iterations);
                    ParametersWithIV kp = ((ParametersWithIV) g.generateDerivedParameters(
                            descriptor.getEncryption().aesKeyLength,
                            descriptor.getEncryption().aesKeyLength)
                    );

                    // encrypt the inventory
                    byte[] encryptedInv = SimpleAES.encrypt(
                            inventoryJsonBytes,
                            ((KeyParameter) kp.getParameters()).getKey(),
                            kp.getIV()
                    );
                    Arrays.fill(inventoryJsonBytes, (byte) 0);
                    inventoryJsonBytes = encryptedInv;
                }

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
