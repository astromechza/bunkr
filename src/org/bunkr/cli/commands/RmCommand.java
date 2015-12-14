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
public class RmCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_RECURSIVE = "recursive";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("remove a file or directory");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("archive path to create the new directory");
        target.addArgument("-r", "--recursive")
                .dest(ARG_RECURSIVE)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("remove all subfolders and files");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        PasswordProvider passProv = makePasswordProvider(args);
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), passProv);
        deleteItem(aic.getInventory(), args.getString(ARG_PATH), args.getBoolean(ARG_RECURSIVE));
        MetadataWriter.write(aic, passProv);
        System.out.println(String.format("Deleted %s from archive.", args.getString(ARG_PATH)));
    }

    public void deleteItem(Inventory inv, String targetPath, boolean recursive) throws TraversalException
    {
        if (targetPath.equals("/")) throw new TraversalException("Cannot remove root directory");

        String parentPath = InventoryPather.dirname(targetPath);

        IFFTraversalTarget parentDir = InventoryPather.traverse(inv, parentPath);

        String targetName = InventoryPather.baseName(targetPath);

        if (parentDir.isAFile()) throw new TraversalException("'%s' is a file and does not contain item '%s'", parentPath, targetName);

        IFFContainer parentContainer = (IFFContainer) parentDir;

        FolderInventoryItem folderItem = (FolderInventoryItem) parentContainer.findFolder(targetName);
        if (folderItem != null)
        {
            if (!recursive && (folderItem.getFiles().size() > 0 || folderItem.getFolders().size() > 0)) throw new TraversalException("Folder '%s' is not empty", targetPath);
            parentContainer.getFolders().remove(folderItem);
            return;
        }
        FileInventoryItem fileItem = parentContainer.findFile(targetName);
        if (fileItem != null)
        {
            parentContainer.getFiles().remove(fileItem);
            return;
        }

        throw new TraversalException("'%s' does not exist", targetPath);
    }
}
