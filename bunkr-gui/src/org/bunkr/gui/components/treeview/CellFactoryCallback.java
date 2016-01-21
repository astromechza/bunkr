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

package org.bunkr.gui.components.treeview;

import javafx.event.EventHandler;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import javafx.util.Pair;
import org.bunkr.gui.Icons;
import org.bunkr.gui.controllers.ContextMenus;

import java.io.File;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created At: 2015-12-27
 */
public class CellFactoryCallback implements Callback<TreeView<InventoryTreeData>, TreeCell<InventoryTreeData>>
{
    private final ContextMenus callbackContainer;
    private final EventHandler<DragEvent> dragOverHandler;
    private Consumer<Pair<UUID, File>> fileDragImportHandler;

    public CellFactoryCallback(ContextMenus callbackContainer)
    {
        this.callbackContainer = callbackContainer;

        this.dragOverHandler = event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles() && db.getFiles().size() == 1)
            {
                event.acceptTransferModes(TransferMode.COPY);
            }
            else
            {
                event.consume();
            }
        };
    }

    @Override
    public TreeCell<InventoryTreeData> call(TreeView<InventoryTreeData> param)
    {
        return new TreeCell<InventoryTreeData>()
        {
            @Override
            protected void updateItem(InventoryTreeData item, boolean empty)
            {
                super.updateItem(item, empty);
                if (! empty)
                {
                    setText(item != null ? item.getName() : "");
                    if (item != null)
                    {
                        setGraphic(Icons.buildIconLabel(item.getIcon()));
                        if (item.getType().equals(InventoryTreeData.Type.ROOT))
                        {
                            setContextMenu(callbackContainer.rootContextMenu);
                            setOnDragOver(dragOverHandler);
                            setOnDragDropped(new DragDropEventHandler(item.getUuid()));
                        }
                        else if (item.getType().equals(InventoryTreeData.Type.FOLDER))
                        {
                            setContextMenu(callbackContainer.dirContextMenu);
                            setOnDragOver(dragOverHandler);
                            setOnDragDropped(new DragDropEventHandler(item.getUuid()));
                        }
                        else
                        {
                            setContextMenu(callbackContainer.fileContextMenu);
                            setOnDragOver(null);
                            setOnDragDropped(null);
                        }
                    }
                }
                else
                {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                    setOnDragOver(null);
                }
            }
        };
    }

    public void setFileDragImportHandler(Consumer<Pair<UUID, File>> fileDragImportHandler)
    {
        this.fileDragImportHandler = fileDragImportHandler;
    }

    private class DragDropEventHandler implements EventHandler<DragEvent>
    {
        private final UUID uuid;

        public DragDropEventHandler(UUID uuid)
        {
            this.uuid = uuid;
        }

        @Override
        public void handle(DragEvent event)
        {
            Dragboard db = event.getDragboard();
            if (! db.hasFiles()) return;
            if (db.getFiles().size() != 1) return;
            File fileToImport = db.getFiles().get(0);
            fileDragImportHandler.accept(new Pair<>(uuid, fileToImport));
            event.consume();
        }
    }
}
