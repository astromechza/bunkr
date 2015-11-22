package com.bunkr_beta_tests.scenarios;

import com.bunkr_beta.*;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.streams.input.MultilayeredInputStream;
import com.bunkr_beta.streams.output.MultilayeredOutputStream;
import com.bunkr_beta_tests.XTemporaryFolder;
import org.junit.Rule;
import org.junit.Test;

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
    public XTemporaryFolder folder = new XTemporaryFolder();

    private void runThreeFileTestOnContext(ArchiveInfoContext context) throws IOException
    {
        FileInventoryItem fileOne = new FileInventoryItem("a.txt");
        {
            context.getInventory().getFiles().add(fileOne);
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
            context.getInventory().getFiles().add(fileTwo);
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
            context.getInventory().getFiles().add(fileThree);
            MetadataWriter.write(context);
        }

        try(MultilayeredInputStream ms = new MultilayeredInputStream(context, fileOne))
        {
            byte[] buffer = new byte[1500];
            assertThat(IO.reliableRead(ms, buffer), is(equalTo(1500)));
            for (int i = 0; i < 1500; i++)
            {
                assertThat((int)buffer[i], is(equalTo(65 + i % 10)));
            }
        }

        try(MultilayeredInputStream ms = new MultilayeredInputStream(context, fileTwo))
        {
            byte[] buffer = new byte[50];
            assertThat(IO.reliableRead(ms, buffer), is(equalTo(50)));
            for (int i = 0; i < 50; i++)
            {
                assertThat((int)buffer[i], is(equalTo(75 + i % 10)));
            }
        }

        try(MultilayeredInputStream ms = new MultilayeredInputStream(context, fileThree))
        {
            assertThat(ms.read(), is(equalTo(-1)));
        }
    }

    @Test
    public void testReadingPlain() throws IOException, NoSuchAlgorithmException
    {
        File tempfile = folder.newPrefixedFile("plain");

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, null));

        runThreeFileTestOnContext(context);
    }

    @Test
    public void testReadingWithCompression() throws IOException, NoSuchAlgorithmException
    {
        File tempfile = folder.newPrefixedFile("withcompres");

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, new Descriptor.CompressionDescriptor("zlib")));

        runThreeFileTestOnContext(context);
    }

    @Test
    public void testReadingWithEncryption() throws IOException, NoSuchAlgorithmException
    {
        File tempfile = folder.newPrefixedFile("withencrypt");

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(new Descriptor
                .EncryptionDescriptor("", "", "".getBytes()), null));

        runThreeFileTestOnContext(context);
    }

    @Test
    public void testReadingWithCompressionAndEncryption() throws IOException, NoSuchAlgorithmException
    {
        File tempfile = folder.newPrefixedFile("withboth");

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(new Descriptor
                .EncryptionDescriptor("", "", "".getBytes()), new Descriptor.CompressionDescriptor("zlib")));

        runThreeFileTestOnContext(context);
    }
}
