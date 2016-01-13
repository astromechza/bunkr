/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package test.bunkr.core.scenarios;

import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.*;
import org.bunkr.core.descriptor.DescriptorBuilder;
import org.bunkr.core.descriptor.IDescriptor;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.FolderInventoryItem;
import org.bunkr.core.inventory.Inventory;
import org.bunkr.core.inventory.InventoryJSON;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.IO;
import org.bunkr.core.utils.RandomMaker;
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
        UserSecurityProvider usp = new UserSecurityProvider(passProv);
        IArchiveInfoContext
                context = ArchiveBuilder.createNewEmptyArchive(tempfile, new PlaintextDescriptor(), usp);
        assertTrue(context.getInventory().getFiles().isEmpty());
        assertTrue(context.getInventory().getFolders().isEmpty());
        assertEquals(context.getBlockSize(), ArchiveBuilder.DEFAULT_BLOCK_SIZE);

        try(DataInputStream dis = new DataInputStream(new FileInputStream(tempfile)))
        {
            assertEquals(IO.readNByteString(dis, 5), "BUNKR");
            assertEquals(dis.read(), Version.versionMajor);
            assertEquals(dis.read(), Version.versionMinor);
            assertEquals(dis.read(), Version.versionBugfix);
            assertEquals(dis.readInt(), ArchiveBuilder.DEFAULT_BLOCK_SIZE);
            assertEquals(dis.readLong(), 0);

            String desJSON = IO.readNByteString(dis, dis.readInt());
            IDescriptor descriptor = DescriptorBuilder.fromJSON(desJSON);

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
        UserSecurityProvider usp = new UserSecurityProvider(passProv);
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new PlaintextDescriptor(), usp);

        FileInventoryItem newFile = new FileInventoryItem("some file.txt");
        context.getInventory().addFile(newFile);
        try(MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, newFile))
        {
            for (int i = 0; i < 3333; i++)
            {
                bwos.write(65 + i % 26);
            }
        }
        MetadataWriter.write(context, usp);

        try(DataInputStream dis = new DataInputStream(new FileInputStream(tempfile)))
        {
            assertEquals(IO.readNByteString(dis, 5), "BUNKR");
            assertEquals(dis.read(), Version.versionMajor);
            assertEquals(dis.read(), Version.versionMinor);
            assertEquals(dis.read(), Version.versionBugfix);
            assertEquals(dis.readInt(), ArchiveBuilder.DEFAULT_BLOCK_SIZE);
            assertEquals(dis.readLong(), 1024);
            byte[] data = new byte[1024];
            assertEquals(dis.read(data), 1024);

            FragmentedRange expected = new FragmentedRange(0, 1);

            String desJSON = IO.readNByteString(dis, dis.readInt());
            IDescriptor descriptor = DescriptorBuilder.fromJSON(desJSON);

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
        UserSecurityProvider usp = new UserSecurityProvider(passProv);
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new PlaintextDescriptor(), usp);

        FileInventoryItem fileOne = new FileInventoryItem("some file.txt");
        fileOne.addTag("bob");
        fileOne.addTag("charles");
        {
            context.getInventory().addFile(fileOne);
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileOne))
            {
                for (int i = 0; i < 3333; i++)
                {
                    bwos.write(65 + i % 26);
                }
            }
            MetadataWriter.write(context, usp);
        }

        FileInventoryItem fileTwo = new FileInventoryItem("another file.txt");
        fileTwo.addTag("thing_one");
        {
            context.getInventory().addFile(fileTwo);
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileTwo))
            {
                for (int i = 0; i < 50; i++)
                {
                    bwos.write(65 + i % 26);
                }
            }
            MetadataWriter.write(context, usp);
        }

        try(DataInputStream dis = new DataInputStream(new FileInputStream(tempfile)))
        {
            assertEquals(IO.readNByteString(dis, 5), "BUNKR");
            assertEquals(dis.read(), Version.versionMajor);
            assertEquals(dis.read(), Version.versionMinor);
            assertEquals(dis.read(), Version.versionBugfix);
            assertEquals(dis.readInt(), ArchiveBuilder.DEFAULT_BLOCK_SIZE);
            assertEquals(dis.readLong(), ArchiveBuilder.DEFAULT_BLOCK_SIZE + ArchiveBuilder.DEFAULT_BLOCK_SIZE);
            byte[] data = new byte[ArchiveBuilder.DEFAULT_BLOCK_SIZE];
            assertEquals(dis.read(data), data.length);
            data = new byte[ArchiveBuilder.DEFAULT_BLOCK_SIZE];
            assertEquals(dis.read(data), data.length);

            String desJSON = IO.readNByteString(dis, dis.readInt());
            IDescriptor descriptor = DescriptorBuilder.fromJSON(desJSON);

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
        UserSecurityProvider usp = new UserSecurityProvider(prov);
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new PlaintextDescriptor(), usp);

        FolderInventoryItem folder1 = new FolderInventoryItem("some folder");
        FolderInventoryItem folder2 = new FolderInventoryItem("another folder");
        FolderInventoryItem folder3 = new FolderInventoryItem("another folder");
        folder1.addFolder(folder2);
        context.getInventory().addFolder(folder1);
        context.getInventory().addFolder(folder3);

        FileInventoryItem newFile = new FileInventoryItem("some file.txt");
        folder1.addFile(newFile);
        MetadataWriter.write(context, usp);

        try(DataInputStream dis = new DataInputStream(new FileInputStream(tempfile)))
        {
            assertEquals(IO.readNByteString(dis, 5), "BUNKR");
            assertEquals(dis.read(), Version.versionMajor);
            assertEquals(dis.read(), Version.versionMinor);
            assertEquals(dis.read(), Version.versionBugfix);
            assertEquals(dis.readInt(), ArchiveBuilder.DEFAULT_BLOCK_SIZE);
            assertEquals(dis.readLong(), 0);

            String desJSON = IO.readNByteString(dis, dis.readInt());
            IDescriptor descriptor = DescriptorBuilder.fromJSON(desJSON);

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

    private long readDataLength(File target, UserSecurityProvider usp) throws Exception
    {
        ArchiveInfoContext ic = new ArchiveInfoContext(target, usp);
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
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(folder.newFile(), new PlaintextDescriptor(), usp);

        // now write 4 files
        for (int i = 0; i < 4; i++)
        {
            FileInventoryItem newFile = new FileInventoryItem("file" + (i + 1));
            context.getInventory().addFile(newFile);
            try(MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, newFile))
            {
                bwos.write(RandomMaker.get(4096 * 8));
            }
        }

        // now write metadatas
        MetadataWriter.write(context, usp);

        assertThat(readDataLength(context.filePath, usp), is(equalTo(5 * 4096L)));

        // now modify file 4
        {
            FileInventoryItem file4 = context.getInventory().findFile("file4");
            try(MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, file4))
            {
                bwos.write(RandomMaker.get(2048 * 8));
            }
        }

        // now write metadatas
        MetadataWriter.write(context, usp);

        assertThat(readDataLength(context.filePath, usp), is(equalTo(3 * 4096 + 4096 + 2048L)));

        // now remove file 3
        {
            FileInventoryItem file3 = context.getInventory().findFile("file3");
            context.getInventory().removeFile(file3);
        }

        // now write metadatas
        MetadataWriter.write(context, usp);

        assertThat(readDataLength(context.filePath, usp), is(equalTo(3 * 4096 + 4096 + 2048L)));

        // now remove file 4
        {
            FileInventoryItem file4 = context.getInventory().findFile("file4");
            context.getInventory().removeFile(file4);
        }

        // now write metadatas
        MetadataWriter.write(context, usp);

        assertThat(readDataLength(context.filePath, usp), is(equalTo(2 * 4096L + 2048L)));
    }
}
