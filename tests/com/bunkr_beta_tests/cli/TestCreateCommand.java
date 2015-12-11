package com.bunkr_beta_tests.cli;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.cli.commands.NewArchiveCommand;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta_tests.XTemporaryFolder;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Creator: benmeier
 * Created At: 2015-12-11
 */
public class TestCreateCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

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
}
