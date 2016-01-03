package org.bunkr.gui.components.tabpanes;

import java.util.function.Consumer;

/**
 * Creator: benmeier
 * Created At: 2016-01-03
 */
public interface IOpenedFileTab
{
    void setOnSaveInventoryRequest(Consumer<String> onSaveInventoryRequest);

    void notifyRename();
}
