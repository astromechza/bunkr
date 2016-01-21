/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.cli.commands;

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.cli.CLI;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.exceptions.CLIException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.core.inventory.*;

import java.util.*;
import java.util.stream.Collectors;

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
        target.addArgument("--depth")
                .dest(ARG_DEPTH)
                .type(Integer.class)
                .help("recurse into subfolders this many times");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
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
                                      args.get(ARG_TYPE), maxDepth);
        }
    }

    private void printBreadthFirstFindTree(IFFContainer root, String pathPrefix, String prefix, String suffix, String type, Integer depth)
    {
        List<InventoryItem> itemsToSort = new ArrayList<>();
        if (type == null || type.equals(ARG_TYPE_FILE))
        {
            itemsToSort.addAll(root.getFiles().stream()
               .filter(item -> (prefix == null || item.getName().startsWith(prefix)) && (suffix == null || item.getName().endsWith(suffix)))
               .collect(Collectors.toList()));
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
                if ((type == null || type.equals(ARG_TYPE_FOLDER)) && (prefix == null || i.getName().startsWith(prefix)) && (suffix == null || i.getName().endsWith(suffix)))
                {
                    System.out.println(InventoryPather.simpleJoin(pathPrefix, i.getName()) + "/");
                }
                if (depth > 0) printBreadthFirstFindTree((FolderInventoryItem) i, InventoryPather.simpleJoin(pathPrefix, i.getName()), prefix, suffix, type, depth - 1);
            }
        }
    }
}
