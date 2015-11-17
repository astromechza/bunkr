package com.bunkr_beta_tests.scenarios;

import com.bunkr_beta.*;
import com.bunkr_beta.interfaces.IArchiveInfoContext;
import com.bunkr_beta.inventory.FileInventoryItem;
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
import java.nio.file.Files;
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
    public void TestScenarioOne() throws IOException, NoSuchAlgorithmException
    {
        File tempfile = folder.newFile("scenario_one.bunkr");
        IArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, null));
        assertTrue(context.getArchiveInventory().files.isEmpty());
        assertTrue(context.getArchiveInventory().folders.isEmpty());
        assertTrue(context.isFresh());
        assertEquals(context.getBlockSize(), ArchiveBuilder.DEFAULT_BLOCK_SIZE);
    }

    @Test
    public void TestScenarioTwo() throws IOException, NoSuchAlgorithmException
    {
        File tempfile = folder.newFile("scenario_one.bunkr");

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
            dis.read(data);
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
            assertEquals(inventory.files.get(0).getSize(), newFile.getSize());
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


}
