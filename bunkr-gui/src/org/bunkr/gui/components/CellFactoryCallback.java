package org.bunkr.gui.components;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.bunkr.gui.controllers.InventoryCMController;

/**
 * Creator: benmeier
 * Created At: 2015-12-27
 */
public class CellFactoryCallback implements Callback<TreeView<IntermedInvTreeDS>, TreeCell<IntermedInvTreeDS>>
{
    private final InventoryCMController callbackContainer;

    public CellFactoryCallback(InventoryCMController callbackContainer)
    {
        this.callbackContainer = callbackContainer;
    }

    @Override
    public TreeCell<IntermedInvTreeDS> call(TreeView<IntermedInvTreeDS> param)
    {
        return new TreeCell<IntermedInvTreeDS>()
        {
            @Override
            protected void updateItem(IntermedInvTreeDS item, boolean empty)
            {
                super.updateItem(item, empty);
                if (! empty)
                {
                    setText(item != null ? item.getName() : "");
                    // setGraphic(createImageView(item));
                    if (item != null)
                    {
                        if (item.getType().equals(IntermedInvTreeDS.Type.ROOT))
                        {
                            setContextMenu(CellFactoryCallback.this.callbackContainer.rootContextMenu);
                        }
                        else if (item.getType().equals(IntermedInvTreeDS.Type.FOLDER))
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
