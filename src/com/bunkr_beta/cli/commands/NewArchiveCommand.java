package com.bunkr_beta.cli.commands;

import com.bunkr_beta.*;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.descriptor.Descriptor;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

import java.io.File;
import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-03
 */
public class NewArchiveCommand implements ICLICommand
{

    @Override
    public void buildParser(Subparser target)
    {
        target.help("create a new empty archive");
        target.addArgument("-o", "--overwrite")
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("overwrite the existing file");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        try
        {
            PasswordProvider passProv = makePasswordProvider(args);
            File archiveFile = args.get(CLI.ARG_ARCHIVE_PATH);
            if (archiveFile.exists() && !args.getBoolean("overwrite"))
                throw new CLIException("File %s already exists. Pass --overwrite in order to overwrite it.", archiveFile.getAbsolutePath());
            ArchiveBuilder.createNewEmptyArchive(archiveFile, Descriptor.makeDefaults(), passProv);
        }
        catch (CryptoException e)
        {
            e.printStackTrace();
            throw new IOException("Failure to create new archive");
        }
    }
}
