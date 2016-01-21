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
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.IFFTraversalTarget;
import org.bunkr.core.inventory.InventoryPather;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import org.bunkr.core.utils.Units;
import org.bunkr.gui.ProgressTask;
import org.bunkr.gui.components.treeview.InventoryTreeData;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.dialogs.ProgressDialog;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Created At: 2016-01-17
 */
public class FileExportHandler implements EventHandler<ActionEvent>
{
    private final ArchiveInfoContext archive;
    private final InventoryTreeView tree;

    public FileExportHandler(ArchiveInfoContext archive, InventoryTreeView tree)
    {
        this.archive = archive;
        this.tree = tree;
    }

    @Override
    public void handle(ActionEvent event)
    {
        try
        {
            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItem();
            String selectedPath = this.tree.getPathForTreeItem(selected);

            IFFTraversalTarget selectedItem = InventoryPather.traverse(this.archive.getInventory(), selectedPath);

            // fail if not a file
            if (! (selectedItem instanceof FileInventoryItem))
            {
                QuickDialogs.error("%s is not a file inventory item.", selectedPath);
                return;
            }

            FileInventoryItem selectedFile = (FileInventoryItem) selectedItem;

            // choose file to be exported
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Export Location ...");
            fileChooser.setInitialFileName(selectedFile.getName());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));

            if (selectedFile.getName().contains(".") && selectedFile.getName().lastIndexOf('.') > 0)
            {
                String foundExtension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.'));
                if (foundExtension.length() > 0)
                {
                    FileChooser.ExtensionFilter o = new FileChooser.ExtensionFilter("Original Extension", String.format("*.%s", foundExtension));
                    fileChooser.getExtensionFilters().add(o);
                    fileChooser.setSelectedExtensionFilter(o);
                }
            }

            File exportedFile = fileChooser.showSaveDialog(this.tree.getScene().getWindow());
            if (exportedFile == null) return;

            ProgressTask<Void> progressTask = new ProgressTask<Void>()
            {
                @Override
                protected Void innerCall() throws Exception
                {
                    this.updateMessage("Opening file.");
                    FileChannel fc = new RandomAccessFile(exportedFile, "rw").getChannel();
                    long bytesTotal = selectedFile.getActualSize();
                    long bytesDone = 0;
                    try (OutputStream contentOutputStream = Channels.newOutputStream(fc))
                    {
                        try (MultilayeredInputStream ms = new MultilayeredInputStream(archive, selectedFile))
                        {
                            this.updateMessage("Exporting bytes...");
                            byte[] buffer = new byte[(int) Units.MEBIBYTE];
                            int n;
                            while ((n = ms.read(buffer)) != -1)
                            {
                                contentOutputStream.write(buffer, 0, n);
                                bytesDone += n;
                                this.updateProgress(bytesDone, bytesTotal);
                            }
                            Arrays.fill(buffer, (byte) 0);
                            this.updateMessage("Finished.");
                        }
                    }
                    return null;
                }

                @Override
                protected void succeeded()
                {
                    QuickDialogs.info("File successfully exported to %s", exportedFile.getAbsolutePath());
                }

                @Override
                protected void failed()
                {
                    QuickDialogs.exception(this.getException());
                }
            };

            ProgressDialog pd = new ProgressDialog(progressTask);
            pd.setHeaderText(String.format("Exporting file %s ...", exportedFile.getName()));
            new Thread(progressTask).start();
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }
}
