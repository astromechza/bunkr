package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.cli.CLIPasswordPrompt;
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
public class MkdirCommand implements ICLICommand
{
    @Override
    public void buildParser(Subparser target)
    {
        target.addArgument("path")
                .type(String.class)
                .help("archive path to create the new directory");
        target.addArgument("-r", "--recursive")
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("build directories revursively");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        try
        {
            PasswordProvider passProv = makePasswordProvider(args);
            ArchiveInfoContext aic = new ArchiveInfoContext(args.get("archive"), passProv);
            mkdirs(aic.getInventory(), args.getString("path"), args.getBoolean("recursive"));
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
