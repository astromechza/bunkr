package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.cli.CLIPasswordPrompt;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.exceptions.IllegalPathException;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.*;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class LsCommand implements ICLICommand
{
    @Override
    public void buildParser(Subparser target)
    {
        target.addArgument("path")
                .type(String.class)
                .help("directory path to list");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        String targetPath = args.getString("path");

        File archiveFile = new File(args.getString("archive"));
        PasswordProvider passProv = new PasswordProvider(new CLIPasswordPrompt());
        if (args.getString("password-file") != null)
        {
            passProv.setArchivePassword(new File(args.getString("password-file")));
        }

        try
        {
            ArchiveInfoContext aic = new ArchiveInfoContext(archiveFile, passProv);
            IFFTraversalTarget t = InventoryPather.traverse(aic.getInventory(), targetPath);

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
