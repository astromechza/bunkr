package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.exceptions.IllegalPathException;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.*;
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

    @Override
    public void buildParser(Subparser target)
    {
        target.help("list the contents of a folder");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("directory path to list");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        try
        {
            PasswordProvider passProv = makePasswordProvider(args);
            ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), passProv);
            IFFTraversalTarget t = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_PATH));

            if (t.isAFile())
            {
                System.out.println(((FileInventoryItem)t).getName());
            }
            else
            {
                IFFContainer c = (IFFContainer) t;
                List<FolderInventoryItem> folders = c.getFolders();
                Collections.sort(folders);
                for (FolderInventoryItem folder : folders)
                {
                    System.out.println(folder.getName() + "/");
                }
                List<FileInventoryItem> files = c.getFiles();
                Collections.sort(files);
                for (FileInventoryItem file : files)
                {
                    System.out.println(file.getName());
                }
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
