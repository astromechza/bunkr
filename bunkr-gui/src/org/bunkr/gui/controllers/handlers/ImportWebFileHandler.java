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
import okhttp3.*;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.*;
import org.bunkr.core.inventory.MediaType;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import org.bunkr.core.utils.Units;
import org.bunkr.gui.ProgressTask;
import org.bunkr.gui.components.treeview.InventoryTreeData;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.dialogs.ProgressDialog;
import org.bunkr.gui.dialogs.QuickDialogs;
import org.bunkr.gui.windows.MainWindow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Arrays;

/**
 * Created At: 2016-01-17
 */
public class ImportWebFileHandler implements EventHandler<ActionEvent>
{
    private final ArchiveInfoContext archive;
    private final InventoryTreeView tree;
    private final MainWindow mainWindow;

    public ImportWebFileHandler(ArchiveInfoContext archive, InventoryTreeView tree, MainWindow mainWindow)
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
            // get input url
            String url = QuickDialogs.input("Please enter URL:", null);
            if (url == null) return;

            // get new file name
            String placeholderName = new File(new URI(url).getPath()).getName();
            String newName = QuickDialogs.input("Enter a file name:", placeholderName);
            if (newName == null) return;
            if (! InventoryPather.isValidName(newName))
            {
                QuickDialogs.error("Import Error", null, "'%s' is an invalid file name.", newName);
                return;
            }

            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeFolder();

            String selectedPath = this.tree.getPathForTreeItem(selected);
            IFFTraversalTarget selectedItem = InventoryPather.traverse(this.archive.getInventory(), selectedPath);
            if (selectedItem.isAFile())
            {
                QuickDialogs.error("Create Error", null, "'%s' is a file.", selectedPath);
                return;
            }

            // find subject FolderInventoryItem
            IFFContainer selectedContainer = (IFFContainer) selectedItem;

            // check parent for the same name
            IFFTraversalTarget target = selectedContainer.findFileOrFolder(newName);
            FileInventoryItem newFile;
            if (target != null)
            {
                if (! QuickDialogs.confirmFull("Import Error", null, "There is already an item named '%s' in the parent folder. Do you want to replace it?", newName))
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
                    Response r = downloadContent(url, newFile, this);
                    okhttp3.MediaType mt = r.body().contentType();
                    if (mt.type().equals("text"))
                    {
                        if (mt.subtype().equals("html"))
                        {
                            newFile.setMediaType(MediaType.HTML);
                        }
                        else
                        {
                            newFile.setMediaType(MediaType.TEXT);
                        }
                    }
                    else if (mt.type().equals("image"))
                    {
                        newFile.setMediaType(MediaType.IMAGE);
                    }
                    else
                    {
                        newFile.setMediaType(MediaType.guess(newFile.getName()));
                    }
                    return null;
                }

                @Override
                protected void succeeded()
                {
                    // add to the container if this is a new item
                    if (target == null) selectedContainer.addFile(newFile);

                    TreeItem<InventoryTreeData> newItem = new TreeItem<>(new InventoryTreeData(newFile));
                    if (target != null) selected.getChildren().removeIf(i -> i.getValue().getName().equals(newName));
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
                protected void failed()
                {
                    mainWindow.requestMetadataSave("Failed file import");
                    QuickDialogs.exception(this.getException());
                }

                @Override
                protected void cancelled()
                {
                    mainWindow.requestMetadataSave("Cancelled file import");
                }
            };

            ProgressDialog pd = new ProgressDialog(progressTask);
            pd.setContentText(String.format("Reading url: %s", url));
            new Thread(progressTask).start();
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }

    private Response downloadContent(String url, FileInventoryItem destination, ProgressTask<Void> task) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(url).build();
        task.updateMessage("Beginning Connection..");
        Response response = client.newCall(request).execute();
        if (! response.isSuccessful()) throw new IOException("Request returned code " + response.code());
        try (InputStream connInputStream = response.body().byteStream())
        {
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(archive, destination))
            {
                task.updateMessage("Reading from stream..");
                long total = response.body().contentLength();
                long bytesRead = 0;
                byte[] buffer = new byte[(int)(32 * Units.KIBIBYTE)];
                int n;
                while ((n = connInputStream.read(buffer)) > -1)
                {
                    bwos.write(buffer, 0, n);
                    bytesRead += n;
                    task.updateProgress(bytesRead, total);
                }
                Arrays.fill(buffer, (byte) 0);
                task.updateMessage("Finished Downloading.");
                return response;
            }
        }
    }
}
