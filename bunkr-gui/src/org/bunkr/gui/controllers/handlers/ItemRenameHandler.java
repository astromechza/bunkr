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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.*;
import org.bunkr.gui.components.treeview.InventoryTreeData;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.dialogs.QuickDialogs;
import org.bunkr.gui.windows.MainWindow;

/**
 * Created At: 2016-01-17
 */
public class ItemRenameHandler implements EventHandler<ActionEvent>
{
    private final ArchiveInfoContext archive;
    private final InventoryTreeView tree;
    private final MainWindow mainWindow;

    public ItemRenameHandler(ArchiveInfoContext archive, InventoryTreeView tree, MainWindow mainWindow)
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
            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItem();

            // find parent item
            TreeItem<InventoryTreeData> oldParentItem = selected.getParent();
            String parentPath = this.tree.getPathForTreeItem(oldParentItem);
            IFFContainer oldParentContainer = (IFFContainer) InventoryPather
                    .traverse(this.archive.getInventory(), parentPath);

            // get new file name
            String userInputPath = QuickDialogs.input("Enter a new file name:", selected.getValue().getName());
            if (userInputPath == null) return;

            String traversalPathComponent = "";
            String newNameComponent = userInputPath;
            if (userInputPath.contains("/") && InventoryPather.isValidRelativePath(userInputPath))
            {
                traversalPathComponent = InventoryPather.dirname(newNameComponent);
                newNameComponent = InventoryPather.baseName(newNameComponent);
            }
            else if (userInputPath.contains("/") && InventoryPather.isValidPath(userInputPath))
            {
                traversalPathComponent = InventoryPather.dirname(newNameComponent);
                newNameComponent = InventoryPather.baseName(newNameComponent);
            }
            else if (! InventoryPather.isValidName(newNameComponent))
            {
                QuickDialogs.error("Rename Error", "'%s' is an invalid file name, relative file path, or absolute file path.", newNameComponent);
                return;
            }

            // get subject item that we can rename
            IFFTraversalTarget renameSubject = oldParentContainer.findFileOrFolder(selected.getValue().getName());
            if (renameSubject == null)
            {
                QuickDialogs.error("Rename Error", "Critical! No subject item.");
                return;
            }

            String oldParentPathString = this.tree.getPathForTreeItem(oldParentItem);
            String newParentPathString = (traversalPathComponent.startsWith("/")) ? traversalPathComponent : InventoryPather.applyRelativePath(oldParentPathString, traversalPathComponent);

            IFFContainer newParentContainer = oldParentContainer;
            TreeItem<InventoryTreeData> newParentItem = oldParentItem;
            if (!newParentPathString.equals(oldParentPathString))
            {
                IFFTraversalTarget pt = InventoryPather.traverse(this.archive.getInventory(), newParentPathString);
                if (pt.isAFile())
                {
                    QuickDialogs.error("Rename Error", "Cannot move folder to be a child of file '%s'.", InventoryPather.baseName(newParentPathString));
                    return;
                }
                newParentContainer = (IFFContainer) pt;
                newParentItem = this.tree.traverseTo(newParentPathString);
            }

            // check parent for the same name
            IFFTraversalTarget target = newParentContainer.findFileOrFolder(newNameComponent);
            if (target != null)
            {
                QuickDialogs.error("Rename Error", "There is already an item named '%s' in the parent folder.", newNameComponent);
                return;
            }

            // rename the subject
            if (renameSubject.isAFolder())
            {
                ((FolderInventoryItem) renameSubject).setName(newNameComponent);
            }
            else if (renameSubject.isAFile())
            {
                ((FileInventoryItem) renameSubject).setName(newNameComponent);
            }
            else
            {
                QuickDialogs.error("Rename Error", "Critical! cannot rename a root.");
                return;
            }

            if (newParentContainer != oldParentContainer)
            {
                if (renameSubject.isAFolder() && renameSubject instanceof FolderInventoryItem)
                {
                    oldParentContainer.removeFolder((FolderInventoryItem) renameSubject);
                    newParentContainer.addFolder((FolderInventoryItem) renameSubject);
                }
                else if (renameSubject.isAFile() && renameSubject instanceof FileInventoryItem)
                {
                    oldParentContainer.removeFile((FileInventoryItem) renameSubject);
                    newParentContainer.addFile((FileInventoryItem) renameSubject);
                }
            }
            if (oldParentItem != newParentItem)
            {
                oldParentItem.getChildren().remove(selected);
            }
            selected.getValue().setName(newNameComponent);
            if (oldParentItem != newParentItem)
            {
                newParentItem.getChildren().add(selected);
            }
            newParentItem.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
            Event.fireEvent(selected, new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected,
                                                                           selected.getValue()));
            this.tree.getSelectionModel().select(selected);
            mainWindow.requestMetadataSave(String.format("Renamed file %s", newNameComponent));
            if (renameSubject.isAFile() && renameSubject instanceof FileInventoryItem)
            {
                FileInventoryItem renameFile = (FileInventoryItem) renameSubject;
                this.mainWindow.requestNotifyRename(renameFile);
            }
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }
}
