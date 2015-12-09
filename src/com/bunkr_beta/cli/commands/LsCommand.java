package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.cli.Formatters;
import com.bunkr_beta.cli.TabularLayout;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.exceptions.IllegalPathException;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

import java.util.Collections;
import java.util.List;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class LsCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_NOHEADINGS = "noheadings";
    public static final String ARG_MACHINEREADABLE = "machinereadable";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("list the contents of a folder");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("directory path to list");
        target.addArgument("-H", "--no-headings")
                .dest(ARG_NOHEADINGS)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("disable headings in the output");
        target.addArgument("-M", "--machine-readable")
                .dest(ARG_MACHINEREADABLE)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("format data in machine readable form");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        try
        {
            PasswordProvider passProv = makePasswordProvider(args);
            ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), passProv);
            IFFTraversalTarget t = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_PATH));

            TabularLayout table = new TabularLayout();
            if (! args.getBoolean(ARG_NOHEADINGS)) table.setHeaders("SIZE", "MODIFIED", "NAME");

            if (t.isAFile())
            {
                FileInventoryItem file = (FileInventoryItem) t;
                addFileRow(file, table, args.getBoolean(ARG_MACHINEREADABLE));
            }
            else
            {
                IFFContainer c = (IFFContainer) t;
                List<FolderInventoryItem> folders = c.getFolders();
                Collections.sort(folders);
                for (FolderInventoryItem folder : folders)
                {
                    table.addRow("", "", folder.getName() + "/");
                }
                List<FileInventoryItem> files = c.getFiles();
                Collections.sort(files);
                for (FileInventoryItem file : files)
                {
                    addFileRow(file, table, args.getBoolean(ARG_MACHINEREADABLE));
                }
            }
            table.printOut();
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

    private void addFileRow(FileInventoryItem file, TabularLayout table, boolean machinereadable)
    {
        String sizeCell;
        if (machinereadable)
            sizeCell = "" + file.getActualSize();
        else
            sizeCell = Formatters.formatBytes(file.getActualSize());
        String dateCell;
        if (machinereadable)
            dateCell = Formatters.formatIso8601utc(file.getModifiedAt());
        else
            dateCell = Formatters.formatPrettyDate(file.getModifiedAt());
        table.addRow(sizeCell, dateCell, file.getName());
    }
}
