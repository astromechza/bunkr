package com.bunkr_beta;

import com.bunkr_beta.inventory.Inventory;

import java.io.*;
import java.security.NoSuchAlgorithmException;
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

    public static ArchiveInfoContext createNewEmptyArchive(File path, Descriptor descriptor)
            throws IOException, NoSuchAlgorithmException
    {
        Inventory blankInventory = new Inventory(new ArrayList<>(), new ArrayList<>());

        String inventoryJson = IO.convertToJson(blankInventory);
        String descriptorJson = IO.convertToJson(descriptor);

        try(FileOutputStream fos = new FileOutputStream(path))
        {
            try(DataOutputStream dos = new DataOutputStream(fos))
            {
                dos.write(FORMAT_SIG);
                dos.write(VERSION_BYTES);
                dos.writeInt(DEFAULT_BLOCK_SIZE);
                dos.writeLong(0);
                dos.writeInt(inventoryJson.length());
                dos.writeBytes(inventoryJson);
                dos.writeInt(descriptorJson.length());
                dos.writeBytes(descriptorJson);
            }
        }
        return new ArchiveInfoContext(path);
    }
}
