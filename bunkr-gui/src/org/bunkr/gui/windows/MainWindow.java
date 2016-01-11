package org.bunkr.gui.windows;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.Resources;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.exceptions.TraversalException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.IFFTraversalTarget;
import org.bunkr.core.inventory.InventoryPather;
import org.bunkr.core.inventory.MediaType;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.Logging;
import org.bunkr.gui.Icons;
import org.bunkr.gui.components.tabs.IOpenedFileTab;
import org.bunkr.gui.components.tabs.TabLoadError;
import org.bunkr.gui.components.tabs.images.ImageTab;
import org.bunkr.gui.components.tabs.markdown.MarkdownTab;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.controllers.InventoryCMController;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-12-26
 */
public class MainWindow extends BaseWindow
{
    private static final int WINDOW_WIDTH = 800, WINDOW_HEIGHT = 600;

    private final ArchiveInfoContext archive;
    private final String cssPath;
    private final UserSecurityProvider securityProvider;

    private Label lblHierarchy;
    private TabPane tabPane;
    private InventoryTreeView tree;
    private Button encryptionSettingsButton;

    private final Map<UUID, IOpenedFileTab> openTabs;

    public MainWindow(ArchiveInfoContext archive, UserSecurityProvider securityProvider) throws IOException
    {
        super();

        this.archive = archive;
        this.securityProvider = securityProvider;
        this.cssPath = Resources.getExternalPath("/resources/css/main_window.css");
        this.openTabs = new HashMap<>();
        this.initialise();

        InventoryCMController contextMenuController = new InventoryCMController(this.archive, this.tree);
        contextMenuController.bindEvents();
        contextMenuController.setOnSaveInventoryRequest(this::saveMetadata);
        contextMenuController.setOnRenameFile(this::notifyRename);
        contextMenuController.setOnDeleteFile(this::requestClose);
        contextMenuController.setOnOpenFile(this::requestOpen);

        this.tree.refreshAll();
    }

    @Override
    public void initControls()
    {
        this.lblHierarchy = new Label("File Structure");
        this.tree = new InventoryTreeView(this.archive);
        this.tabPane = new TabPane();
        this.encryptionSettingsButton = Icons.buildIconButton("Security Settings", Icons.ICON_SETTINGS);
        this.encryptionSettingsButton.setOnAction(event -> {
            try
            {
                ArchiveSecurityWindow popup = new ArchiveSecurityWindow(this.archive, this.securityProvider);
                popup.setOnSaveDescriptorRequest(this::saveMetadata);
                popup.getStage().showAndWait();
            }
            catch (IOException e)
            {
                QuickDialogs.exception(e);
            }
        });
    }

    @Override
    public Parent initLayout()
    {
        SplitPane sp = new SplitPane();
        sp.setDividerPosition(0, 0.3);

        VBox leftBox = new VBox(0, this.lblHierarchy, this.tree, this.encryptionSettingsButton);
        VBox.setVgrow(this.lblHierarchy, Priority.NEVER);
        this.lblHierarchy.setAlignment(Pos.CENTER);
        VBox.setVgrow(this.tree, Priority.ALWAYS);
        VBox.setVgrow(this.encryptionSettingsButton, Priority.NEVER);
        this.encryptionSettingsButton.setMaxWidth(Double.MAX_VALUE);

        sp.getItems().add(leftBox);
        sp.getItems().add(this.tabPane);
        return sp;
    }

    @Override
    public void bindEvents()
    {

    }

    @Override
    public void applyStyling()
    {
        this.lblHierarchy.getStyleClass().add("hierarchy-label");
        this.lblHierarchy.setAlignment(Pos.CENTER);
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(this.cssCommon);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle(String.format("Bunkr - %s", this.archive.filePath.getAbsolutePath()));
        this.getStage().setScene(scene);
        this.getStage().setResizable(true);
        return scene;
    }

    private void saveMetadata(String reason)
    {
        try
        {
            Logging.info("Saving Archive Metadata due to %s", reason);
            MetadataWriter.write(this.archive, this.securityProvider);
            Logging.info("Saved Archive Metadata");
        }
        catch (IOException | BaseBunkrException e)
        {
            QuickDialogs.exception(e);
        }
    }

    /**
     * requestOpen
     * Open the given file in a tab. If it is already open in another tab, switch to it.
     */
    public void requestOpen(FileInventoryItem file)
    {
        if (openTabs.containsKey(file.getUuid()))
        {
            this.tabPane.getSelectionModel().select((Tab) openTabs.get(file.getUuid()));
        }
        else
        {
            try
            {
                IOpenedFileTab tab;
                switch (file.getMediaType())
                {
                    case MediaType.TEXT:
                        tab = new MarkdownTab(file, this.archive);
                        ((MarkdownTab) tab).setOnSaveInventoryRequest(this::saveMetadata);
                        ((MarkdownTab) tab).setOnTryOpenFileItem(this::tryOpenFileItem);
                        break;
                    case MediaType.IMAGE:
                        tab = new ImageTab(file, this.archive);
                        break;
                    default:
                        QuickDialogs.error("Cannot open file with media type: " + file.getMediaType());
                        return;
                }
                ((Tab) tab).setOnClosed(e -> this.openTabs.remove(file.getUuid()));
                this.tabPane.getTabs().add((Tab) tab);
                this.openTabs.put(file.getUuid(), tab);
                this.tabPane.getSelectionModel().select((Tab) tab);
            }
            catch (TabLoadError tabLoadError)
            {
                QuickDialogs.error("Tab Load Error", "An error occured while building the new tab", tabLoadError.getMessage());
            }
        }
    }

    private void tryOpenFileItem(String abspath)
    {
        try
        {
            IFFTraversalTarget t = InventoryPather.traverse(this.archive.getInventory(), abspath);
            if (t.isAFile() && t instanceof FileInventoryItem)
            {
                this.requestOpen((FileInventoryItem) t);
            }
        }
        catch (TraversalException e)
        {
            QuickDialogs.exception(e);
        }
    }

    /**
     * requestClose
     * Close the given file tab if it is open. If it isn't open, dont bother. If the file has pending changes, allow
     * them to be saved if necessary before resuming the close.
     */
    public void requestClose(FileInventoryItem file)
    {
        if (openTabs.containsKey(file.getUuid()))
        {
            this.tabPane.getTabs().remove((Tab) openTabs.get(file.getUuid()));
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
}
