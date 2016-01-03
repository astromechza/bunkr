package org.bunkr.gui.components.treeview;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.bunkr.core.Resources;
import org.bunkr.core.utils.Logging;
import org.bunkr.gui.controllers.InventoryCMController;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-27
 */
public class CellFactoryCallback implements Callback<TreeView<InventoryTreeData>, TreeCell<InventoryTreeData>>
{
    private final String fileImagePath, folderImagePath;

    private final InventoryCMController callbackContainer;

    public CellFactoryCallback(InventoryCMController callbackContainer)
    {
        this.callbackContainer = callbackContainer;

        String temp = null;
        try
        {
            temp = Resources.getExternalPath("/resources/images/file.png");
        }
        catch (IOException e)
        {
            Logging.exception(e);
        }
        this.fileImagePath = temp;

        temp = null;
        try
        {
            temp = Resources.getExternalPath("/resources/images/folder.png");
        }
        catch (IOException e)
        {
            Logging.exception(e);
        }
        this.folderImagePath = temp;
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
                        if (item.getType().equals(InventoryTreeData.Type.ROOT))
                        {
                            setGraphic(new ImageView(folderImagePath));
                            setContextMenu(CellFactoryCallback.this.callbackContainer.rootContextMenu);
                        }
                        else if (item.getType().equals(InventoryTreeData.Type.FOLDER))
                        {
                            setGraphic(new ImageView(folderImagePath));
                            setContextMenu(CellFactoryCallback.this.callbackContainer.dirContextMenu);
                        }
                        else
                        {
                            setGraphic(new ImageView(fileImagePath));
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
