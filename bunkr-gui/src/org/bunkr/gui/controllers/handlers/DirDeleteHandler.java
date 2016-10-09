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

package org.bunkr.gui.controllers.handlers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.BlockAllocationManager;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.inventory.*;
import org.bunkr.core.operations.WipeBlocksOp;
import org.bunkr.gui.ProgressTask;
import org.bunkr.gui.components.treeview.InventoryTreeData;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.dialogs.ProgressDialog;
import org.bunkr.gui.dialogs.QuickDialogs;
import org.bunkr.gui.windows.MainWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created At: 2016-01-17
 */
public class DirDeleteHandler implements EventHandler<ActionEvent>
{
    private final ArchiveInfoContext archive;
    private final InventoryTreeView tree;
    private final MainWindow mainWindow;

    public DirDeleteHandler(ArchiveInfoContext archive, InventoryTreeView tree, MainWindow mainWindow)
    {
        this.archive = archive;
        this.tree = tree;
        this.mainWindow = mainWindow;
    }

    @Override
    public void handle(ActionEvent event)
    {
        try
        {
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItem();

            if (! QuickDialogs.confirm("Are you sure you want to delete '%s' and all of its children?", selected.getValue().getName())) return;

            // find parent item
            TreeItem<InventoryTreeData> parent = selected.getParent();
            String parentPath = this.tree.getPathForTreeItem(parent);

            // find inventory item
            IFFContainer parentContainer = (IFFContainer) InventoryPather.traverse(this.archive.getInventory(), parentPath);

            // just get inventory item
            IFFTraversalTarget target = parentContainer.findFileOrFolder(selected.getValue().getName());
            if (target instanceof FolderInventoryItem)
            {
                parent.getChildren().remove(selected);

                FolderInventoryItem targetFolder = (FolderInventoryItem) target;

                FragmentedRange wipeblocks = new FragmentedRange();
                List<FolderInventoryItem> queue = new ArrayList<>();
                queue.add(targetFolder);
                while (!queue.isEmpty())
                {
                    FolderInventoryItem c = queue.remove(0);
                    for (FileInventoryItem f : c.getFiles())
                    {
                        mainWindow.requestClose(f);
                        wipeblocks.union(f.getBlocks());
                    }
                    queue.addAll(c.getFolders());
                }
                parentContainer.getFolders().remove(targetFolder);

                mainWindow.requestMetadataSave(String.format("Deleted directory %s from %s", selected.getValue().getName(),
                                                             parent.getValue().getName()));

                // now calculate which blocks we can still safely wipe (the descriptor and inventory may have landed over some)
                long usedBlocks = BlockAllocationManager.calculateUsedBlocks(this.archive.getInventory());
                wipeblocks.subtract(new FragmentedRange((int) usedBlocks, Integer.MAX_VALUE));

                // now attempt wipe of those blocks if required
                if (!wipeblocks.isEmpty())
                {
                    if (QuickDialogs.confirm("Do you want to securely wipe the data blocks used by the files you deleted?"))
                    {
                        WipeBlocksOp op = new WipeBlocksOp(this.archive.filePath, this.archive.getBlockSize(), wipeblocks, true);
                        ProgressTask<Void> progressTask = new ProgressTask<Void>()
                        {
                            @Override
                            protected Void innerCall() throws Exception
                            {
                                this.updateMessage("Wiping blocks");
                                op.setProgressUpdate(o -> this.updateProgress(o.getBlocksWiped(), o.getTotalBlocks()));
                                op.run();
                                return null;
                            }

                            @Override
                            protected void failed()
                            {
                                QuickDialogs.exception(this.getException());
                            }
                        };

                        ProgressDialog pd = new ProgressDialog(progressTask);
                        pd.setHeaderText(String.format("Wiping %d data blocks ...", wipeblocks.size()));
                        Thread task = new Thread(progressTask);
                        task.setDaemon(true);
                        task.start();
                    }
                }
            }
            else
            {
                throw new BaseBunkrException("Attempted to delete a file but selected was a folder?");
            }
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }
}
