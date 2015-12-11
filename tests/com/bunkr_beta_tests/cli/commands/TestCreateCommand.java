package com.bunkr_beta_tests.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.cli.commands.NewArchiveCommand;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta_tests.XTemporaryFolder;
import com.bunkr_beta_tests.cli.PasswordFile;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

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
        new NewArchiveCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    @Test
    public void createNewArchive() throws Exception
    {
        File pwFile = PasswordFile.genPasswordFile(folder.newFilePath());
        File archiveFile = folder.newFilePath();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, pwFile);
        args.put(NewArchiveCommand.ARG_OVERWRITE, false);
        new NewArchiveCommand().handle(new Namespace(args));

        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword(pwFile);
        new ArchiveInfoContext(archiveFile, prov);
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
        args.put(NewArchiveCommand.ARG_OVERWRITE, false);
        try
        {
            new NewArchiveCommand().handle(new Namespace(args));
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
        args.put(NewArchiveCommand.ARG_OVERWRITE, true);
        new NewArchiveCommand().handle(new Namespace(args));

        PasswordProvider prov = new PasswordProvider();
        prov.setArchivePassword(pwFile);
        new ArchiveInfoContext(archiveFile, prov);
    }
}
