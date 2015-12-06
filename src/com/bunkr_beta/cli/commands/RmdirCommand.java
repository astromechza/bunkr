package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.cli.CLIPasswordPrompt;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.exceptions.IllegalPathException;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

import java.io.File;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class RmdirCommand implements ICLICommand
{
    @Override
    public void buildParser(Subparser target)
    {
        target.addArgument("path")
                .type(String.class)
                .help("archive path to create the new directory");
        target.addArgument("-r", "--revursive")
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("remove all subfolders and files");
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
            rmdir(aic.getInventory(), args.getString("path"), args.getBoolean("recursive"));
            MetadataWriter.write(aic, passProv);
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

    public void rmdir(Inventory inv, String targetPath, boolean recursive) throws TraversalException
    {
        if (targetPath.equals("/")) throw new TraversalException("Cannot remove root directory");

        String parentPath = InventoryPather.dirname(targetPath);

        IFFTraversalTarget parentDir = InventoryPather.traverse(inv, parentPath);

        String targetName = InventoryPather.baseName(targetPath);

        if (parentDir.isAFile()) throw new TraversalException("'%s' is a file and does not contain directory '%s'", parentPath, targetName);

        IFFContainer parentContainer = (IFFContainer) parentDir;

        for (FolderInventoryItem item : parentContainer.getFolders())
        {
            if (targetName.equals(item.getName()))
            {
                if (!recursive && (item.getFiles().size() > 0 || item.getFolders().size() > 0)) throw new TraversalException("Folder '%s' is not empty", targetPath);
                parentContainer.getFolders().remove(item);
                return;
            }
        }
        throw new TraversalException("Folder '%s' does not exist", targetPath);
    }
}
