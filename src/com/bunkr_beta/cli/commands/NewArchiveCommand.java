package com.bunkr_beta.cli.commands;

import com.bunkr_beta.*;
import com.bunkr_beta.cli.CLIPasswordPrompt;
import com.bunkr_beta.descriptor.Descriptor;
import com.bunkr_beta.interfaces.ICLICommand;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    public void handle(Namespace args) throws IOException
    {
        File archiveFile = new File(args.getString("archive"));
        if (archiveFile.exists() && !args.getBoolean("overwrite"))
            throw new IOException(String.format("File %s already exists. Pass --overwrite in order to overwrite it.", archiveFile.getAbsolutePath()));

        PasswordProvider passProv = new PasswordProvider(new CLIPasswordPrompt());
        if (args.getString("password-file") != null)
        {
            try(BufferedReader br = new BufferedReader(new FileReader(args.getString("password-file"))))
            {
                passProv.setArchivePassword(br.readLine().getBytes());
            }
        }

        try
        {
            ArchiveBuilder.createNewEmptyArchive(archiveFile, Descriptor.makeDefaults(), passProv);
        }
        catch (CryptoException e)
        {
            e.printStackTrace();
            throw new IOException("Failure to create new archive");
        }
    }
}
