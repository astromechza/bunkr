package com.bunkr_beta_tests.scenarios;

import com.bunkr_beta.*;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.streams.input.MultilayeredInputStream;
import com.bunkr_beta.streams.output.MultilayeredOutputStream;
import com.bunkr_beta_tests.XTemporaryFolder;
import org.bouncycastle.crypto.CryptoException;
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

    private void runThreeFileTestOnContext(ArchiveInfoContext context, UserInfoContext uic) throws IOException, CryptoException
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
    public void testReadingPlain() throws IOException, NoSuchAlgorithmException, CryptoException
    {
        File tempfile = folder.newPrefixedFile("plain");
        UserInfoContext uic = new UserInfoContext("Hunter2".getBytes());

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, null), uic);

        runThreeFileTestOnContext(context, uic);
    }

    @Test
    public void testReadingWithCompression() throws IOException, NoSuchAlgorithmException, CryptoException
    {
        File tempfile = folder.newPrefixedFile("withcompres");
        UserInfoContext uic = new UserInfoContext("Hunter2".getBytes());

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(null, new Descriptor.CompressionDescriptor("ZLIB")), uic);

        runThreeFileTestOnContext(context, uic);
    }

    @Test
    public void testReadingWithEncryption() throws IOException, NoSuchAlgorithmException, CryptoException
    {
        File tempfile = folder.newPrefixedFile("withencrypt");
        UserInfoContext uic = new UserInfoContext("Hunter2".getBytes());

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(new Descriptor
                .EncryptionDescriptor(4096, 256, RandomMaker.get(64)), null), uic);

        runThreeFileTestOnContext(context, uic);
    }

    @Test
    public void testReadingWithCompressionAndEncryption() throws IOException, NoSuchAlgorithmException, CryptoException
    {
        File tempfile = folder.newPrefixedFile("withboth");
        UserInfoContext uic = new UserInfoContext("Hunter2".getBytes());

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new Descriptor(new Descriptor
                .EncryptionDescriptor(4096, 256, RandomMaker.get(64)), new Descriptor.CompressionDescriptor("ZLIB")), uic);

        runThreeFileTestOnContext(context, uic);
    }
}
