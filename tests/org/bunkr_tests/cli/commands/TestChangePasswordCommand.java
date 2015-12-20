package org.bunkr_tests.cli.commands;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.UserSecurityProvider;
import org.bunkr.descriptor.PBKDF2Descriptor;
import org.bunkr.exceptions.BaseBunkrException;
import org.bunkr.utils.RandomMaker;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.ChangePasswordCommand;
import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr_tests.XTemporaryFolder;
import org.bunkr_tests.cli.PasswordFile;
import org.junit.Rule;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;

/**
 * Creator: benmeier
 * Created At: 2015-12-16
 */
public class TestChangePasswordCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    @Test
    public void testBuildParser()
    {
        new ChangePasswordCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    @Test
    public void testSuccessfulPasswordChange() throws Exception
    {

        String originalPassword = DatatypeConverter.printHexBinary(RandomMaker.get(64));
        File originalPasswordFile = PasswordFile.genPasswordFile(folder.newFilePath(), originalPassword.getBytes());
        PasswordProvider originalPasswordProv = new PasswordProvider();
        originalPasswordProv.setArchivePassword(originalPasswordFile);
        UserSecurityProvider originalUSP = new UserSecurityProvider(originalPasswordProv);

        File archiveFile = folder.newFilePath();

        ArchiveBuilder.createNewEmptyArchive(
                archiveFile,
                new PBKDF2Descriptor(256, 10000, RandomMaker.get(128)),
                originalUSP,
                true
        );

        {
            new ArchiveInfoContext(archiveFile, originalUSP);
        }

        String newPassword = DatatypeConverter.printHexBinary(RandomMaker.get(64));
        File newPassowrdFile = PasswordFile.genPasswordFile(folder.newFilePath(), newPassword.getBytes());
        PasswordProvider newPasswordProv = new PasswordProvider();
        newPasswordProv.setArchivePassword(newPassowrdFile);
        UserSecurityProvider newUSP = new UserSecurityProvider(newPasswordProv);

        try
        {
            new ArchiveInfoContext(archiveFile, newUSP);
            fail("should fail to decrypt");
        }
        catch (BaseBunkrException ignored) {}


        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, originalPasswordFile);
        args.put(ChangePasswordCommand.ARG_NEW_PASSWORD_FILE, newPassowrdFile);

        new ChangePasswordCommand().handle(new Namespace(args));

        {
            new ArchiveInfoContext(archiveFile, newUSP);
        }

        try
        {
            new ArchiveInfoContext(archiveFile, originalUSP);
            fail("should fail to decrypt");
        }
        catch (BaseBunkrException ignored) {}

    }
}
