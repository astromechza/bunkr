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
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.IFFTraversalTarget;
import org.bunkr.core.inventory.InventoryPather;
import org.bunkr.core.inventory.MediaType;
import org.bunkr.core.utils.Formatters;
import org.bunkr.core.utils.Units;
import org.bunkr.gui.components.treeview.InventoryTreeData;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.dialogs.QuickDialogs;
import org.bunkr.gui.windows.MainWindow;

/**
 * Created At: 2016-01-17
 */
public class FileOpenHandler implements EventHandler<ActionEvent>
{
    private final ArchiveInfoContext archive;
    private final InventoryTreeView tree;
    private final MainWindow mainWindow;

    public FileOpenHandler(ArchiveInfoContext archive, InventoryTreeView tree, MainWindow mainWindow)
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
            // get selected tree item
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItem();
            // make sure it looks like a file
            if (selected.getValue().getType() != InventoryTreeData.Type.FILE)
            {
                throw new BaseBunkrException("Failed to open item %s. It is not a file.", selected.getValue().getName());
            }

            // get absolute file path
            String selectedPath = this.tree.getPathForTreeItem(selected);

            // traverse down to correct file item
            IFFTraversalTarget selectedItem = InventoryPather.traverse(this.archive.getInventory(), selectedPath);
            // double check that its a file
            if (! selectedItem.isAFile())
            {
                throw new BaseBunkrException("Failed to open item %s. It is not a file.", selected.getValue().getName());
            }

            FileInventoryItem selectedFile = (FileInventoryItem) selectedItem;
            if (! MediaType.OPENABLE_TYPES.contains(selectedFile.getMediaType()))
            {
                QuickDialogs.error("Cannot open file of unknown type. Use Context Menu > Info to change the type.");
            }
            else if (selectedFile.getActualSize() < (Units.MEBIBYTE) || QuickDialogs.confirmFull(
                    "Please Confirm", "This is a large file!", "File %s is %s in size. Are you sure you want to open it?",
                    selectedFile.getName(), Formatters.formatBytes(selectedFile.getActualSize())))
            {
                mainWindow.requestOpen(selectedFile);
            }
        }
        catch (BaseBunkrException e)
        {
            QuickDialogs.exception(e);
        }
    }
}
