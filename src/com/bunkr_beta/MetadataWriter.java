package com.bunkr_beta;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.HC256Engine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
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

    public static void write(ArchiveInfoContext context) throws IOException, CryptoException
    {
        try(RandomAccessFile raf = new RandomAccessFile(context.filePath, "rw"))
        {
            try(FileChannel fc = raf.getChannel())
            {
                long dataBlocksLength;
                ByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY, DBL_DATA_POS, Long.BYTES);
                dataBlocksLength = buf.getLong();

                byte[] inventoryJsonBytes = IO.convertToJson(context.getInventory()).getBytes();
                byte[] descriptorJsonBytes = IO.convertToJson(context.getDescriptor()).getBytes();

                long metaLength = Integer.BYTES + inventoryJsonBytes.length + Integer.BYTES + descriptorJsonBytes.length;
                if (context.getDescriptor().encryption != null)
                {
                    int b = context.getDescriptor().encryption.aesKeyLength;
                    metaLength += b - (inventoryJsonBytes.length % b);
                }

                buf = fc.map(FileChannel.MapMode.READ_WRITE, DBL_DATA_POS + Long.BYTES + dataBlocksLength, metaLength);
                buf.putInt(descriptorJsonBytes.length);
                buf.put(descriptorJsonBytes);

                if (context.getDescriptor().encryption == null)
                {
                    buf.putInt(inventoryJsonBytes.length);
                    buf.put(inventoryJsonBytes);
                }
                else
                {
                    PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator();
                    g.init("password".getBytes(), context.getDescriptor().encryption.pbkdf2Salt,
                           context.getDescriptor().encryption.pbkdf2Iterations);
                    ParametersWithIV kp = ((ParametersWithIV)g.generateDerivedParameters(
                            context.getDescriptor().encryption.aesKeyLength,
                            context.getDescriptor().encryption.aesKeyLength)
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
