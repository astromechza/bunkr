package org.bunkr_tests.scenarios;

import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.IArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.descriptor.Descriptor;
import org.bunkr.descriptor.DescriptorJSON;
import org.bunkr.fragmented_range.FragmentedRange;
import org.bunkr.inventory.FileInventoryItem;
import org.bunkr.inventory.FolderInventoryItem;
import org.bunkr.inventory.Inventory;
import org.bunkr.inventory.InventoryJSON;
import org.bunkr.streams.output.MultilayeredOutputStream;
import org.bunkr.utils.IO;
import org.bunkr.utils.RandomMaker;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public class TestWriteScenarios
{
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    public long updiv(long n, long d)
    {
        return (long) Math.ceil(n / (float) d);
    }

    public long upround(long n, long d)
    {
        return updiv(n, d) * d;
    }

    @Test
    public void testEmptyArchive() throws Exception
    {
        File tempfile = folder.newFile();
        PasswordProvider passProv = new PasswordProvider();
        IArchiveInfoContext
                context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, null), passProv);
        assertTrue(context.getInventory().getFiles().isEmpty());
        assertTrue(context.getInventory().getFolders().isEmpty());
        assertTrue(context.isFresh());
        assertEquals(context.getBlockSize(), ArchiveBuilder.DEFAULT_BLOCK_SIZE);

        try(DataInputStream dis = new DataInputStream(new FileInputStream(tempfile)))
        {
            assertEquals(IO.readNByteString(dis, 5), "BUNKR");
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 1);
            assertEquals(dis.readInt(), ArchiveBuilder.DEFAULT_BLOCK_SIZE);
            assertEquals(dis.readLong(), 0);

            String desJSON = IO.readNByteString(dis, dis.readInt());
            Descriptor descriptor = DescriptorJSON.decode(desJSON);

            assertEquals(descriptor.getCompression(), null);
            assertEquals(descriptor.getEncryption(), null);

            String invJSON = IO.readNByteString(dis, dis.readInt());
            Inventory inventory = InventoryJSON.decode(invJSON);
            assertEquals(inventory.getFiles().size(), 0);
            assertEquals(inventory.getFolders().size(), 0);

            assertEquals(dis.available(), 0);
        }
    }

    @Test
    public void testSingleFile() throws Exception
    {
        File tempfile = folder.newFile();

        PasswordProvider passProv = new PasswordProvider();
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, null), passProv);

        FileInventoryItem newFile = new FileInventoryItem("some file.txt");
        context.getInventory().getFiles().add(newFile);
        try(MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, newFile))
        {
            for (int i = 0; i < 3333; i++)
            {
                bwos.write(65 + i % 26);
            }
        }
        MetadataWriter.write(context, passProv);

        try(DataInputStream dis = new DataInputStream(new FileInputStream(tempfile)))
        {
            assertEquals(IO.readNByteString(dis, 5), "BUNKR");
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 1);
            assertEquals(dis.readInt(), ArchiveBuilder.DEFAULT_BLOCK_SIZE);
            assertEquals(dis.readLong(), 4096);
            byte[] data = new byte[4096];
            assertEquals(dis.read(data), 4096);
            for (int i = 0; i < 3333; i++)
            {
                assertEquals(data[i], (65 + i % 26));
            }

            String desJSON = IO.readNByteString(dis, dis.readInt());
            Descriptor descriptor = DescriptorJSON.decode(desJSON);

            assertEquals(descriptor.getCompression(), null);
            assertEquals(descriptor.getEncryption(), null);

            FragmentedRange expected = new FragmentedRange(0, 4096 / ArchiveBuilder.DEFAULT_BLOCK_SIZE);

            String invJSON = IO.readNByteString(dis, dis.readInt());
            Inventory inventory = InventoryJSON.decode(invJSON);
            assertEquals(inventory.getFiles().size(), 1);
            assertEquals(inventory.getFolders().size(), 0);
            assertEquals(inventory.getFiles().get(0).getName(), "some file.txt");
            assertEquals(inventory.getFiles().get(0).getBlocks().toString(), expected.toString());
            assertEquals(inventory.getFiles().get(0).getUuid(), newFile.getUuid());
            assertEquals(inventory.getFiles().get(0).getSizeOnDisk(), newFile.getSizeOnDisk());
            assertEquals(inventory.getFiles().get(0).getModifiedAt(), newFile.getModifiedAt());
            assertArrayEquals(inventory.getFiles().get(0).getEncryptionData(), newFile.getEncryptionData());

            assertEquals(dis.available(), 0);
        }
    }

    @Test
    public void testMultipleFiles() throws Exception
    {
        File tempfile = folder.newFile();
        PasswordProvider passProv = new PasswordProvider();
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, null), passProv);

        FileInventoryItem fileOne = new FileInventoryItem("some file.txt");
        fileOne.addTag("bob");
        fileOne.addTag("charles");
        {
            context.getInventory().getFiles().add(fileOne);
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileOne))
            {
                for (int i = 0; i < 3333; i++)
                {
                    bwos.write(65 + i % 26);
                }
            }
            MetadataWriter.write(context, passProv);
        }

        FileInventoryItem fileTwo = new FileInventoryItem("another file.txt");
        fileTwo.addTag("thing_one");
        {
            context.getInventory().getFiles().add(fileTwo);
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileTwo))
            {
                for (int i = 0; i < 50; i++)
                {
                    bwos.write(65 + i % 26);
                }
            }
            MetadataWriter.write(context, passProv);
        }

        try(DataInputStream dis = new DataInputStream(new FileInputStream(tempfile)))
        {
            assertEquals(IO.readNByteString(dis, 5), "BUNKR");
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 1);
            assertEquals(dis.readInt(), ArchiveBuilder.DEFAULT_BLOCK_SIZE);
            assertEquals(dis.readLong(), upround(3333, ArchiveBuilder.DEFAULT_BLOCK_SIZE) + upround(50, ArchiveBuilder.DEFAULT_BLOCK_SIZE));
            byte[] data = new byte[(int) upround(3333, ArchiveBuilder.DEFAULT_BLOCK_SIZE)];
            assertEquals(dis.read(data), data.length);
            for (int i = 0; i < 3333; i++)
            {
                assertEquals(data[i], (65 + i % 26));
            }
            data = new byte[ArchiveBuilder.DEFAULT_BLOCK_SIZE];
            assertEquals(dis.read(data), data.length);
            for (int i = 0; i < 50; i++)
            {
                assertEquals(data[i], (65 + i % 26));
            }

            String desJSON = IO.readNByteString(dis, dis.readInt());
            Descriptor descriptor = DescriptorJSON.decode(desJSON);

            assertEquals(descriptor.getCompression(), null);
            assertEquals(descriptor.getEncryption(), null);

            String invJSON = IO.readNByteString(dis, dis.readInt());
            Inventory inventory = InventoryJSON.decode(invJSON);
            assertEquals(inventory.getFiles().size(), 2);
            assertEquals(inventory.getFolders().size(), 0);

            assertEquals(inventory.getFiles().get(0).getName(), fileOne.getName());
            assertTrue(inventory.getFiles().get(0).hasTag("bob"));
            assertTrue(inventory.getFiles().get(0).hasTag("charles"));
            assertFalse(inventory.getFiles().get(0).hasTag("john"));
            assertEquals(inventory.getFiles().get(0).getBlocks().toString(), fileOne.getBlocks().toString());
            assertEquals(inventory.getFiles().get(0).getUuid(), fileOne.getUuid());
            assertEquals(inventory.getFiles().get(0).getSizeOnDisk(), fileOne.getSizeOnDisk());
            assertEquals(inventory.getFiles().get(0).getModifiedAt(), fileOne.getModifiedAt());
            assertArrayEquals(inventory.getFiles().get(0).getEncryptionData(), fileOne.getEncryptionData());

            assertEquals(inventory.getFiles().get(1).getName(), fileTwo.getName());
            assertTrue(inventory.getFiles().get(1).hasTag("thing_one"));
            assertFalse(inventory.getFiles().get(1).hasTag("charles"));
            assertEquals(inventory.getFiles().get(1).getBlocks().toString(), fileTwo.getBlocks().toString());
            assertEquals(inventory.getFiles().get(1).getUuid(), fileTwo.getUuid());
            assertEquals(inventory.getFiles().get(1).getSizeOnDisk(), fileTwo.getSizeOnDisk());
            assertEquals(inventory.getFiles().get(1).getModifiedAt(), fileTwo.getModifiedAt());
            assertArrayEquals(inventory.getFiles().get(1).getEncryptionData(), fileTwo.getEncryptionData());

            assertEquals(dis.available(), 0);
        }
    }

    @Test
    public void testFoldersAndFile() throws Exception
    {
        File tempfile = folder.newFile();
        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword("HunterTwo".getBytes());
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, null), prov);

        FolderInventoryItem folder1 = new FolderInventoryItem("some folder");
        FolderInventoryItem folder2 = new FolderInventoryItem("another folder");
        FolderInventoryItem folder3 = new FolderInventoryItem("another folder");
        folder1.getFolders().add(folder2);
        context.getInventory().getFolders().add(folder1);
        context.getInventory().getFolders().add(folder3);

        FileInventoryItem newFile = new FileInventoryItem("some file.txt");
        folder1.getFiles().add(newFile);
        MetadataWriter.write(context, prov);

        try(DataInputStream dis = new DataInputStream(new FileInputStream(tempfile)))
        {
            assertEquals(IO.readNByteString(dis, 5), "BUNKR");
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 0);
            assertEquals(dis.read(), 1);
            assertEquals(dis.readInt(), ArchiveBuilder.DEFAULT_BLOCK_SIZE);
            assertEquals(dis.readLong(), 0);

            String desJSON = IO.readNByteString(dis, dis.readInt());
            Descriptor descriptor = DescriptorJSON.decode(desJSON);

            assertEquals(descriptor.getCompression(), null);
            assertEquals(descriptor.getEncryption(), null);

            String invJSON = IO.readNByteString(dis, dis.readInt());
            Inventory inventory = InventoryJSON.decode(invJSON);

            assertEquals(inventory.getFiles().size(), 0);
            assertEquals(inventory.getFolders().size(), 2);

            FileInventoryItem holder = inventory.getFolders().get(0).getFiles().get(0);
            assertEquals(holder.getName(), "some file.txt");
            assertEquals(holder.getBlocks().toString(), "FragmentedRange{}");
            assertEquals(holder.getUuid(), newFile.getUuid());
            assertEquals(holder.getSizeOnDisk(), newFile.getSizeOnDisk());
            assertEquals(holder.getModifiedAt(), newFile.getModifiedAt());
            assertArrayEquals(holder.getEncryptionData(), newFile.getEncryptionData());

            assertEquals(dis.available(), 0);
        }
    }

    private long readDataLength(File target) throws Exception
    {
        ArchiveInfoContext ic = new ArchiveInfoContext(target, new PasswordProvider());
        return ic.getBlockDataLength();
    }

    @Test
    public void testTruncatingUsedBlocks() throws Exception
    {
        /*
         block size = 1024
         1. add 4 small files each of 4096 bytes
         2. rewrite file 4 to 2048 bytes, check that file was reduced
         3. remove file 3, check that no change in files
         4. remove file 4, check that file was reduced completely
         */

        // first create empty archive
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(folder.newFile(), new Descriptor(null, null), new PasswordProvider());

        // now write 4 files
        for (int i = 0; i < 4; i++)
        {
            FileInventoryItem newFile = new FileInventoryItem("file" + (i + 1));
            context.getInventory().getFiles().add(newFile);
            try(MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, newFile))
            {
                bwos.write(RandomMaker.get(4096 * 8));
            }
        }

        // now write metadatas
        MetadataWriter.write(context, new PasswordProvider());

        assertThat(readDataLength(context.filePath), is(equalTo(4 * 4096L)));

        // now modify file 4
        {
            FileInventoryItem file4 = context.getInventory().findFile("file4");
            try(MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, file4))
            {
                bwos.write(RandomMaker.get(2048 * 8));
            }
        }

        // now write metadatas
        MetadataWriter.write(context, new PasswordProvider());

        assertThat(readDataLength(context.filePath), is(equalTo(2 * 4096 + 4096 + 2048L)));

        // now remove file 3
        {
            FileInventoryItem file3 = context.getInventory().findFile("file3");
            context.getInventory().getFiles().remove(file3);
        }

        // now write metadatas
        MetadataWriter.write(context, new PasswordProvider());

        assertThat(readDataLength(context.filePath), is(equalTo(2 * 4096 + 4096 + 2048L)));

        // now remove file 4
        {
            FileInventoryItem file4 = context.getInventory().findFile("file4");
            context.getInventory().getFiles().remove(file4);
        }

        // now write metadatas
        MetadataWriter.write(context, new PasswordProvider());

        assertThat(readDataLength(context.filePath), is(equalTo(2 * 4096L)));
    }
}
