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

import org.bunkr.cli.ProgressBar;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.BlockAllocationManager;
import org.bunkr.core.MetadataWriter;
import org.bunkr.cli.CLI;
import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.operations.WipeBlocksOp;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.exceptions.TraversalException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.core.inventory.*;

/**
 * Created At: 2015-12-06
 */
public class RmCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_RECURSIVE = "recursive";
    public static final String ARG_NOWIPE = "nowipe";
    public static final String ARG_NOPROGRESS = "noprogress";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("remove a file or directory");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("path of directory to remove from the archive");
        target.addArgument("-r", "--recursive")
                .dest(ARG_RECURSIVE)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("remove all subfolders and files");
        target.addArgument("-n", "--no-wipe")
                .dest(ARG_NOWIPE)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("dont wipe deleted files blocks");
        target.addArgument("-q", "--no-progress")
                .dest(ARG_NOPROGRESS)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("dont display progress bar if wiping blocks");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);

        // first delete the correct items and collect blocks to wipe
        FragmentedRange wipeblocks = deleteItem(aic.getInventory(), args.getString(ARG_PATH), args.getBoolean(ARG_RECURSIVE));

        // save metadata as soon as possible
        MetadataWriter.write(aic, usp);
        System.out.println(String.format("Deleted %s from archive.", args.getString(ARG_PATH)));

        // now calculate which blocks we can still safely wipe (the descriptor and inventory may have landed over some)
        long usedBlocks = BlockAllocationManager.calculateUsedBlocks(aic.getInventory());
        wipeblocks.subtract(new FragmentedRange((int) usedBlocks, Integer.MAX_VALUE));

        // now attempt wipe of those blocks if required
        if (!args.getBoolean(ARG_NOWIPE) && !wipeblocks.isEmpty())
        {
            WipeBlocksOp op = new WipeBlocksOp(aic.filePath, aic.getBlockSize(), wipeblocks, true);
            ProgressBar pb = new ProgressBar(120, op.getTotalBlocks(), "Wiping file blocks: ");
            pb.setEnabled(!args.getBoolean(ARG_NOPROGRESS));
            pb.startFresh();
            op.setProgressUpdate(o -> pb.inc(1));
            op.run();
            pb.finish();
            System.out.println(String.format("Wiped %d blocked clean.", op.getBlocksWiped()));
        }
    }

    public FragmentedRange deleteItem(Inventory inv, String targetPath, boolean recursive) throws TraversalException
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
            FragmentedRange wipeBlocks = new FragmentedRange();

            boolean hasContents = (folderItem.getFiles().size() > 0 || folderItem.getFolders().size() > 0);
            if (hasContents)
            {
                if (recursive)
                {
                    collectFileBlocks(folderItem, wipeBlocks);
                }
                else
                {
                    throw new TraversalException("Folder '%s' is not empty", targetPath);
                }
            }
            parentContainer.removeFolder(folderItem);
            return wipeBlocks;
        }
        FileInventoryItem fileItem = parentContainer.findFile(targetName);
        if (fileItem != null)
        {
            parentContainer.removeFile(fileItem);
            return fileItem.getBlocks();
        }

        throw new TraversalException("'%s' does not exist", targetPath);
    }

    protected void collectFileBlocks(FolderInventoryItem folder, FragmentedRange target)
    {
        for (FileInventoryItem item : folder.getFiles())
        {
            target.union(item.getBlocks());
        }
        for (FolderInventoryItem item : folder.getFolders())
        {
            collectFileBlocks(item, target);
        }
    }
}
