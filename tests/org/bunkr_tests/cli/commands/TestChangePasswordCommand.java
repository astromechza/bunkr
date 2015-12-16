package org.bunkr_tests.cli.commands;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.bouncycastle.crypto.CryptoException;
import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.utils.RandomMaker;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.ChangePasswordCommand;
import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.descriptor.CompressionDescriptor;
import org.bunkr.descriptor.Descriptor;
import org.bunkr.descriptor.EncryptionDescriptor;
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
        File originalPassowrdFile = PasswordFile.genPasswordFile(folder.newFilePath(), originalPassword.getBytes());
        PasswordProvider originalPasswordProv = new PasswordProvider();
        originalPasswordProv.setArchivePassword(originalPassowrdFile);

        File archiveFile = folder.newFilePath();

        ArchiveBuilder.createNewEmptyArchive(
                archiveFile,
                new Descriptor(EncryptionDescriptor.makeDefaults(), CompressionDescriptor.makeDefaults()),
                originalPasswordProv
        );

        {
            new ArchiveInfoContext(archiveFile, originalPasswordProv);
        }

        String newPassword = DatatypeConverter.printHexBinary(RandomMaker.get(64));
        File newPassowrdFile = PasswordFile.genPasswordFile(folder.newFilePath(), newPassword.getBytes());
        PasswordProvider newPasswordProv = new PasswordProvider();
        newPasswordProv.setArchivePassword(newPassowrdFile);

        try
        {
            new ArchiveInfoContext(archiveFile, newPasswordProv);
            fail("should fail to decrypt");
        }
        catch (CryptoException ignored) {}


        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(CLI.ARG_PASSWORD_FILE, originalPassowrdFile);
        args.put(ChangePasswordCommand.ARG_NEW_PASSWORD, newPassowrdFile);

        new ChangePasswordCommand().handle(new Namespace(args));


        {
            new ArchiveInfoContext(archiveFile, newPasswordProv);
        }

        try
        {
            new ArchiveInfoContext(archiveFile, originalPasswordProv);
            fail("should fail to decrypt");
        }
        catch (CryptoException ignored) {}

    }
}
