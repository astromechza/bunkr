package org.bunkr_tests.cli.commands;

import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.UserSecurityProvider;
import org.bunkr.descriptor.PBKDF2Descriptor;
import org.bunkr.descriptor.PlaintextDescriptor;
import org.bunkr.exceptions.BaseBunkrException;
import org.bunkr.utils.RandomMaker;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.CheckPasswordCommand;
import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr_tests.XTemporaryFolder;
import org.bunkr_tests.cli.PasswordFile;
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
                PBKDF2Descriptor.makeDefaults(),
                usp,
                true
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
                PBKDF2Descriptor.makeDefaults(),
                usp,
                true
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
                new UserSecurityProvider(new PasswordProvider()),
                true
        );

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        new CheckPasswordCommand().handle(new Namespace(args));
    }
}
