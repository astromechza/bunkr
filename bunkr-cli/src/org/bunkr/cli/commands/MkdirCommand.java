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
import org.bunkr.core.MetadataWriter;
import org.bunkr.cli.CLI;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.exceptions.TraversalException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.core.inventory.*;

/**
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
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);
        mkdirs(aic.getInventory(), args.getString(ARG_PATH), args.getBoolean(ARG_RECURSIVE));
        MetadataWriter.write(aic, usp);
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
                    current.addFolder(f);
                    current = f;
                }
            }
        }
        else
        {
            IFFTraversalTarget parent = InventoryPather.traverse(inv, InventoryPather.dirname(targetPath));
            if (parent.isAFile())
                throw new TraversalException("Cannot create a directory as a child of file '%s'.", ((FileInventoryItem) parent).getName());
            ((IFFContainer) parent).addFolder(new FolderInventoryItem(InventoryPather.baseName(targetPath)));
        }
    }
}
