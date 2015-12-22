package org.bunkr.core;

import org.bunkr.Version;
import org.bunkr.descriptor.IDescriptor;
import org.bunkr.exceptions.BaseBunkrException;
import org.bunkr.inventory.Inventory;
import org.bouncycastle.crypto.CryptoException;
import org.bunkr.usersec.UserSecurityProvider;

import java.io.*;
import java.util.ArrayList;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class ArchiveBuilder
{
    public static final byte[] FORMAT_SIG = "BUNKR".getBytes();
    public static final int DEFAULT_BLOCK_SIZE = 1024;

    public static ArchiveInfoContext createNewEmptyArchive(File path, IDescriptor descriptor, UserSecurityProvider uic, boolean compressed)
            throws IOException, CryptoException, BaseBunkrException
    {
        Inventory blankInventory = new Inventory(new ArrayList<>(), new ArrayList<>(), descriptor.mustEncryptFiles(), compressed);

        try(FileOutputStream fos = new FileOutputStream(path))
        {
            try(DataOutputStream dos = new DataOutputStream(fos))
            {
                dos.write(FORMAT_SIG);
                dos.write(Version.versionMajor);
                dos.write(Version.versionMinor);
                dos.write(Version.versionBugfix);
                dos.writeInt(DEFAULT_BLOCK_SIZE);
                dos.writeLong(0);
            }
        }
        MetadataWriter.write(path, blankInventory, descriptor, uic, DEFAULT_BLOCK_SIZE);
        return new ArchiveInfoContext(path, uic);
    }
}
