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

package test.bunkr.cli.commands;

import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.Algorithms;
import org.bunkr.core.inventory.Algorithms.Encryption;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.descriptor.PBKDF2Descriptor;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.utils.RandomMaker;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.CheckPasswordCommand;
import org.bunkr.core.usersec.PasswordProvider;
import test.bunkr.core.XTemporaryFolder;
import test.bunkr.cli.PasswordFile;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Rule;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Creator: benmeier
 * Created At: 2015-12-11
 */
public class TestCheckPasswordCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    @Test
    public void testBuildParser()
    {
        new CheckPasswordCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    @Test
    public void testSuccessfulAuthOnArchive() throws Exception
    {
        String password = DatatypeConverter.printHexBinary(RandomMaker.get(64));

        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword(password.getBytes());
        UserSecurityProvider usp = new UserSecurityProvider(prov);

        File archiveFile = folder.newFilePath();
        ArchiveBuilder.createNewEmptyArchive(
                archiveFile,
                PBKDF2Descriptor.make(Encryption.AES128_CTR, 10000),
                usp
        );

        File pwFile = PasswordFile.genPasswordFile(folder.newFilePath(), password.getBytes());

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, pwFile);
        new CheckPasswordCommand().handle(new Namespace(args));
    }


    @Test
    public void testFailedAuthOnArchive() throws Exception
    {
        String password = DatatypeConverter.printHexBinary(RandomMaker.get(64));
        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword(password.getBytes());
        UserSecurityProvider usp = new UserSecurityProvider(prov);
        File archiveFile = folder.newFilePath();
        ArchiveBuilder.createNewEmptyArchive(
                archiveFile,
                PBKDF2Descriptor.make(Encryption.AES128_CTR, 10000),
                usp
        );

        File pwFile = PasswordFile.genPasswordFile(folder.newFilePath(), "this is definitely not the password".getBytes());

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, pwFile);
        try
        {
            new CheckPasswordCommand().handle(new Namespace(args));
            fail("Should have raised an exception");
        }
        catch(BaseBunkrException e)
        {
            assertThat(e.getMessage(), is(equalTo("org.bouncycastle.crypto.InvalidCipherTextException: pad block corrupted")));
        }
    }


    @Test
    public void testSuccessfulAuthOnArchiveWithNoPassword() throws Exception
    {
        File archiveFile = folder.newFilePath();
        ArchiveBuilder.createNewEmptyArchive(
                archiveFile,
                new PlaintextDescriptor(),
                new UserSecurityProvider(new PasswordProvider())
        );

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        new CheckPasswordCommand().handle(new Namespace(args));
    }
}
