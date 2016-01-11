package org.bunkr.gui.components.treeview;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.bunkr.gui.Icons;
import org.bunkr.gui.controllers.MainWindowActionsController;

/**
 * Creator: benmeier
 * Created At: 2015-12-27
 */
public class CellFactoryCallback implements Callback<TreeView<InventoryTreeData>, TreeCell<InventoryTreeData>>
{
    private final MainWindowActionsController callbackContainer;

    public CellFactoryCallback(MainWindowActionsController callbackContainer)
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
