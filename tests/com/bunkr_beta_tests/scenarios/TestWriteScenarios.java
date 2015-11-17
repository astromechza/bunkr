package com.bunkr_beta_tests.scenarios;

import com.bunkr_beta.*;
import com.bunkr_beta.interfaces.IArchiveInfoContext;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.FolderInventoryItem;
import com.bunkr_beta.inventory.Inventory;
import com.bunkr_beta.streams.MultilayeredOutputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public class TestWriteScenarios
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testEmptyArchive() throws IOException, NoSuchAlgorithmException
    {
        File tempfile = folder.newFile();
        IArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, null));
        assertTrue(context.getArchiveInventory().files.isEmpty());
        assertTrue(context.getArchiveInventory().folders.isEmpty());
        assertTrue(context.isFresh());
        assertEquals(context.getBlockSize(), ArchiveBuilder.DEFAULT_BLOCK_SIZE);

        try(DataInputStream dis = new DataInputStream(new FileInputStream(tempfile)))
        {
            assertEquals(IO.readNByteString(dis, 5), "BUNKR");
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 1);
            assertEquals(dis.readInt(), 1024);
            assertEquals(dis.readLong(), 0);

            String invJSON = IO.readNByteString(dis, dis.readInt());
            Inventory inventory = new ObjectMapper().readValue(invJSON, Inventory.class);
            assertEquals(inventory.files.size(), 0);
            assertEquals(inventory.folders.size(), 0);

            String desJSON = IO.readNByteString(dis, dis.readInt());
            Descriptor descriptor = new ObjectMapper().readValue(desJSON, Descriptor.class);

            assertEquals(descriptor.compression, null);
            assertEquals(descriptor.encryption, null);

            assertEquals(dis.available(), 0);
        }
    }

    @Test
    public void testSingleFile() throws IOException, NoSuchAlgorithmException
    {
        File tempfile = folder.newFile();

        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, null));

        FileInventoryItem newFile = new FileInventoryItem("some file.txt");
        context.getArchiveInventory().files.add(newFile);
        try(MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, newFile))
        {
            for (int i = 0; i < 3333; i++)
            {
                bwos.write(65 + i % 26);
            }
        }
        MetadataWriter.write(context);

        try(DataInputStream dis = new DataInputStream(new FileInputStream(tempfile)))
        {
            assertEquals(IO.readNByteString(dis, 5), "BUNKR");
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 1);
            assertEquals(dis.readInt(), 1024);
            assertEquals(dis.readLong(), 4096);
            byte[] data = new byte[4096];
            assertEquals(dis.read(data), 4096);
            for (int i = 0; i < 3333; i++)
            {
                assertEquals(data[i], (65 + i % 26));
            }
            String invJSON = IO.readNByteString(dis, dis.readInt());
            Inventory inventory = new ObjectMapper().readValue(invJSON, Inventory.class);
            assertEquals(inventory.files.size(), 1);
            assertEquals(inventory.folders.size(), 0);
            assertEquals(inventory.files.get(0).getName(), "some file.txt");
            assertEquals(inventory.files.get(0).getBlocks().toString(), "FragmentedRange{0,1,2,3,}");
            assertEquals(inventory.files.get(0).getUuid(), newFile.getUuid());
            assertEquals(inventory.files.get(0).getSizeOnDisk(), newFile.getSizeOnDisk());
            assertEquals(inventory.files.get(0).getModifiedAt(), newFile.getModifiedAt());
            assertArrayEquals(inventory.files.get(0).getEncryptionIV(), newFile.getEncryptionIV());
            assertArrayEquals(inventory.files.get(0).getEncryptionKey(), newFile.getEncryptionKey());

            String desJSON = IO.readNByteString(dis, dis.readInt());
            Descriptor descriptor = new ObjectMapper().readValue(desJSON, Descriptor.class);

            assertEquals(descriptor.compression, null);
            assertEquals(descriptor.encryption, null);

            assertEquals(dis.available(), 0);
        }
    }

    @Test
    public void testMultipleFiles() throws IOException, NoSuchAlgorithmException
    {
        File tempfile = folder.newFile();
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, null));

        FileInventoryItem fileOne = new FileInventoryItem("some file.txt");
        {
            context.getArchiveInventory().files.add(fileOne);
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileOne))
            {
                for (int i = 0; i < 3333; i++)
                {
                    bwos.write(65 + i % 26);
                }
            }
            MetadataWriter.write(context);
        }

        FileInventoryItem fileTwo = new FileInventoryItem("another file.txt");
        {
            context.getArchiveInventory().files.add(fileTwo);
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileTwo))
            {
                for (int i = 0; i < 50; i++)
                {
                    bwos.write(65 + i % 26);
                }
            }
            MetadataWriter.write(context);
        }

        try(DataInputStream dis = new DataInputStream(new FileInputStream(tempfile)))
        {
            assertEquals(IO.readNByteString(dis, 5), "BUNKR");
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 1);
            assertEquals(dis.readInt(), 1024);
            assertEquals(dis.readLong(), 5120);
            byte[] data = new byte[4096];
            assertEquals(dis.read(data), 4096);
            for (int i = 0; i < 3333; i++)
            {
                assertEquals(data[i], (65 + i % 26));
            }
            data = new byte[1024];
            assertEquals(dis.read(data), 1024);
            for (int i = 0; i < 50; i++)
            {
                assertEquals(data[i], (65 + i % 26));
            }
            String invJSON = IO.readNByteString(dis, dis.readInt());
            Inventory inventory = new ObjectMapper().readValue(invJSON, Inventory.class);
            assertEquals(inventory.files.size(), 2);
            assertEquals(inventory.folders.size(), 0);

            assertEquals(inventory.files.get(0).getName(), fileOne.getName());
            assertEquals(inventory.files.get(0).getBlocks().toString(), fileOne.getBlocks().toString());
            assertEquals(inventory.files.get(0).getUuid(), fileOne.getUuid());
            assertEquals(inventory.files.get(0).getSizeOnDisk(), fileOne.getSizeOnDisk());
            assertEquals(inventory.files.get(0).getModifiedAt(), fileOne.getModifiedAt());
            assertArrayEquals(inventory.files.get(0).getEncryptionIV(), fileOne.getEncryptionIV());
            assertArrayEquals(inventory.files.get(0).getEncryptionKey(), fileOne.getEncryptionKey());

            assertEquals(inventory.files.get(1).getName(), fileTwo.getName());
            assertEquals(inventory.files.get(1).getBlocks().toString(), fileTwo.getBlocks().toString());
            assertEquals(inventory.files.get(1).getUuid(), fileTwo.getUuid());
            assertEquals(inventory.files.get(1).getSizeOnDisk(), fileTwo.getSizeOnDisk());
            assertEquals(inventory.files.get(1).getModifiedAt(), fileTwo.getModifiedAt());
            assertArrayEquals(inventory.files.get(1).getEncryptionIV(), fileTwo.getEncryptionIV());
            assertArrayEquals(inventory.files.get(1).getEncryptionKey(), fileTwo.getEncryptionKey());

            String desJSON = IO.readNByteString(dis, dis.readInt());
            Descriptor descriptor = new ObjectMapper().readValue(desJSON, Descriptor.class);

            assertEquals(descriptor.compression, null);
            assertEquals(descriptor.encryption, null);

            assertEquals(dis.available(), 0);
        }
    }

    @Test
    public void testFoldersAndFile() throws IOException, NoSuchAlgorithmException
    {
        File tempfile = folder.newFile();
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, null));

        FolderInventoryItem folder1 = new FolderInventoryItem("some folder");
        FolderInventoryItem folder2 = new FolderInventoryItem("another folder");
        FolderInventoryItem folder3 = new FolderInventoryItem("another folder");
        folder1.getFolders().add(folder2);
        context.getArchiveInventory().folders.add(folder1);
        context.getArchiveInventory().folders.add(folder3);

        FileInventoryItem newFile = new FileInventoryItem("some file.txt");
        folder1.getFiles().add(newFile);
        MetadataWriter.write(context);

        try(DataInputStream dis = new DataInputStream(new FileInputStream(tempfile)))
        {
            assertEquals(IO.readNByteString(dis, 5), "BUNKR");
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 1);
            assertEquals(dis.readInt(), 1024);
            assertEquals(dis.readLong(), 0);
            String invJSON = IO.readNByteString(dis, dis.readInt());
            Inventory inventory = new ObjectMapper().readValue(invJSON, Inventory.class);

            assertEquals(inventory.files.size(), 0);
            assertEquals(inventory.folders.size(), 2);

            FileInventoryItem holder = inventory.folders.get(0).getFiles().get(0);
            assertEquals(holder.getName(), "some file.txt");
            assertEquals(holder.getBlocks().toString(), "FragmentedRange{}");
            assertEquals(holder.getUuid(), newFile.getUuid());
            assertEquals(holder.getSizeOnDisk(), newFile.getSizeOnDisk());
            assertEquals(holder.getModifiedAt(), newFile.getModifiedAt());
            assertArrayEquals(holder.getEncryptionIV(), newFile.getEncryptionIV());
            assertArrayEquals(holder.getEncryptionKey(), newFile.getEncryptionKey());

            String desJSON = IO.readNByteString(dis, dis.readInt());
            Descriptor descriptor = new ObjectMapper().readValue(desJSON, Descriptor.class);

            assertEquals(descriptor.compression, null);
            assertEquals(descriptor.encryption, null);

            assertEquals(dis.available(), 0);
        }
    }
}
