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

import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.Algorithms;
import org.bunkr.core.inventory.Algorithms.Encryption;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.descriptor.PBKDF2Descriptor;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.utils.IO;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import test.bunkr.core.XTemporaryFolder;
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

    private void runThreeFileTestOnContext(ArchiveInfoContext context, UserSecurityProvider uic) throws Exception
    {
        FileInventoryItem fileOne = new FileInventoryItem("a.txt");
        {
            context.getInventory().addFile(fileOne);
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
            context.getInventory().addFile(fileTwo);
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
            context.getInventory().addFile(fileThree);
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
        UserSecurityProvider usp = new UserSecurityProvider(prov);

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, new PlaintextDescriptor(), usp);

        runThreeFileTestOnContext(context, usp);
    }

    @Test
    public void testReadingWithPBKDF2_AES256_CTR() throws Exception
    {
        File tempfile = folder.newPrefixedFile("withencrypt");
        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword("HunterTwo".getBytes());
        UserSecurityProvider usp = new UserSecurityProvider(prov);

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, PBKDF2Descriptor.make(
                Encryption.AES128_CTR, 10000), usp);
        context.getInventory().setDefaultEncryption(Encryption.AES256_CTR);

        runThreeFileTestOnContext(context, usp);
    }

    @Test
    public void testReadingWithPBKDF2_TWOFISH256_CTR() throws Exception
    {
        File tempfile = folder.newPrefixedFile("withencrypt");
        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword("HunterTwo".getBytes());
        UserSecurityProvider usp = new UserSecurityProvider(prov);

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, PBKDF2Descriptor.make(
                Encryption.AES128_CTR, 10000), usp);
        context.getInventory().setDefaultEncryption(Encryption.TWOFISH256_CTR);

        runThreeFileTestOnContext(context, usp);
    }

    @Test
    public void testReadingWithPBKDF2_AES128_CTR() throws Exception
    {
        File tempfile = folder.newPrefixedFile("withencrypt");
        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword("HunterTwo".getBytes());
        UserSecurityProvider usp = new UserSecurityProvider(prov);

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, PBKDF2Descriptor.make(
                Encryption.AES128_CTR, 10000), usp);
        context.getInventory().setDefaultEncryption(Encryption.AES128_CTR);

        runThreeFileTestOnContext(context, usp);
    }

    @Test
    public void testReadingWithPBKDF2_TWOFISH128_CTR() throws Exception
    {
        File tempfile = folder.newPrefixedFile("withencrypt");
        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword("HunterTwo".getBytes());
        UserSecurityProvider usp = new UserSecurityProvider(prov);

        // first create the demo file
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(tempfile, PBKDF2Descriptor.make(
                Encryption.AES128_CTR, 10000), usp);
        context.getInventory().setDefaultEncryption(Encryption.TWOFISH128_CTR);

        runThreeFileTestOnContext(context, usp);
    }
}
