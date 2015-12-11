package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.exceptions.IllegalPathException;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.IFFContainer;
import com.bunkr_beta.inventory.IFFTraversalTarget;
import com.bunkr_beta.inventory.InventoryPather;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Creator: benmeier
 * Created At: 2015-12-10
 */
public class TagCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_TAGS = "tags";
    public static final String ARG_CLEAR = "clear";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("list, set or add tags to a file");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("target path in the archive");
        target.addArgument("-t", "--tags")
                .dest(ARG_TAGS)
                .type(String.class)
                .nargs("*")
                .setDefault(new ArrayList<>())
                .help("a list of tags to associate with this file");
        target.addArgument("-c", "--clear")
                .dest(ARG_CLEAR)
                .type(Boolean.class)
                .setDefault(false)
                .action(Arguments.storeTrue())
                .help("remove all tags associated with the file");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        // first do some arg checking
        if (args.getBoolean(ARG_CLEAR) && args.getList(ARG_TAGS).size() > 0)
        {
            throw new CLIException("Please use either --tags or --clear, not both.");
        }

        try
        {
            PasswordProvider passProv = makePasswordProvider(args);
            ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), passProv);
            IFFTraversalTarget parent = InventoryPather
                    .traverse(aic.getInventory(), InventoryPather.dirname(args.getString(ARG_PATH)));
            if (parent.isAFile()) throw new CLIException("'%s' is a file.", InventoryPather.dirname(args.getString(ARG_PATH)));

            IFFTraversalTarget target = ((IFFContainer) parent).findFileOrFolder(InventoryPather.baseName(args.getString(ARG_PATH)));
            if (target == null) throw new CLIException("No such file '%s'.", args.getString(ARG_PATH));
            if (target.isAFolder()) throw new CLIException("'%s' is a folder.", args.getString(ARG_PATH));

            FileInventoryItem targetFile = (FileInventoryItem) target;

            if (args.getBoolean(ARG_CLEAR))
            {
                targetFile.setTags(new HashSet<>());
                MetadataWriter.write(aic, passProv);
                System.out.println(String.format("Cleared tags on file %s", args.getString(ARG_PATH)));
            }
            else if (args.getList(ARG_TAGS).size() > 0)
            {
                try
                {
                    targetFile.setCheckTags(new HashSet<>(args.getList(ARG_TAGS)));
                    MetadataWriter.write(aic, passProv);
                    System.out.println(String.format("Set %d tags on file %s", args.getList(ARG_TAGS).size(),
                                                     args.getString(ARG_PATH)));
                }
                catch (IllegalArgumentException e)
                {
                    throw new CLIException(e.getMessage());
                }
            }
            else
            {
                for (String s : targetFile.getTags())
                {
                    System.out.println(s);
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
