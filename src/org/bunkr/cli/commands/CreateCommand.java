package org.bunkr.cli.commands;

import org.bunkr.core.ArchiveBuilder;
import org.bunkr.cli.CLI;
import org.bunkr.usersec.UserSecurityProvider;
import org.bunkr.descriptor.PBKDF2Descriptor;
import org.bunkr.descriptor.PlaintextDescriptor;
import org.bunkr.exceptions.CLIException;
import org.bunkr.descriptor.IDescriptor;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.File;

/**
 * Creator: benmeier
 * Created At: 2015-12-03
 */
public class CreateCommand implements ICLICommand
{
    public static final String ARG_OVERWRITE = "overwrite";
    public static final String ARG_NOENCRYPTION = "noencryption";
    public static final String ARG_NOCOMPRESSION = "nodecryption";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("create a new empty archive");
        target.addArgument("-o", "--overwrite")
                .dest(ARG_OVERWRITE)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("overwrite the existing file");
        target.addArgument("--no-encryption")
                .dest(ARG_NOENCRYPTION)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("the archive will not require a password");
        target.addArgument("--no-compression")
                .dest(ARG_NOCOMPRESSION)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("the archive will not use compression on its files");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makePasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));

        File archiveFile = args.get(CLI.ARG_ARCHIVE_PATH);
        if (archiveFile.exists() && !args.getBoolean(ARG_OVERWRITE))
            throw new CLIException("File %s already exists. Pass --overwrite in order to overwrite it.", archiveFile.getAbsolutePath());

        IDescriptor descriptor = new PlaintextDescriptor();
        if (!args.getBoolean(ARG_NOENCRYPTION))
        {
            descriptor = PBKDF2Descriptor.makeDefaults();
        }

        ArchiveBuilder.createNewEmptyArchive(archiveFile, descriptor, usp, !args.getBoolean(ARG_NOCOMPRESSION));
        System.out.println(String.format("Created new archive %s", archiveFile.getAbsolutePath()));
    }
}
