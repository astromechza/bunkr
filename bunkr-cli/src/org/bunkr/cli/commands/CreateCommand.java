package org.bunkr.cli.commands;

import org.bunkr.core.ArchiveBuilder;
import org.bunkr.cli.CLI;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.exceptions.CLIException;
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

    @Override
    public void buildParser(Subparser target)
    {
        target.help("create a new empty archive");
        target.addArgument("-o", "--overwrite")
                .dest(ARG_OVERWRITE)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("overwrite the existing file");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));

        File archiveFile = args.get(CLI.ARG_ARCHIVE_PATH);
        if (archiveFile.exists() && !args.getBoolean(ARG_OVERWRITE))
            throw new CLIException("File %s already exists. Pass --overwrite in order to overwrite it.", archiveFile.getAbsolutePath());

        ArchiveBuilder.createNewEmptyArchive(archiveFile, new PlaintextDescriptor(), usp);
        System.out.println(String.format("Created new archive %s", archiveFile.getAbsolutePath()));
    }
}
