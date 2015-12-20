package org.bunkr_tests.scenarios;

import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.utils.IO;
import org.bunkr.core.MetadataWriter;
import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.descriptor.Descriptor;
import org.bunkr.descriptor.EncryptionDescriptor;
import org.bunkr.inventory.FileInventoryItem;
import org.bunkr.streams.input.MultilayeredInputStream;
import org.bunkr.streams.output.MultilayeredOutputStream;
import org.bunkr_tests.XTemporaryFolder;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

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
    public final XTemporaryFolder folder = new XTemporaryFolder();

    private void runThreeFileTestOnContext(ArchiveInfoContext context, PasswordProvider uic) throws Exception
    {
        FileInventoryItem fileOne = new FileInventoryItem("a.txt");
        fileOne.addTag("something");
        fileOne.addTag("another_thing");
        {
            context.getInventory().getFiles().add(fileOne);
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileOne))
            {
                for (int i = 0; i < 1500; i++)
                {
                    bwos.write(65 + i % 10);
                }
            }
            MetadataWriter.write(context, uic);
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
            MetadataWriter.write(context, uic);
        }
        FileInventoryItem fileThree = new FileInventoryItem("c.txt");
        fileThree.addTag("bob");
        fileThree.addTag("charles");
        {
            context.getInventory().getFiles().add(fileThree);
            MetadataWriter.write(context, uic);
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
    public void testReadingPlain() throws Exception
    {
        File tempfile = folder.newPrefixedFile("plain");
        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword("HunterTwo".getBytes());

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null), prov, false, false);

        runThreeFileTestOnContext(context, prov);
    }

    @Test
    public void testReadingWithCompression() throws Exception
    {
        File tempfile = folder.newPrefixedFile("withcompres");
        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword("HunterTwo".getBytes());

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null), prov, false, true);

        runThreeFileTestOnContext(context, prov);
    }

    @Test
    public void testReadingWithEncryption() throws Exception
    {
        File tempfile = folder.newPrefixedFile("withencrypt");
        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword("HunterTwo".getBytes());

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(EncryptionDescriptor.makeDefaults()), prov, true, false);

        runThreeFileTestOnContext(context, prov);
    }

    @Test
    public void testReadingWithCompressionAndEncryption() throws Exception
    {
        File tempfile = folder.newPrefixedFile("withboth");
        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword("HunterTwo".getBytes());

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(EncryptionDescriptor.makeDefaults()), prov, true, true);

        runThreeFileTestOnContext(context, prov);
    }
}
