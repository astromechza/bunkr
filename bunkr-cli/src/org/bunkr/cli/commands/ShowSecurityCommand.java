package org.bunkr.cli.commands;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.cli.CLI;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.descriptor.PBKDF2Descriptor;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.descriptor.ScryptDescriptor;
import org.bunkr.core.usersec.UserSecurityProvider;

/**
 * Creator: benmeier
 * Created At: 2016-01-21
 */
public class ShowSecurityCommand implements ICLICommand
{
    @Override
    public void buildParser(Subparser target)
    {
        target.help("show the security settings of the archive");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        ArchiveInfoContext archive = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);

        System.out.println(String.format("Archive Security: %s", archive.getDescriptor().getIdentifier()));
        switch (archive.getDescriptor().getIdentifier())
        {
            case PlaintextDescriptor.IDENTIFIER:
            {
                System.out.println("Inventory Encryption: None");
                break;
            }
            case PBKDF2Descriptor.IDENTIFIER:
            {
                PBKDF2Descriptor descriptor = (PBKDF2Descriptor) archive.getDescriptor();
                System.out.println(String.format("PBKDF2 Iterations: %s", descriptor.pbkdf2Iterations));
                System.out.println(String.format("PBKDF2 Salt Length: %s bytes", descriptor.pbkdf2Salt.length));
                System.out.println(String.format("Inventory Encryption: %s", descriptor.encryptionAlgorithm));
                break;
            }
            case ScryptDescriptor.IDENTIFIER:
            {
                ScryptDescriptor descriptor = (ScryptDescriptor) archive.getDescriptor();
                System.out.println(String.format("Scrypt Cost: %s", descriptor.scryptN));
                System.out.println(String.format("Scrypt Parallelization: %s", descriptor.scryptP));
                System.out.println(String.format("Scrypt Block Size: %s", descriptor.scryptR));
                System.out.println(String.format("Scrypt Salt Length: %s bytes", descriptor.scryptSalt.length));
                System.out.println(String.format("Inventory Encryption: %s", descriptor.encryptionAlgorithm));
                break;
            }
        }

        System.out.println(String.format("File Encryption: %s", archive.getInventory().getDefaultEncryption()));
    }
}
