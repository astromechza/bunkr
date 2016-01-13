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

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.bunkr.gui.Icons;
import org.bunkr.gui.controllers.ContextMenus;

/**
 * Creator: benmeier
 * Created At: 2015-12-27
 */
public class CellFactoryCallback implements Callback<TreeView<InventoryTreeData>, TreeCell<InventoryTreeData>>
{
    private final ContextMenus callbackContainer;

    public CellFactoryCallback(ContextMenus callbackContainer)
    {
        this.callbackContainer = callbackContainer;
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
                            setContextMenu(CellFactoryCallback.this.callbackContainer.rootContextMenu);
                        }
                        else if (item.getType().equals(InventoryTreeData.Type.FOLDER))
                        {
                            setContextMenu(CellFactoryCallback.this.callbackContainer.dirContextMenu);
                        }
                        else
                        {
                            setContextMenu(CellFactoryCallback.this.callbackContainer.fileContextMenu);
                        }
                    }
                }
                else
                {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                }
            }
        };
    }
}
