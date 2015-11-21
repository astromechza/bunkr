package com.bunkr_beta_tests.scenarios;

import com.bunkr_beta.ArchiveBuilder;
import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.Descriptor;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.streams.input.MultilayeredInputStream;
import com.bunkr_beta.streams.output.MultilayeredOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public class TestReadScenarios
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testBasicReading() throws IOException, NoSuchAlgorithmException
    {
        File tempfile = folder.newFile();

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, null));
        FileInventoryItem fileOne = new FileInventoryItem("a.txt");
        {
            context.getArchiveInventory().files.add(fileOne);
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileOne))
            {
                for (int i = 0; i < 1500; i++)
                {
                    bwos.write(65 + i % 10);
                }
            }
            MetadataWriter.write(context);
        }
        FileInventoryItem fileTwo = new FileInventoryItem("b.txt");
        {
            context.getArchiveInventory().files.add(fileTwo);
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileTwo))
            {
                for (int i = 0; i < 50; i++)
                {
                    bwos.write(75 + i % 10);
                }
            }
            MetadataWriter.write(context);
        }
        FileInventoryItem fileThree = new FileInventoryItem("c.txt");
        {
            context.getArchiveInventory().files.add(fileThree);
            MetadataWriter.write(context);
        }

        try(MultilayeredInputStream ms = new MultilayeredInputStream(context, fileOne))
        {
            assertThat(ms.available(), is(equalTo(1500)));
            byte[] buffer = new byte[1500];
            assertThat(ms.read(buffer), is(equalTo(1500)));
            for (int i = 0; i < 1500; i++)
            {
                assertThat((int)buffer[i], is(equalTo(65 + i % 10)));
            }
        }

        try(MultilayeredInputStream ms = new MultilayeredInputStream(context, fileTwo))
        {
            assertThat(ms.available(), is(equalTo(50)));
            byte[] buffer = new byte[50];
            assertThat(ms.read(buffer), is(equalTo(50)));
            for (int i = 0; i < 50; i++)
            {
                assertThat((int)buffer[i], is(equalTo(75 + i % 10)));
            }
        }

        try(MultilayeredInputStream ms = new MultilayeredInputStream(context, fileThree))
        {
            assertThat(ms.available(), is(equalTo(0)));
        }
    }
}
