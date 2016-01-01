package org.bunkr.gui.controllers;

import javafx.scene.control.TabPane;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.gui.components.MarkdownTab;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Creator: benmeier
 * Created At: 2015-12-30
 */
public class FilesTabPaneController
{
    private final ArchiveInfoContext archive;
    private final TabPane pane;
    private final Map<UUID, MarkdownTab> openTabs;

    private Consumer<String> onSaveInventoryRequest;

    public FilesTabPaneController(ArchiveInfoContext archive, TabPane subjectPane)
    {
        this.archive = archive;
        this.pane = subjectPane;
        this.openTabs = new HashMap<>();
    }

    /**
     * requestOpen
     * Open the given file in a tab. If it is already open in another tab, switch to it.
     */
    public void requestOpen(FileInventoryItem file)
    {
        if (openTabs.containsKey(file.getUuid()))
        {
            this.pane.getSelectionModel().select(openTabs.get(file.getUuid()));
        }
        else
        {
            MarkdownTab tab = new MarkdownTab(file, this.archive);
            tab.setOnClosed(e -> {
                this.openTabs.remove(file.getUuid());
            });
            tab.setOnSaveInventoryRequest(s -> this.getOnSaveInventoryRequest().accept(s));

            this.pane.getTabs().add(tab);
            this.openTabs.put(file.getUuid(), tab);
            this.pane.getSelectionModel().select(tab);
        }
    }

    /**
     * requestClose
     * Close the given file tab if it is open. If it isn't open, dont bother. If the file has pending changes, allow
     * them to be saved if necessary before resuming the close.
     */
    public void requestClose(FileInventoryItem file, boolean allowSavePrompt)
    {
        if (openTabs.containsKey(file.getUuid()))
        {
            this.pane.getTabs().remove(openTabs.get(file.getUuid()));
            this.openTabs.remove(file.getUuid());
        }
    }

    /**
     * notifyRename
     * Notify that a file has been renamed or moved. This may mean updating GUI fields on the tab if the file is open.
     */
    public void notifyRename(FileInventoryItem file)
    {
        if (openTabs.containsKey(file.getUuid()))
        {
            this.openTabs.get(file.getUuid()).notifyRename();
        }
    }

    public Consumer<String> getOnSaveInventoryRequest()
    {
        return onSaveInventoryRequest;
    }

    public void setOnSaveInventoryRequest(Consumer<String> onSaveInventoryRequest)
    {
        this.onSaveInventoryRequest = onSaveInventoryRequest;
    }
}
