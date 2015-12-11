package com.bunkr_beta.cli.commands;

import com.bunkr_beta.*;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.descriptor.Descriptor;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.File;

/**
 * Creator: benmeier
 * Created At: 2015-12-03
 */
public class NewArchiveCommand implements ICLICommand
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
        PasswordProvider passProv = makePasswordProvider(args);
        File archiveFile = args.get(CLI.ARG_ARCHIVE_PATH);
        if (archiveFile.exists() && !args.getBoolean(ARG_OVERWRITE))
            throw new CLIException("File %s already exists. Pass --overwrite in order to overwrite it.", archiveFile.getAbsolutePath());
        ArchiveBuilder.createNewEmptyArchive(archiveFile, Descriptor.makeDefaults(), passProv);
    }
}
