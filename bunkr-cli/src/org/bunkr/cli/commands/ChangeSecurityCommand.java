package org.bunkr.cli.commands;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.bunkr.cli.CLI;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.descriptor.PBKDF2Descriptor;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.descriptor.ScryptDescriptor;
import org.bunkr.core.inventory.Algorithms;
import org.bunkr.core.usersec.UserSecurityProvider;

import java.io.File;

/**
 * Creator: benmeier
 * Created At: 2016-01-10
 */
public class ChangeSecurityCommand implements ICLICommand
{
    public static final String ARG_ARCHIVE_SECURITY = "algorithm";
    public static final String ARG_NEW_PASSWORD_FILE = "newpasswordfile";
    public static final String ARG_FILE_SECURITY = "filesecurity";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("change the security settings for the archive");

        Subparsers securityType = target.addSubparsers().dest(ARG_ARCHIVE_SECURITY);
        securityType.addParser(PlaintextDescriptor.IDENTIFIER.toLowerCase());

        Subparser p1 = securityType.addParser(PBKDF2Descriptor.IDENTIFIER.toLowerCase());
        p1.addArgument("new-password-file")
                .dest(ARG_NEW_PASSWORD_FILE)
                .type(Arguments.fileType().verifyExists().verifyCanRead().acceptSystemIn())
                .help("read the new archive password from the given file or '-' for stdin");
        p1.addArgument("file-security")
                .dest(ARG_FILE_SECURITY)
                .choices(Algorithms.Encryption.AES256_CTR.toString().toLowerCase())
                .help("set the encryption used on files");

        Subparser p2 = securityType.addParser(ScryptDescriptor.IDENTIFIER.toLowerCase());
        p2.addArgument(ARG_NEW_PASSWORD_FILE)
                .type(Arguments.fileType().verifyExists().verifyCanRead().acceptSystemIn())
                .help("read the new archive password from the given file or '-' for stdin");
        p2.addArgument("file-security")
                .dest(ARG_FILE_SECURITY)
                .choices(Algorithms.Encryption.AES256_CTR.toString().toLowerCase())
                .help("set the encryption used on files");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        // build security provider
        UserSecurityProvider usp = new UserSecurityProvider(
                makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE), "Enter current archive password:")
        );
        // open archive file
        ArchiveInfoContext context = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);

        String before = String.format("Archive Security: %s, File Security: %s",
                                      context.getDescriptor().getIdentifier(),
                                      context.getInventory().getDefaultEncryption());

        switch ((String) args.get(ARG_ARCHIVE_SECURITY))
        {
            case PlaintextDescriptor.IDENTIFIER:
                context.setDescriptor(new PlaintextDescriptor());
                context.getInventory().setDefaultEncryption(Algorithms.Encryption.NONE);
                break;
            case PBKDF2Descriptor.IDENTIFIER:
                context.setDescriptor(PBKDF2Descriptor.makeDefaults());
                usp.setProvider(makeCLIPasswordProvider(args.get(ARG_NEW_PASSWORD_FILE), "Enter new password for pbkdf2:"));
                context.getInventory().setDefaultEncryption(Algorithms.Encryption.AES256_CTR);
                break;
            case ScryptDescriptor.IDENTIFIER:
                context.setDescriptor(ScryptDescriptor.makeDefaults());
                usp.setProvider(makeCLIPasswordProvider(args.get(ARG_NEW_PASSWORD_FILE), "Enter new password for scrypt:"));
                context.getInventory().setDefaultEncryption(Algorithms.Encryption.AES256_CTR);
                break;
            default:
                return;
        }

        String after = String.format("Archive Security: %s, File Security: %s",
                                      context.getDescriptor().getIdentifier(),
                                      context.getInventory().getDefaultEncryption());

        MetadataWriter.write(context, usp);
        System.out.println("Successfully changed security settings for achive.");
        System.out.println(String.format("Before: %s", before));
        System.out.println(String.format("After: %s", after));
    }
}