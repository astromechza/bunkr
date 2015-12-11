package com.bunkr_beta;

import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.descriptor.Descriptor;
import com.bunkr_beta.inventory.Inventory;
import org.bouncycastle.crypto.CryptoException;

import java.io.*;
import java.util.ArrayList;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class ArchiveBuilder
{
    public static final byte[] FORMAT_SIG = "BUNKR".getBytes();
    public static final byte[] VERSION_BYTES = new byte[] {0, 0, 1};
    public static final int DEFAULT_BLOCK_SIZE = 1024;

    public static ArchiveInfoContext createNewEmptyArchive(File path, Descriptor descriptor, PasswordProvider uic)
            throws IOException, CryptoException
    {
        Inventory blankInventory = new Inventory(new ArrayList<>(), new ArrayList<>());

        try(FileOutputStream fos = new FileOutputStream(path))
        {
            try(DataOutputStream dos = new DataOutputStream(fos))
            {
                dos.write(FORMAT_SIG);
                dos.write(VERSION_BYTES);
                dos.writeInt(DEFAULT_BLOCK_SIZE);
                dos.writeLong(0);
            }
        }
        MetadataWriter.write(path, blankInventory, descriptor, uic);
        return new ArchiveInfoContext(path, uic);
    }
}
