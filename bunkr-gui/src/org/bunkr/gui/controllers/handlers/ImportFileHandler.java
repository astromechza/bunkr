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
import javafx.stage.FileChooser;
import javafx.util.Pair;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.gui.components.treeview.InventoryTreeData;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.dialogs.QuickDialogs;
import org.bunkr.gui.windows.MainWindow;

import java.io.File;

/**
 * Created At: 2016-01-17
 */
public class ImportFileHandler implements EventHandler<ActionEvent>
{
    private final InventoryTreeView tree;
    private final DragFileImportHandler subHandler;

    public ImportFileHandler(ArchiveInfoContext archive, InventoryTreeView tree, MainWindow mainWindow)
    {
        this.tree = tree;
        this.subHandler = new DragFileImportHandler(archive, tree, mainWindow);
    }

    @Override
    public void handle(ActionEvent event)
    {
        try
        {
            // first choose file to be imported
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File ...");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
            File importedFile = fileChooser.showOpenDialog(this.tree.getScene().getWindow());
            if (importedFile == null) return;

            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeFolder();

            subHandler.accept(new Pair<>(selected.getValue().getUuid(), importedFile));
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }
}
