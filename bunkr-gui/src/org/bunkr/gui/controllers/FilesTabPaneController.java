package org.bunkr.gui.controllers;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.MediaType;
import org.bunkr.gui.components.tabs.IOpenedFileTab;
import org.bunkr.gui.components.tabs.images.ImageTab;
import org.bunkr.gui.components.tabs.markdown.MarkdownTab;
import org.bunkr.gui.dialogs.QuickDialogs;

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
    private final Map<UUID, IOpenedFileTab> openTabs;

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
            this.pane.getSelectionModel().select((Tab) openTabs.get(file.getUuid()));
        }
        else
        {
            IOpenedFileTab tab;
            switch(file.getMediaType())
            {
                case MediaType.TEXT:
                    tab = new MarkdownTab(file, this.archive);
                    ((MarkdownTab) tab).setOnSaveInventoryRequest(s -> this.getOnSaveInventoryRequest().accept(s));
                    break;
                case MediaType.IMAGE:
                    tab = new ImageTab(file, this.archive);
                    break;
                default:
                    QuickDialogs.error("Cannot open file with media type: " + file.getMediaType());
                    return;
            }
            ((Tab) tab).setOnClosed(e -> this.openTabs.remove(file.getUuid()));
            this.pane.getTabs().add((Tab) tab);
            this.openTabs.put(file.getUuid(), tab);
            this.pane.getSelectionModel().select((Tab) tab);
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
            this.pane.getTabs().remove((Tab) openTabs.get(file.getUuid()));
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
