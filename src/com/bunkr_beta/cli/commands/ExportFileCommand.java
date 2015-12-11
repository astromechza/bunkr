package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.IFFTraversalTarget;
import com.bunkr_beta.inventory.InventoryPather;
import com.bunkr_beta.streams.input.MultilayeredInputStream;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.*;

/**
 * Creator: benmeier
 * Created At: 2015-12-08
 */
public class ExportFileCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_DESTINATION_FILE = "destination";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("read or export a file from the archive");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("source path in the archive");
        target.addArgument("destination")
                .dest(ARG_DESTINATION_FILE)
                .type(Arguments.fileType().acceptSystemIn())
                .help("file to export to or - for stdout");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        PasswordProvider passProv = makePasswordProvider(args);
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), passProv);
        IFFTraversalTarget target = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_PATH));
        if (!target.isAFile()) throw new CLIException("'%s' is not a file.", args.getString(ARG_PATH));

        FileInventoryItem targetFile = (FileInventoryItem) target;

        File inputFile = args.get(ARG_DESTINATION_FILE);
        if (inputFile.getPath().equals("-"))
        {
            writeBlockFileToStream(aic, targetFile, System.out);
        }
        else
        {
            if (inputFile.exists()) throw new CLIException("'%s' already exists. Will not overwrite.", inputFile.getCanonicalPath());
            try(OutputStream contentOutputStream = new FileOutputStream(inputFile))
            {
                writeBlockFileToStream(aic, targetFile, contentOutputStream);
            }
        }
    }

    private void writeBlockFileToStream(ArchiveInfoContext ctxt, FileInventoryItem targetFile, OutputStream os)
            throws IOException
    {
        try (MultilayeredInputStream ms = new MultilayeredInputStream(ctxt, targetFile))
        {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = ms.read(buffer)) != -1)
            {
                os.write(buffer, 0, n);
            }
        }
    }
}
