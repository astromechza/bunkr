package com.bunkr_beta_tests.scenarios;

import com.bunkr_beta.*;
import com.bunkr_beta.interfaces.IArchiveInfoContext;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.streams.MultilayeredOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

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

        System.out.println(tempfile);
        Files.copy(tempfile.toPath(), new File("./scenario_one.bunkr").toPath());
    }


}
