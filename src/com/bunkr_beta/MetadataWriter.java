package com.bunkr_beta;

import com.bunkr_beta.inventory.Inventory;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

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

    public static void write(ArchiveInfoContext context, UserInfoContext uic) throws IOException, CryptoException
    {
        write(context.filePath, context.getInventory(), context.getDescriptor(), uic);
    }

    public static void write(File filePath, Inventory inventory, Descriptor descriptor, UserInfoContext uic)
            throws IOException, CryptoException
    {
        try(RandomAccessFile raf = new RandomAccessFile(filePath, "rw"))
        {
            try(FileChannel fc = raf.getChannel())
            {
                long dataBlocksLength;
                ByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY, DBL_DATA_POS, Long.BYTES);
                dataBlocksLength = buf.getLong();

                byte[] inventoryJsonBytes = IO.convertToJson(inventory).getBytes();
                byte[] descriptorJsonBytes = IO.convertToJson(descriptor).getBytes();

                long metaLength = Integer.BYTES + inventoryJsonBytes.length + Integer.BYTES + descriptorJsonBytes.length;
                if (descriptor.encryption != null)
                {
                    int b = descriptor.encryption.aesKeyLength;
                    metaLength += b - (inventoryJsonBytes.length % b);
                }

                buf = fc.map(FileChannel.MapMode.READ_WRITE, DBL_DATA_POS + Long.BYTES + dataBlocksLength, metaLength);
                buf.putInt(descriptorJsonBytes.length);
                buf.put(descriptorJsonBytes);

                if (descriptor.encryption == null)
                {
                    buf.putInt(inventoryJsonBytes.length);
                    buf.put(inventoryJsonBytes);
                }
                else
                {
                    PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator();
                    g.init(uic.getArchivePassword(), descriptor.encryption.pbkdf2Salt,
                           descriptor.encryption.pbkdf2Iterations);
                    ParametersWithIV kp = ((ParametersWithIV)g.generateDerivedParameters(
                            descriptor.encryption.aesKeyLength,
                            descriptor.encryption.aesKeyLength)
                    );

                    byte[] encryptedInv = SimpleAES.encrypt(
                            inventoryJsonBytes,
                            ((KeyParameter) kp.getParameters()).getKey(),
                            kp.getIV()
                    );

                    buf.putInt(encryptedInv.length);
                    buf.put(encryptedInv);
                }
            }
        }
    }
}
