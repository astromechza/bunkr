package org.bunkr.cli.commands;

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.cli.CLI;
import org.bunkr.usersec.UserSecurityProvider;
import org.bunkr.exceptions.CLIException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.inventory.*;

import java.util.*;

/**
 * Creator: benmeier
 * Created At: 2015-12-12
 */
public class FindCommand implements ICLICommand
{
    public static final String ARG_TYPE_FOLDER = "folder";
    public static final String ARG_TYPE_FILE = "file";
    public static final String ARG_PATH = "path";
    public static final String ARG_PREFIX = "prefix";
    public static final String ARG_SUFFIX = "suffix";
    public static final String ARG_TYPE = "type";
    public static final String ARG_TAG = "tag";
    public static final String ARG_DEPTH = "depth";


    @Override
    public void buildParser(Subparser target)
    {
        target.help("search for files or folders by name or tags");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("path to search in");
        target.addArgument("--prefix")
                .dest(ARG_PREFIX)
                .type(String.class)
                .help("select only items with a name beginning with this");
        target.addArgument("--suffix")
                .dest(ARG_SUFFIX)
                .type(String.class)
                .help("select only items with a name ending with this");
        target.addArgument("--type")
                .dest(ARG_TYPE)
                .choices(ARG_TYPE_FILE, ARG_TYPE_FOLDER)
                .type(String.class)
                .help("select only items of this type");
        target.addArgument("--tag")
                .dest(ARG_TAG)
                .type(String.class)
                .help("select only items that have the following tag");
        target.addArgument("--depth")
                .dest(ARG_DEPTH)
                .type(Integer.class)
                .help("recurse into subfolders this many times");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makePasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);
        IFFTraversalTarget t = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_PATH));

        if (t.isAFile())
        {
            throw new CLIException("'path' target must be a folder, not a file.");
        }
        else
        {
            IFFContainer c = (IFFContainer) t;
            int maxDepth = Integer.MAX_VALUE;
            if (args.get(ARG_DEPTH) != null) maxDepth = args.getInt(ARG_DEPTH);
            printBreadthFirstFindTree(c, args.get(ARG_PATH), args.get(ARG_PREFIX),
                                      args.get(ARG_SUFFIX),
                                      args.get(ARG_TYPE), args.get(ARG_TAG), maxDepth);
        }
    }

    private void printBreadthFirstFindTree(IFFContainer root, String pathPrefix, String prefix, String suffix, String type, String tag, Integer depth)
    {
        List<InventoryItem> itemsToSort = new ArrayList<>();
        if (type == null || type.equals(ARG_TYPE_FILE))
        {
            for (FileInventoryItem item : root.getFiles())
            {
                if ((prefix == null || item.getName().startsWith(prefix)) && (suffix == null || item.getName().endsWith(suffix)) && (tag == null || item.hasTag(tag)))
                {
                    itemsToSort.add(item);
                }
            }
        }
        itemsToSort.addAll(root.getFolders());
        Collections.sort(itemsToSort);

        for (InventoryItem i : itemsToSort)
        {
            if (i instanceof FileInventoryItem)
            {
                System.out.println(InventoryPather.simpleJoin(pathPrefix, i.getName()));
            }
            else
            {
                if ((tag == null) && (type == null || type.equals(ARG_TYPE_FOLDER)) && (prefix == null || i.getName().startsWith(prefix)) && (suffix == null || i.getName().endsWith(suffix)))
                {
                    System.out.println(InventoryPather.simpleJoin(pathPrefix, i.getName()) + "/");
                }
                if (depth > 0) printBreadthFirstFindTree((FolderInventoryItem) i, InventoryPather.simpleJoin(pathPrefix, i.getName()), prefix, suffix, type, tag, depth - 1);
            }
        }
    }
}
