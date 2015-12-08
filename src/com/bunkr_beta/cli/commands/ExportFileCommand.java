package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.cli.CLIPasswordPrompt;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.exceptions.IllegalPathException;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.IFFTraversalTarget;
import com.bunkr_beta.inventory.InventoryPather;
import com.bunkr_beta.streams.input.MultilayeredInputStream;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

import java.io.*;

/**
 * Creator: benmeier
 * Created At: 2015-12-08
 */
public class ExportFileCommand implements ICLICommand
{
    @Override
    public void buildParser(Subparser target)
    {
        target.addArgument("path")
                .type(String.class)
                .help("source path in the archive");
        target.addArgument("destination")
                .type(Arguments.fileType().acceptSystemIn())
                .help("file to export to or - for stdout");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        File archiveFile = new File(args.getString("archive"));
        PasswordProvider passProv = new PasswordProvider(new CLIPasswordPrompt());
        if (args.getString("password-file") != null)
        {
            passProv.setArchivePassword(new File(args.getString("password-file")));
        }

        try
        {
            ArchiveInfoContext aic = new ArchiveInfoContext(archiveFile, passProv);
            IFFTraversalTarget target = InventoryPather.traverse(aic.getInventory(), args.getString("path"));
            if (!target.isAFile()) throw new CLIException("'%s' is not a file.", args.getString("path"));

            FileInventoryItem targetFile = (FileInventoryItem) target;

            OutputStream contentOutputStream;
            File inputFile = args.get("destination");
            if (inputFile.getPath().equals("-"))
                contentOutputStream = System.out;
            else
                contentOutputStream = new FileOutputStream(inputFile);

            try
            {
                try (MultilayeredInputStream ms = new MultilayeredInputStream(aic, targetFile))
                {
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = ms.read(buffer)) != -1)
                    {
                        contentOutputStream.write(buffer, 0, n);
                    }
                }
            }
            finally
            {
                contentOutputStream.close();
            }
        }
        catch (IllegalPathException | TraversalException e)
        {
            throw new CLIException(e);
        }
        catch (CryptoException e)
        {
            throw new CLIException("Decryption failed: %s", e.getMessage());
        }
    }
}
