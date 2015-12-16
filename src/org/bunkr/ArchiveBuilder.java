package org.bunkr;

import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.descriptor.Descriptor;
import org.bunkr.exceptions.BaseBunkrException;
import org.bunkr.inventory.Inventory;
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
            throws IOException, CryptoException, BaseBunkrException
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
        MetadataWriter.write(path, blankInventory, descriptor, uic, DEFAULT_BLOCK_SIZE);
        return new ArchiveInfoContext(path, uic);
    }
}
