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

package org.bunkr.gui.windows;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.Resources;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.inventory.*;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.Logging;
import org.bunkr.gui.Icons;
import org.bunkr.gui.components.tabs.IOpenedFileTab;
import org.bunkr.gui.components.tabs.TabLoadError;
import org.bunkr.gui.components.tabs.html.HtmlTab;
import org.bunkr.gui.components.tabs.images.ImageTab;
import org.bunkr.gui.components.tabs.markdown.MarkdownTab;
import org.bunkr.gui.components.treeview.CellFactoryCallback;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.controllers.*;
import org.bunkr.gui.controllers.handlers.*;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.*;
import java.util.*;

/**
 * Created At: 2015-12-26
 */
public class MainWindow extends BaseWindow
{
    private static final int WINDOW_WIDTH = 800, WINDOW_HEIGHT = 600;

    private final ArchiveInfoContext archive;
    private final String cssPath;
    private final UserSecurityProvider securityProvider;

    private TabPane tabPane;
    private InventoryTreeView tree;
    private Button encryptionSettingsButton;
    private Button newFileButton;
    private Button newFolderButton;

    private final Map<UUID, IOpenedFileTab> openTabs;

    public MainWindow(ArchiveInfoContext archive, UserSecurityProvider securityProvider) throws IOException
    {
        super();

        this.archive = archive;
        this.securityProvider = securityProvider;
        this.cssPath = Resources.getExternalPath("/resources/css/main_window.css");
        this.openTabs = new HashMap<>();
        this.initialise();

        ContextMenus contextMenuController = new ContextMenus(this.tree);
        contextMenuController.fileOpen.setOnAction(new FileOpenHandler(archive, tree, this));
        contextMenuController.fileRename.setOnAction(new ItemRenameHandler(archive, tree, this));
        contextMenuController.fileDelete.setOnAction(new FileDeleteHandler(archive, tree, this));
        contextMenuController.fileInfo.setOnAction(new FileInfoHandler(archive, tree, this));
        contextMenuController.fileExport.setOnAction(new FileExportHandler(archive, tree));
        contextMenuController.dirDelete.setOnAction(new DirDeleteHandler(archive, tree, this));
        contextMenuController.dirRename.setOnAction(new ItemRenameHandler(archive, tree, this));
        contextMenuController.dirNewFile.setOnAction(new NewFileHandler(archive, tree, this));
        contextMenuController.dirNewSubDir.setOnAction(new NewSubDirHandler(archive, tree, this));
        contextMenuController.dirImportFile.setOnAction(new ImportFileHandler(archive, tree, this));
        contextMenuController.dirImportWeb.setOnAction(new ImportWebFileHandler(archive, tree, this));
        contextMenuController.rootNewSubDir.setOnAction(new NewSubDirHandler(archive, tree, this));
        contextMenuController.rootNewFile.setOnAction(new NewFileHandler(archive, tree, this));
        contextMenuController.rootImportFile.setOnAction(new ImportFileHandler(archive, tree, this));
        contextMenuController.rootImportWeb.setOnAction(new ImportWebFileHandler(archive, tree, this));

        CellFactoryCallback cellFactory = new CellFactoryCallback(contextMenuController);
        cellFactory.setFileDragImportHandler(new DragFileImportHandler(archive, tree, this));
        this.tree.setCellFactory(cellFactory);

        this.tree.refreshAll();
    }

    @Override
    public void initControls()
    {
        this.tree = new InventoryTreeView(this.archive);
        this.tabPane = new TabPane();
        this.encryptionSettingsButton = Icons.buildIconButton("Security Settings", Icons.ICON_SETTINGS);
        this.encryptionSettingsButton.setOnAction(event -> {
            try
            {
                ArchiveSecurityWindow popup = new ArchiveSecurityWindow(this.archive, this.securityProvider);
                popup.setOnSaveMetadataRequest(this::requestMetadataSave);
                popup.getStage().showAndWait();
            }
            catch (IOException e)
            {
                QuickDialogs.exception(e);
            }
        });
        this.encryptionSettingsButton.setTooltip(new Tooltip("Change the encryption settings on this archive"));
        this.newFileButton = Icons.buildIconButton("New File", Icons.ICON_FILE);
        this.newFileButton.setTooltip(new Tooltip("Add new file"));
        this.newFileButton.setOnAction(event -> {
            new NewFileHandler(archive, tree, this).handle(event);
        });
        this.newFolderButton = Icons.buildIconButton("New Folder", Icons.ICON_FOLDER);
        this.newFolderButton.setTooltip(new Tooltip("Add new folder"));
        this.newFolderButton.setOnAction(event -> {
            new NewSubDirHandler(archive, tree, this).handle(event);
        });
    }

    @Override
    public Parent initLayout()
    {
        SplitPane sp = new SplitPane();
        sp.setDividerPosition(0, 0.3);

        HBox buttonBox = new HBox(0, this.newFileButton, this.newFolderButton);
        HBox.setHgrow(this.newFileButton, Priority.ALWAYS);
        HBox.setHgrow(this.newFolderButton, Priority.ALWAYS);
        newFileButton.setMaxWidth(Double.MAX_VALUE);
        newFolderButton.setMaxWidth(Double.MAX_VALUE);

        VBox leftBox = new VBox(0, buttonBox, this.tree, this.encryptionSettingsButton);
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

    public void requestMetadataSave(String reason)
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
                        ((MarkdownTab) tab).setOnSaveInventoryRequest(this::requestMetadataSave);
                        ((MarkdownTab) tab).setOnRequestOpen(this::requestOpen);
                        break;
                    case MediaType.HTML:
                        tab = new HtmlTab(file, this.archive);
                        ((HtmlTab) tab).setOnSaveInventoryRequest(this::requestMetadataSave);
                        break;
                    case MediaType.IMAGE:
                        tab = new ImageTab(file, this.archive);
                        break;
                    default:
                        QuickDialogs.error("Cannot open file with media type: " + file.getMediaType());
                        return;
                }
                Tab nativeTab = (Tab) tab;
                nativeTab.setOnClosed(e -> this.openTabs.remove(file.getUuid()));
                this.tabPane.getTabs().add(nativeTab);
                this.openTabs.put(file.getUuid(), tab);
                this.tabPane.getSelectionModel().select(nativeTab);
            }
            catch (TabLoadError tabLoadError)
            {
                QuickDialogs.error("Tab Load Error", "An error occured while building the new tab", tabLoadError.getMessage());
            }
        }
    }

    public void requestNotifyRename(FileInventoryItem file)
    {
        if (openTabs.containsKey(file.getUuid()))
        {
            this.openTabs.get(file.getUuid()).notifyRename();
        }
    }

    public void requestClose(FileInventoryItem file)
    {
        if (openTabs.containsKey(file.getUuid()))
        {
            this.tabPane.getTabs().remove((Tab) openTabs.get(file.getUuid()));
            this.openTabs.remove(file.getUuid());
        }
    }
}
