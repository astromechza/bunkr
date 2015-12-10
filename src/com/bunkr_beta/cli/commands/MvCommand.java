package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.exceptions.IllegalPathException;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.*;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

/**
 * Creator: benmeier
 * Created At: 2015-12-11
 *
 * A cli command for moving items.
 * eg:
 * mv /something /another
 *
 */
public class MvCommand implements ICLICommand
{
    public static final String ARG_FROMPATH = "fromPath";
    public static final String ARG_TOPATH = "toPath";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("move a file or folder to a different path");
        target.addArgument("from-path")
                .dest(ARG_FROMPATH)
                .type(String.class)
                .help("the original path of the item to move");
        target.addArgument("to-path")
                .dest(ARG_TOPATH)
                .type(String.class)
                .help("the new path of the item being moved");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        try
        {
            String fromPathParent = InventoryPather.dirname(args.getString(ARG_FROMPATH));
            String toPathParent = InventoryPather.dirname(args.getString(ARG_TOPATH));
            String toPathName = InventoryPather.baseName(args.getString(ARG_TOPATH));

            PasswordProvider passProv = makePasswordProvider(args);
            ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), passProv);

            IFFTraversalTarget targetItem = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_FROMPATH));

            IFFTraversalTarget fromParent = InventoryPather.traverse(aic.getInventory(), fromPathParent);
            IFFContainer fromContainer = (IFFContainer) fromParent;

            IFFTraversalTarget toParent = InventoryPather.traverse(aic.getInventory(), toPathParent);
            if (toParent.isAFile()) throw new CLIException("Cannot move item to be a child of a file.");
            IFFContainer toContainer = (IFFContainer) toParent;
            if (toContainer.hasFile(toPathName)) throw new CLIException("Destination folder already contains a file '%s'", toPathName);
            if (toContainer.hasFolder(toPathName)) throw new CLIException("Destination folder already contains a folder '%s'", toPathName);

            if (targetItem.isAFile())
            {
                FileInventoryItem targetFile = (FileInventoryItem) targetItem;
                targetFile.setName(toPathName);
                fromContainer.getFiles().remove(targetFile);
                toContainer.getFiles().add(targetFile);
                MetadataWriter.write(aic, passProv);
                System.out.println(String.format("Moved file '%s' to '%s'", args.getString(ARG_FROMPATH), args.getString(ARG_TOPATH)));
            }
            else
            {
                FolderInventoryItem targetFolder = (FolderInventoryItem) targetItem;
                targetFolder.setName(toPathName);
                fromContainer.getFolders().remove(targetFolder);
                toContainer.getFolders().add(targetFolder);
                MetadataWriter.write(aic, passProv);
                System.out.println(String.format("Moved folder '%s' to '%s'", args.getString(ARG_FROMPATH), args.getString(ARG_TOPATH)));
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

        // identify source item

        // check destination - fail if it already exists - fail if the parent is a file

        // if folder
            // move it
        // if file
            // move it


    }
}
