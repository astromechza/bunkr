package org.bunkr_tests.cli.commands;

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.CreateCommand;
import org.bunkr.usersec.PasswordProvider;
import org.bunkr.usersec.UserSecurityProvider;
import org.bunkr.exceptions.CLIException;
import org.bunkr_tests.XTemporaryFolder;
import org.bunkr_tests.cli.PasswordFile;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

/**
 * Creator: benmeier
 * Created At: 2015-12-11
 */
public class TestCreateCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    @Test
    public void testBuildParser()
    {
        new CreateCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    @Test
    public void createNewArchive() throws Exception
    {
        File pwFile = PasswordFile.genPasswordFile(folder.newFilePath());
        File archiveFile = folder.newFilePath();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, pwFile);
        args.put(CreateCommand.ARG_OVERWRITE, false);
        args.put(CreateCommand.ARG_NOCOMPRESSION, false);
        args.put(CreateCommand.ARG_NOENCRYPTION, false);
        new CreateCommand().handle(new Namespace(args));

        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword(pwFile);
        UserSecurityProvider usp = new UserSecurityProvider(prov);
        new ArchiveInfoContext(archiveFile, usp);
    }

    @Test
    public void createNewArchiveAlreadyExists() throws Exception
    {
        File pwFile = PasswordFile.genPasswordFile(folder.newFilePath());
        File archiveFile = folder.newFile();
        assertTrue(archiveFile.exists());

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, pwFile);
        args.put(CreateCommand.ARG_OVERWRITE, false);
        args.put(CreateCommand.ARG_NOCOMPRESSION, false);
        args.put(CreateCommand.ARG_NOENCRYPTION, false);
        try
        {
            new CreateCommand().handle(new Namespace(args));
            fail("should not overwrite the file");
        }
        catch (CLIException ignored) {}
    }


    @Test
    public void createNewArchiveAlreadyExistsOverride() throws Exception
    {
        File pwFile = PasswordFile.genPasswordFile(folder.newFilePath());
        File archiveFile = folder.newFile();
        assertTrue(archiveFile.exists());

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, pwFile);
        args.put(CreateCommand.ARG_OVERWRITE, true);
        args.put(CreateCommand.ARG_NOCOMPRESSION, false);
        args.put(CreateCommand.ARG_NOENCRYPTION, false);
        new CreateCommand().handle(new Namespace(args));

        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword(pwFile);
        UserSecurityProvider usp = new UserSecurityProvider(prov);
        new ArchiveInfoContext(archiveFile, usp);
    }

    @Test
    public void createNewArchiveWithoutEncryption() throws Exception
    {
        File pwFile = PasswordFile.genPasswordFile(folder.newFilePath());
        File archiveFile = folder.newFilePath();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, pwFile);
        args.put(CreateCommand.ARG_OVERWRITE, false);
        args.put(CreateCommand.ARG_NOCOMPRESSION, false);
        args.put(CreateCommand.ARG_NOENCRYPTION, true);
        new CreateCommand().handle(new Namespace(args));

        ArchiveInfoContext c = new ArchiveInfoContext(archiveFile, new UserSecurityProvider(new PasswordProvider()));
        assertFalse(c.getDescriptor().mustEncryptFiles());
    }

    @Test
    public void createNewArchiveWithoutCompression() throws Exception
    {
        File pwFile = PasswordFile.genPasswordFile(folder.newFilePath());
        File archiveFile = folder.newFilePath();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, pwFile);
        args.put(CreateCommand.ARG_OVERWRITE, false);
        args.put(CreateCommand.ARG_NOCOMPRESSION, true);
        args.put(CreateCommand.ARG_NOENCRYPTION, false);
        new CreateCommand().handle(new Namespace(args));

        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword(pwFile);
        UserSecurityProvider usp = new UserSecurityProvider(prov);
        ArchiveInfoContext c = new ArchiveInfoContext(archiveFile, usp);
        assertTrue(c.getDescriptor().mustEncryptFiles());
    }
}
