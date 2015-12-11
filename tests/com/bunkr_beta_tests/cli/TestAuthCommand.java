package com.bunkr_beta_tests.cli;

import com.bunkr_beta.ArchiveBuilder;
import com.bunkr_beta.RandomMaker;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.cli.commands.AuthCommand;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.descriptor.CompressionDescriptor;
import com.bunkr_beta.descriptor.Descriptor;
import com.bunkr_beta.descriptor.EncryptionDescriptor;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta_tests.XTemporaryFolder;
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
public class TestAuthCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    @Test
    public void testSuccessfulAuthOnArchive() throws Exception
    {
        String password = DatatypeConverter.printHexBinary(RandomMaker.get(64));
        PasswordProvider prov = new PasswordProvider(password.getBytes());
        File archiveFile = folder.newFilePath();
        ArchiveBuilder.createNewEmptyArchive(
                archiveFile,
                new Descriptor(EncryptionDescriptor.makeDefaults(), CompressionDescriptor.makeDefaults()),
                prov
        );

        File pwFile = PasswordFile.genPasswordFile(folder.newFilePath(), prov.getArchivePassword());

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, pwFile);
        new AuthCommand().handle(new Namespace(args));
    }


    @Test
    public void testFailedAuthOnArchive() throws Exception
    {
        String password = DatatypeConverter.printHexBinary(RandomMaker.get(64));
        PasswordProvider prov = new PasswordProvider(password.getBytes());
        File archiveFile = folder.newFilePath();
        ArchiveBuilder.createNewEmptyArchive(
                archiveFile,
                new Descriptor(EncryptionDescriptor.makeDefaults(), CompressionDescriptor.makeDefaults()),
                prov
        );

        File pwFile = PasswordFile.genPasswordFile(folder.newFilePath(), "this is definitely not the password".getBytes());

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, pwFile);
        try
        {
            new AuthCommand().handle(new Namespace(args));
            fail("Should have raised an exception");
        }
        catch(CLIException e)
        {
            assertThat(e.getMessage(), is(equalTo("Decryption failed: pad block corrupted")));
        }
    }


    @Test
    public void testSuccessfulAuthOnArchiveWithNoPassword() throws Exception
    {
        File archiveFile = folder.newFilePath();
        ArchiveBuilder.createNewEmptyArchive(
                archiveFile,
                new Descriptor(null, CompressionDescriptor.makeDefaults()),
                new PasswordProvider()
        );

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        new AuthCommand().handle(new Namespace(args));
    }
}
