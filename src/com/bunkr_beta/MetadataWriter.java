package com.bunkr_beta;

import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.HC256Engine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.modes.SICBlockCipher;
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

    public static void write(ArchiveInfoContext context) throws IOException
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

                buf = fc.map(FileChannel.MapMode.READ_WRITE, DBL_DATA_POS + Long.BYTES + dataBlocksLength, metaLength);
                buf.putInt(descriptorJsonBytes.length);
                buf.put(descriptorJsonBytes);

                buf.putInt(inventoryJsonBytes.length);
                if (context.getDescriptor().encryption == null)
                {
                    buf.put(inventoryJsonBytes);
                }
                else
                {
                    PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator();
                    g.init("password".getBytes(), "SALTYBUNKR".getBytes(),
                           context.getDescriptor().encryption.pbkdf2Iterations);
                    ParametersWithIV kp = ((ParametersWithIV)g.generateDerivedParameters(
                            context.getDescriptor().encryption.aesKeyLength,
                            context.getDescriptor().encryption.aesKeyLength)
                    );

                    HC256Engine cipher = new HC256Engine();
                    cipher.init(true, kp);
                    byte[] encryptedInv = new byte[inventoryJsonBytes.length];
                    cipher.processBytes(inventoryJsonBytes, 0, inventoryJsonBytes.length, encryptedInv, 0);
                    buf.put(encryptedInv);
                }
            }
        }
    }
}
