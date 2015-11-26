package com.bunkr_beta;

import com.bunkr_beta.inventory.Inventory;
import org.bouncycastle.crypto.engines.HC256Engine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class ArchiveBuilder
{
    public static final byte[] FORMAT_SIG = "BUNKR".getBytes();
    public static final byte[] VERSION_BYTES = new byte[] {0, 0, 1};
    public static final int DEFAULT_BLOCK_SIZE = 1024;

    public static ArchiveInfoContext createNewEmptyArchive(File path, Descriptor descriptor)
            throws IOException, NoSuchAlgorithmException
    {
        Inventory blankInventory = new Inventory(new ArrayList<>(), new ArrayList<>());

        byte[] inventoryJsonBytes = IO.convertToJson(blankInventory).getBytes();
        byte[] descriptorJsonBytes = IO.convertToJson(descriptor).getBytes();

        try(FileOutputStream fos = new FileOutputStream(path))
        {
            try(DataOutputStream dos = new DataOutputStream(fos))
            {
                dos.write(FORMAT_SIG);
                dos.write(VERSION_BYTES);
                dos.writeInt(DEFAULT_BLOCK_SIZE);
                dos.writeLong(0);
                dos.writeInt(descriptorJsonBytes.length);
                dos.write(descriptorJsonBytes);

                dos.writeInt(inventoryJsonBytes.length);
                if (descriptor.encryption == null)
                {
                    dos.write(inventoryJsonBytes);
                }
                else
                {
                    PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator();
                    g.init("password".getBytes(), descriptor.encryption.pbkdf2Salt,
                           descriptor.encryption.pbkdf2Iterations);
                    ParametersWithIV kp = ((ParametersWithIV)g.generateDerivedParameters(
                            descriptor.encryption.aesKeyLength,
                            descriptor.encryption.aesKeyLength)
                    );

                    HC256Engine cipher = new HC256Engine();
                    cipher.init(true, kp);
                    byte[] encryptedInv = new byte[inventoryJsonBytes.length];
                    cipher.processBytes(inventoryJsonBytes, 0, inventoryJsonBytes.length, encryptedInv, 0);
                    dos.write(encryptedInv);
                }

            }
        }
        return new ArchiveInfoContext(path);
    }
}
