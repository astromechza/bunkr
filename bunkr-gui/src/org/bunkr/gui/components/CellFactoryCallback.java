package org.bunkr.gui.components;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/**
 * Creator: benmeier
 * Created At: 2015-12-27
 */
public class CellFactoryCallback implements Callback<TreeView<IntermedInvTreeDS>, TreeCell<IntermedInvTreeDS>>
{
    private final ContextMenus callbackMenus;

    public CellFactoryCallback(ContextMenus callbackMenus)
    {
        this.callbackMenus = callbackMenus;
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
                            setContextMenu(CellFactoryCallback.this.callbackMenus.rootContextMenu);
                        }
                        else if (item.getType().equals(IntermedInvTreeDS.Type.FOLDER))
                        {
                            setContextMenu(CellFactoryCallback.this.callbackMenus.dirContextMenu);
                        }
                        else
                        {
                            setContextMenu(CellFactoryCallback.this.callbackMenus.fileContextMenu);
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
