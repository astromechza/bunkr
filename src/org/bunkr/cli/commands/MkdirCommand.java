package org.bunkr.cli.commands;

import org.bunkr.ArchiveInfoContext;
import org.bunkr.MetadataWriter;
import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.cli.CLI;
import org.bunkr.exceptions.TraversalException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.inventory.*;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class MkdirCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_RECURSIVE = "recursive";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("construct a directory");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("archive path to create the new directory");
        target.addArgument("-r", "--recursive")
                .dest(ARG_RECURSIVE)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("build directories revursively");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        PasswordProvider passProv = makePasswordProvider(args.get(CLI.ARG_PASSWORD_FILE));
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), passProv);
        mkdirs(aic.getInventory(), args.getString(ARG_PATH), args.getBoolean(ARG_RECURSIVE));
        MetadataWriter.write(aic, passProv);
    }

    public void mkdirs(Inventory inv, String targetPath, boolean recursive) throws TraversalException
    {
        if (recursive)
        {
            String currentPath = "/";
            IFFContainer current = inv;
            for (String part : InventoryPather.getParts(targetPath))
            {
                // if current has folder part, current = current / part
                boolean hasTheFolder = false;
                for (FolderInventoryItem item : current.getFolders())
                {
                    if (item.getName().equals(part))
                    {
                        current = item;
                        hasTheFolder = true;
                        break;
                    }
                }

                // little optimisation
                currentPath = InventoryPather.simpleJoin(currentPath, part);

                if (!hasTheFolder)
                {
                    // otherwise if folder contains a file part, throw exception
                    if (current.hasFile(part))
                        throw new TraversalException("'%s' is a file", currentPath);

                    // otherwise if it doesnt, create a new folder and current = current / part
                    FolderInventoryItem f = new FolderInventoryItem(part);
                    current.getFolders().add(f);
                    current = f;
                }
            }
        }
        else
        {
            IFFTraversalTarget parent = InventoryPather.traverse(inv, InventoryPather.dirname(targetPath));
            if (parent.isAFile())
                throw new TraversalException("Cannot create a directory as a child of file '%s'.", ((FileInventoryItem) parent).getName());
            ((IFFContainer) parent).getFolders().add(new FolderInventoryItem(InventoryPather.baseName(targetPath)));
        }
    }
}
