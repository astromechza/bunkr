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
import org.bunkr.core.inventory.FolderInventoryItem;
import org.bunkr.core.inventory.IFFContainer;
import org.bunkr.core.inventory.IFFTraversalTarget;
import org.bunkr.core.inventory.InventoryPather;
import org.bunkr.gui.components.treeview.InventoryTreeData;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.dialogs.QuickDialogs;
import org.bunkr.gui.windows.MainWindow;

/**
 * Created At: 2016-01-17
 */
public class NewSubDirHandler implements EventHandler<ActionEvent>
{
    private final ArchiveInfoContext archive;
    private final InventoryTreeView tree;
    private final MainWindow mainWindow;

    public NewSubDirHandler(ArchiveInfoContext archive, InventoryTreeView tree, MainWindow mainWindow)
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
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItemOrRoot();
            String selectedPath = this.tree.getPathForTreeItem(selected);
            IFFTraversalTarget selectedItem = InventoryPather.traverse(this.archive.getInventory(), selectedPath);
            if (selectedItem.isAFile())
            {
                // if parent is a file, go up a level
                selected = selected.getParent();
                selectedPath = this.tree.getPathForTreeItem(selected);
                selectedItem = InventoryPather.traverse(this.archive.getInventory(), selectedPath);
            }

            // find subject FolderInventoryItem
            IFFContainer selectedContainer = (IFFContainer) selectedItem;

            // get new file name
            String newName = QuickDialogs.input("Enter a new directory name:", "");
            if (newName == null) return;
            if (! InventoryPather.isValidName(newName))
            {
                QuickDialogs.error("Create Error", "New Folder Error", "'%s' is an invalid file name.", newName);
                return;
            }

            // check parent for the same name
            IFFTraversalTarget target = selectedContainer.findFileOrFolder(newName);
            if (target != null)
            {
                QuickDialogs.error("Create Error", "New Folder Error",
                                   "There is already an item named '%s' in the parent folder.", newName);
                return;
            }

            FolderInventoryItem newFolder = new FolderInventoryItem(newName);
            selectedContainer.addFolder(newFolder);

            // create the new tree item
            InventoryTreeData newValue = new InventoryTreeData(newFolder);
            TreeItem<InventoryTreeData> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);
            selected.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));

            Event.fireEvent(selected,
                            new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
            this.tree.getSelectionModel().select(newItem);
            mainWindow.requestMetadataSave(String.format("Created new directory %s", newFolder.getName()));
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }
}
