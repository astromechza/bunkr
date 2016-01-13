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

import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.Resources;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.inventory.*;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.Formatters;
import org.bunkr.core.utils.Logging;
import org.bunkr.gui.Icons;
import org.bunkr.gui.ProgressTask;
import org.bunkr.gui.components.tabs.IOpenedFileTab;
import org.bunkr.gui.components.tabs.TabLoadError;
import org.bunkr.gui.components.tabs.images.ImageTab;
import org.bunkr.gui.components.tabs.markdown.MarkdownTab;
import org.bunkr.gui.components.treeview.InventoryTreeData;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.controllers.ContextMenus;
import org.bunkr.gui.dialogs.ProgressDialog;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.*;

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

        ContextMenus contextMenuController = new ContextMenus(this.tree);
        contextMenuController.fileOpen.setOnAction(event -> this.handleCMFileOpen());
        contextMenuController.fileRename.setOnAction(event -> this.handleCMRenameItem());
        contextMenuController.fileDelete.setOnAction(event -> this.handleCMFileDelete());
        contextMenuController.fileInfo.setOnAction(event -> this.handleCMFileInfo());
        contextMenuController.fileExport.setOnAction(event -> this.handleCMFileExport());
        contextMenuController.dirDelete.setOnAction(event -> this.handleCMDirDelete());
        contextMenuController.dirRename.setOnAction(event -> this.handleCMRenameItem());
        contextMenuController.dirNewFile.setOnAction(event -> this.handleCMNewFile());
        contextMenuController.dirNewSubDir.setOnAction(event -> this.handleCMNewSubDir());
        contextMenuController.dirImportFile.setOnAction(event -> this.handleCMImportFile());
        contextMenuController.rootNewSubDir.setOnAction(event -> this.handleCMNewSubDir());
        contextMenuController.rootNewFile.setOnAction(event -> this.handleCMNewFile());
        contextMenuController.rootImportFile.setOnAction(event -> this.handleCMImportFile());

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
                popup.setOnSaveDescriptorRequest(this::requestMetadataSave);
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

    private void requestMetadataSave(String reason)
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

    private void handleCMFileOpen()
    {
        try
        {
            // get selected tree item
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItem();
            // make sure it looks like a file
            if (selected.getValue().getType() != InventoryTreeData.Type.FILE)
            {
                throw new BaseBunkrException("Failed to open item %s. It is not a file.", selected.getValue().getName());
            }

            // get absolute file path
            String selectedPath = this.tree.getPathForTreeItem(selected);

            // traverse down to correct file item
            IFFTraversalTarget selectedItem = InventoryPather.traverse(this.archive.getInventory(), selectedPath);
            // double check that its a file
            if (! selectedItem.isAFile())
            {
                throw new BaseBunkrException("Failed to open item %s. It is not a file.", selected.getValue().getName());
            }

            FileInventoryItem selectedFile = (FileInventoryItem) selectedItem;
            if (! MediaType.OPENABLE_TYPES.contains(selectedFile.getMediaType()))
            {
                QuickDialogs.error("Cannot open file of unknown type. Use Context Menu > Info to change the type.");
            }
            else if (selectedFile.getActualSize() < (1024 * 1024) || QuickDialogs.confirm(
                    "Please Confirm", "This is a large file!", "File %s is %s in size. Are you sure you want to open it?",
                    selectedFile.getName(), Formatters.formatBytes(selectedFile.getActualSize())))
            {
                this.requestOpen(selectedFile);
            }
        }
        catch (BaseBunkrException e)
        {
            QuickDialogs.exception(e);
        }
    }

    private void handleCMFileDelete()
    {
        try
        {
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItem();

            if (!QuickDialogs.confirm(String.format("Are you sure you want to delete '%s'?", selected.getValue().getName())))
                return;

            // find parent item
            TreeItem<InventoryTreeData> parent = selected.getParent();
            String parentPath = this.tree.getPathForTreeItem(parent);

            // find inventory item
            IFFContainer parentContainer = (IFFContainer) InventoryPather.traverse(this.archive.getInventory(), parentPath);

            // just get inventory item
            IFFTraversalTarget target = parentContainer.findFileOrFolder(selected.getValue().getName());
            if (target instanceof FileInventoryItem)
            {
                FileInventoryItem targetFile = (FileInventoryItem) target;
                parentContainer.removeFile(targetFile);
                parent.getChildren().remove(selected);

                this.requestMetadataSave(
                        String.format("Deleted file %s from %s", selected.getValue().getName(), parentPath));

                if (openTabs.containsKey(targetFile.getUuid()))
                {
                    this.tabPane.getTabs().remove((Tab) openTabs.get(targetFile.getUuid()));
                    this.openTabs.remove(targetFile.getUuid());
                }
            }
            else
            {
                throw new BaseBunkrException("Attempted to delete a file but selected was a folder?");
            }
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }

    private void handleCMDirDelete()
    {
        try
        {
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItem();

            if (! QuickDialogs.confirm(
                    String.format("Are you sure you want to delete '%s' and all of its children?",
                                  selected.getValue().getName())))
                return;

            // find parent item
            TreeItem<InventoryTreeData> parent = selected.getParent();

            // find inventory item
            IFFContainer parentContainer;
            if (parent.getValue().getType().equals(InventoryTreeData.Type.ROOT))
            {
                parentContainer = this.archive.getInventory();
            }
            else
            {
                parentContainer = (IFFContainer) this.archive.getInventory().search(parent.getValue().getUuid());
            }

            // just get inventory item
            InventoryItem target = parentContainer.search(selected.getValue().getUuid());

            if (target instanceof FolderInventoryItem)
            {
                FolderInventoryItem targetFolder = (FolderInventoryItem) target;
                parentContainer.getFolders().remove(targetFolder);
                parent.getChildren().remove(selected);
            }
            else
            {
                throw new BaseBunkrException("Attempted to delete a file but selected was a folder?");
            }
            this.requestMetadataSave(String.format("Deleted directory %s from %s", selected.getValue().getName(),
                                                   parent.getValue().getName()));
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }

    private void handleCMRenameItem()
    {
        try
        {
            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItem();

            // find parent item
            TreeItem<InventoryTreeData> oldParentItem = selected.getParent();
            IFFContainer oldParentContainer;
            if (oldParentItem.getValue().getType().equals(InventoryTreeData.Type.ROOT))
                oldParentContainer = this.archive.getInventory();
            else
                oldParentContainer = (IFFContainer) this.archive.getInventory().search(
                        oldParentItem.getValue().getUuid());

            // get new file name
            String userInputPath = QuickDialogs.input("Enter a new file name:", selected.getValue().getName());
            if (userInputPath == null) return;

            String traversalPathComponent = "";
            String newNameComponent = userInputPath;
            if (userInputPath.contains("/") && InventoryPather.isValidRelativePath(userInputPath))
            {
                traversalPathComponent = InventoryPather.dirname(newNameComponent);
                newNameComponent = InventoryPather.baseName(newNameComponent);
            }
            else if (userInputPath.contains("/") && InventoryPather.isValidPath(userInputPath))
            {
                traversalPathComponent = InventoryPather.dirname(newNameComponent);
                newNameComponent = InventoryPather.baseName(newNameComponent);
            }
            else if (! InventoryPather.isValidName(newNameComponent))
            {
                QuickDialogs.error("Rename Error", "'%s' is an invalid file name, relative file path, or absolute file path.", newNameComponent);
                return;
            }

            // get subject item that we can rename
            IFFTraversalTarget renameSubject = oldParentContainer.findFileOrFolder(selected.getValue().getName());
            if (renameSubject == null)
            {
                QuickDialogs.error("Rename Error", "Critical! No subject item.");
                return;
            }

            String oldParentPathString = this.tree.getPathForTreeItem(oldParentItem);
            String newParentPathString = (traversalPathComponent.startsWith("/")) ? traversalPathComponent : InventoryPather.applyRelativePath(oldParentPathString, traversalPathComponent);

            IFFContainer newParentContainer = oldParentContainer;
            TreeItem<InventoryTreeData> newParentItem = oldParentItem;
            if (!newParentPathString.equals(oldParentPathString))
            {
                IFFTraversalTarget pt = InventoryPather.traverse(this.archive.getInventory(), newParentPathString);
                if (pt.isAFile())
                {
                    QuickDialogs.error("Rename Error", "Cannot move folder to be a child of file '%s'.", InventoryPather.baseName(newParentPathString));
                    return;
                }
                newParentContainer = (IFFContainer) pt;
                newParentItem = this.tree.traverseTo(newParentPathString);
            }

            // check parent for the same name
            IFFTraversalTarget target = newParentContainer.findFileOrFolder(newNameComponent);
            if (target != null)
            {
                QuickDialogs.error("Rename Error", "There is already an item named '%s' in the parent folder.", newNameComponent);
                return;
            }

            // rename the subject
            if (renameSubject.isAFolder())
            {
                ((FolderInventoryItem) renameSubject).setName(newNameComponent);
            }
            else if (renameSubject.isAFile())
            {
                ((FileInventoryItem) renameSubject).setName(newNameComponent);
            }
            else
            {
                QuickDialogs.error("Rename Error", "Critical! cannot rename a root.");
                return;
            }

            if (newParentContainer != oldParentContainer)
            {
                if (renameSubject.isAFolder() && renameSubject instanceof FolderInventoryItem)
                {
                    oldParentContainer.removeFolder((FolderInventoryItem) renameSubject);
                    newParentContainer.addFolder((FolderInventoryItem) renameSubject);
                }
                else if (renameSubject.isAFile() && renameSubject instanceof FileInventoryItem)
                {
                    oldParentContainer.removeFile((FileInventoryItem) renameSubject);
                    newParentContainer.addFile((FileInventoryItem) renameSubject);
                }
            }
            if (oldParentItem != newParentItem)
            {
                oldParentItem.getChildren().remove(selected);
            }
            selected.getValue().setName(newNameComponent);
            if (oldParentItem != newParentItem)
            {
                newParentItem.getChildren().add(selected);
            }
            newParentItem.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
            Event.fireEvent(selected, new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected,
                                                                           selected.getValue()));
            this.tree.getSelectionModel().select(selected);
            this.requestMetadataSave(String.format("Renamed file %s", newNameComponent));
            if (renameSubject.isAFile() && renameSubject instanceof FileInventoryItem)
            {
                FileInventoryItem renameFile = (FileInventoryItem) renameSubject;
                if (openTabs.containsKey(renameFile.getUuid()))
                {
                    this.openTabs.get(renameFile.getUuid()).notifyRename();
                }
            }
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }

    private void handleCMNewSubDir()
    {
        try
        {
            // get new file name
            String newName = QuickDialogs.input("Enter a new directory name:", "");
            if (newName == null) return;
            if (! InventoryPather.isValidName(newName))
            {
                QuickDialogs.error("Create Error", "'%s' is an invalid file name.", newName);
                return;
            }

            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItem();
            String selectedPath = this.tree.getPathForTreeItem(selected);
            IFFTraversalTarget selectedItem = InventoryPather.traverse(this.archive.getInventory(), selectedPath);
            if (selectedItem.isAFile())
            {
                QuickDialogs.error("Create Error", "'%s' is a file.", selectedPath);
                return;
            }

            // find subject FolderInventoryItem
            IFFContainer selectedContainer = (IFFContainer) selectedItem;

            // check parent for the same name
            IFFTraversalTarget target = selectedContainer.findFileOrFolder(newName);
            if (target != null)
            {
                QuickDialogs.error("Create Error", "There is already an item named '%s' in the parent folder.", newName);
                return;
            }

            FolderInventoryItem newFolder = new FolderInventoryItem(newName);
            selectedContainer.addFolder(newFolder);

            // create the new tree item
            InventoryTreeData newValue = new InventoryTreeData(newFolder);
            TreeItem<InventoryTreeData> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);
            selected.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));

            Event.fireEvent(selected,
                            new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
            this.tree.getSelectionModel().select(newItem);
            this.requestMetadataSave(String.format("Created new directory %s", newFolder.getName()));
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }

    private void handleCMNewFile()
    {
        try
        {
            // get new file name
            String newName = QuickDialogs.input("Enter a new file name:", "");
            if (newName == null) return;
            if (! InventoryPather.isValidName(newName))
            {
                QuickDialogs.error("Create Error", "'%s' is an invalid file name.", newName);
                return;
            }

            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItem();
            String selectedPath = this.tree.getPathForTreeItem(selected);
            IFFTraversalTarget selectedItem = InventoryPather.traverse(this.archive.getInventory(), selectedPath);
            if (selectedItem.isAFile())
            {
                QuickDialogs.error("Create Error", "'%s' is a file.", selectedPath);
                return;
            }

            // find subject FolderInventoryItem
            IFFContainer selectedContainer = (IFFContainer) selectedItem;

            // check parent for the same name
            IFFTraversalTarget target = selectedContainer.findFileOrFolder(newName);
            if (target != null)
            {
                QuickDialogs.error("Create Error", "There is already an item named '%s' in the parent folder.", newName);
                return;
            }

            FileInventoryItem newFile = new FileInventoryItem(newName);
            selectedContainer.addFile(newFile);

            // create the new tree item
            InventoryTreeData newValue = new InventoryTreeData(newFile);
            TreeItem<InventoryTreeData> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);
            selected.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
            selected.setExpanded(true);

            Event.fireEvent(selected,
                            new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
            this.tree.getSelectionModel().select(newItem);
            this.requestMetadataSave(String.format("Created new file %s", newFile.getName()));
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }

    private void handleCMFileInfo()
    {
        try
        {
            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItem();
            String selectedPath = this.tree.getPathForTreeItem(selected);

            IFFTraversalTarget selectedFile = InventoryPather.traverse(this.archive.getInventory(), selectedPath);
            if (selectedFile.isAFile() && selectedFile instanceof FileInventoryItem)
            {
                FileInventoryItem fileItem = (FileInventoryItem) selectedFile;
                FileInfoWindow popup = new FileInfoWindow(fileItem);
                popup.setOnRefreshTreeItem(e -> selected.setValue(new InventoryTreeData(fileItem)));
                popup.setOnSaveInventoryRequest(this::requestMetadataSave);
                popup.getStage().showAndWait();
            }
        }
        catch (BaseBunkrException | IOException e)
        {
            QuickDialogs.exception(e);
        }
    }

    private void handleCMImportFile()
    {
        try
        {
            // first choose file to be imported
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File ...");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
            File importedFile = fileChooser.showOpenDialog(this.tree.getScene().getWindow());
            if (importedFile == null) return;

            // get new file name
            String newName = QuickDialogs.input("Enter a file name:", importedFile.getName());
            if (newName == null) return;
            if (! InventoryPather.isValidName(newName))
            {
                QuickDialogs.error("Import Error", null, "'%s' is an invalid file name.", newName);
                return;
            }

            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItem();
            String selectedPath = this.tree.getPathForTreeItem(selected);
            IFFTraversalTarget selectedItem = InventoryPather.traverse(this.archive.getInventory(), selectedPath);
            if (selectedItem.isAFile())
            {
                QuickDialogs.error("Create Error", null, "'%s' is a file.", selectedPath);
                return;
            }

            // find subject FolderInventoryItem
            IFFContainer selectedContainer = (IFFContainer) selectedItem;

            // check parent for the same name
            IFFTraversalTarget target = selectedContainer.findFileOrFolder(newName);
            if (target != null)
            {
                QuickDialogs.error("Import Error", null, "There is already an item named '%s' in the parent folder.", newName);
                return;
            }

            FileInventoryItem newFile = new FileInventoryItem(newName);

            ProgressTask<Void> progressTask = new ProgressTask<Void>()
            {
                @Override
                protected Void innerCall() throws Exception
                {
                    this.updateMessage("Opening file.");
                    FileChannel fc = new RandomAccessFile(importedFile, "r").getChannel();
                    long bytesTotal = fc.size();
                    long bytesDone = 0;
                    try (InputStream fis = Channels.newInputStream(fc))
                    {
                        try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(archive, newFile))
                        {
                            this.updateMessage("Importing bytes...");
                            byte[] buffer = new byte[1024 * 1024];
                            int n;
                            while ((n = fis.read(buffer)) != -1)
                            {
                                bwos.write(buffer, 0, n);
                                bytesDone += n;
                                this.updateProgress(bytesDone, bytesTotal);
                            }
                            Arrays.fill(buffer, (byte) 0);
                            this.updateMessage("Finished.");
                        }
                    }
                    return null;
                }

                @Override
                protected void failed()
                {
                    QuickDialogs.exception(this.getException());
                }

                @Override
                protected void succeeded()
                {
                    // add to the container
                    selectedContainer.addFile(newFile);

                    // pick the media type
                    newFile.setMediaType(QuickDialogs.pick(
                                                 "Import File",
                                                 null,
                                                 "Pick a Media Type for the new file:",
                                                 new ArrayList<>(MediaType.ALL_TYPES), MediaType.UNKNOWN)
                    );

                    // create the new tree item
                    InventoryTreeData newValue = new InventoryTreeData(newFile);
                    TreeItem<InventoryTreeData> newItem = new TreeItem<>(newValue);
                    selected.getChildren().add(newItem);
                    selected.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
                    selected.setExpanded(true);

                    Event.fireEvent(selected,
                                    new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
                    tree.getSelectionModel().select(newItem);
                    requestMetadataSave(String.format("Imported file %s", newFile.getName()));
                }
            };

            ProgressDialog pd = new ProgressDialog(progressTask);
            pd.setHeaderText(String.format("Importing file %s ...", newFile.getName()));
            new Thread(progressTask).start();
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }

    private void handleCMFileExport()
    {
        try
        {
            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.tree.getSelectedTreeItem();
            String selectedPath = this.tree.getPathForTreeItem(selected);

            IFFTraversalTarget selectedItem = InventoryPather.traverse(this.archive.getInventory(), selectedPath);

            // fail if not a file
            if (! (selectedItem instanceof FileInventoryItem))
            {
                QuickDialogs.error("%s is not a file inventory item.", selectedPath);
                return;
            }

            FileInventoryItem selectedFile = (FileInventoryItem) selectedItem;

            // choose file to be exported
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Export Location ...");
            fileChooser.setInitialFileName(selectedFile.getName());
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));

            if (selectedFile.getName().contains(".") && selectedFile.getName().lastIndexOf('.') > 0)
            {
                String foundExtension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.'));
                if (foundExtension.length() > 0)
                {
                    FileChooser.ExtensionFilter o = new FileChooser.ExtensionFilter("Original Extension", String.format("*.%s", foundExtension));
                    fileChooser.getExtensionFilters().add(o);
                    fileChooser.setSelectedExtensionFilter(o);
                }
            }

            File exportedFile = fileChooser.showSaveDialog(this.tree.getScene().getWindow());
            if (exportedFile == null) return;

            ProgressTask<Void> progressTask = new ProgressTask<Void>()
            {
                @Override
                protected Void innerCall() throws Exception
                {
                    this.updateMessage("Opening file.");
                    FileChannel fc = new RandomAccessFile(exportedFile, "rw").getChannel();
                    long bytesTotal = selectedFile.getActualSize();
                    long bytesDone = 0;
                    try (OutputStream contentOutputStream = Channels.newOutputStream(fc))
                    {
                        try (MultilayeredInputStream ms = new MultilayeredInputStream(archive, selectedFile))
                        {
                            this.updateMessage("Exporting bytes...");
                            byte[] buffer = new byte[1024 * 1024];
                            int n;
                            while ((n = ms.read(buffer)) != -1)
                            {
                                contentOutputStream.write(buffer, 0, n);
                                bytesDone += n;
                                this.updateProgress(bytesDone, bytesTotal);
                            }
                            Arrays.fill(buffer, (byte) 0);
                            this.updateMessage("Finished.");
                        }
                    }
                    return null;
                }

                @Override
                protected void succeeded()
                {
                    QuickDialogs.info("File successfully exported to %s", exportedFile.getAbsolutePath());
                }

                @Override
                protected void failed()
                {
                    QuickDialogs.exception(this.getException());
                }
            };

            ProgressDialog pd = new ProgressDialog(progressTask);
            pd.setHeaderText(String.format("Exporting file %s ...", exportedFile.getName()));
            new Thread(progressTask).start();
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }
}
