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

import javafx.event.Event;
import javafx.scene.control.TreeItem;
import javafx.util.Pair;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.*;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import org.bunkr.core.utils.Units;
import org.bunkr.gui.ProgressTask;
import org.bunkr.gui.components.treeview.InventoryTreeData;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.dialogs.ProgressDialog;
import org.bunkr.gui.dialogs.QuickDialogs;
import org.bunkr.gui.windows.MainWindow;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created At: 2016-01-17
 */
public class DragFileImportHandler implements Consumer<Pair<UUID, File>>
{
    private final ArchiveInfoContext archive;
    private final InventoryTreeView tree;
    private final MainWindow mainWindow;

    public DragFileImportHandler(ArchiveInfoContext archive, InventoryTreeView tree, MainWindow mainWindow)
    {
        this.archive = archive;
        this.tree = tree;
        this.mainWindow = mainWindow;
    }

    @Override
    public void accept(Pair<UUID, File> input)
    {
        try
        {
            TreeItem<InventoryTreeData> selected = this.tree.search(input.getKey());
            String selectedPath = this.tree.getPathForTreeItem(selected);
            IFFTraversalTarget selectedItem = InventoryPather.traverse(this.archive.getInventory(), selectedPath);
            if (selectedItem.isAFile())
            {
                QuickDialogs.error("Create Error", null, "'%s' is a file.", selectedPath);
                return;
            }

            // find subject FolderInventoryItem
            IFFContainer selectedContainer = (IFFContainer) selectedItem;

            // get new file name
            String newName = QuickDialogs.input("Enter a file name:", input.getValue().getName());
            if (newName == null) return;
            if (!InventoryPather.isValidName(newName))
            {
                QuickDialogs.error("Import Error", null, "'%s' is an invalid file name.", newName);
                return;
            }

            // check parent for the same name
            IFFTraversalTarget target = selectedContainer.findFileOrFolder(newName);
            FileInventoryItem newFile;
            if (target != null)
            {
                if (! QuickDialogs.confirm("Import Error", null, "There is already an item named '%s' in the parent folder. Do you want to replace it?", newName))
                {
                    return;
                }
                newFile = (FileInventoryItem) target;
            }
            else
            {
                newFile = new FileInventoryItem(newName);
            }

            ProgressTask<Void> progressTask = new ProgressTask<Void>()
            {
                @Override
                protected Void innerCall() throws Exception
                {
                    this.updateMessage("Opening file.");
                    FileChannel fc = new RandomAccessFile(input.getValue(), "r").getChannel();
                    long bytesTotal = fc.size();
                    long bytesDone = 0;
                    try (InputStream fis = Channels.newInputStream(fc))
                    {
                        try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(archive, newFile))
                        {
                            this.updateMessage("Importing bytes...");
                            byte[] buffer = new byte[(int) Units.MEBIBYTE];
                            int n;
                            while ((n = fis.read(buffer)) != -1)
                            {
                                bwos.write(buffer, 0, n);
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
                    // add to the container
                    if (target == null) selectedContainer.addFile(newFile);

                    // guess type
                    String guessedType = MediaType.guess(newFile.getName());

                    // pick the media type
                    newFile.setMediaType(QuickDialogs.pick(
                                                 "Import File",
                                                 null,
                                                 "Pick a Media Type for the new file:",
                                                 new ArrayList<>(MediaType.ALL_TYPES), guessedType)
                    );

                    TreeItem<InventoryTreeData> newItem = new TreeItem<>(new InventoryTreeData(newFile));
                    if (target != null)
                    {
                        selected.getChildren().removeIf(i -> i.getValue().getName().equals(newName));
                    }
                    selected.getChildren().add(newItem);
                    selected.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
                    selected.setExpanded(true);
                    Event.fireEvent(selected,
                                    new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected,
                                                                         newItem.getValue()));
                    tree.getSelectionModel().select(newItem);
                    mainWindow.requestMetadataSave(String.format("Imported file %s", newFile.getName()));
                }

                @Override
                protected void cancelled()
                {
                    mainWindow.requestMetadataSave("Cancelled file import");
                }

                @Override
                protected void failed()
                {
                    mainWindow.requestMetadataSave("Failed file import");
                    QuickDialogs.exception(this.getException());
                }
            };

            ProgressDialog pd = new ProgressDialog(progressTask);
            pd.setHeaderText(String.format("Importing file %s ...", newFile.getName()));
            Thread task = new Thread(progressTask);
            task.setDaemon(true);
            task.start();
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }
}
