package org.bunkr.gui.controllers;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.inventory.*;
import org.bunkr.gui.components.treeview.CellFactoryCallback;
import org.bunkr.gui.components.treeview.InventoryTreeData;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.dialogs.FileInfoDialog;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Creator: benmeier
 * Created At: 2015-12-28
 */
public class InventoryCMController
{
    public static final String STR_NEW_FILE = "New File";
    public static final String STR_NEW_FOLDER = "New Folder";
    public static final String STR_DELETE = "Delete";
    public static final String STR_RENAME = "Rename";
    public static final String STR_INFO = "Info";
    public static final String STR_IMPORT_FILE = "Import File";
    public static final String STR_OPEN = "Open";
    public static final String STR_EXPORT = "Export File";

    public final ContextMenu dirContextMenu, fileContextMenu, rootContextMenu;
    public final MenuItem rootNewFile, rootNewSubDir, rootImportFile,
            dirNewFile, dirNewSubDir, dirImportFile, dirDelete, dirRename,
            fileDelete, fileRename, fileInfo, fileOpen, fileExport;

    private final Inventory inventory;
    private final InventoryTreeView treeView;

    private Consumer<String> onSaveInventoryRequest;
    private Consumer<FileInventoryItem> onOpenFile;
    private Consumer<FileInventoryItem> onDeleteFile;
    private Consumer<FileInventoryItem> onRenameFile;

    public InventoryCMController(Inventory inventory, InventoryTreeView treeView)
    {
        this.inventory = inventory;
        this.treeView = treeView;

        // root
        this.rootNewFile = new MenuItem(STR_NEW_FILE);
        this.rootNewSubDir = new MenuItem(STR_NEW_FOLDER);
        this.rootImportFile = new MenuItem(STR_IMPORT_FILE);

        // file
        this.fileDelete = new MenuItem(STR_DELETE);
        this.fileRename = new MenuItem(STR_RENAME);
        this.fileInfo = new MenuItem(STR_INFO);
        this.fileOpen = new MenuItem(STR_OPEN);
        this.fileExport = new MenuItem(STR_EXPORT);

        // dir
        this.dirDelete = new MenuItem(STR_DELETE);
        this.dirRename = new MenuItem(STR_RENAME);
        this.dirNewFile = new MenuItem(STR_NEW_FILE);
        this.dirNewSubDir = new MenuItem(STR_NEW_FOLDER);
        this.dirImportFile = new MenuItem(STR_IMPORT_FILE);

        this.dirContextMenu = new ContextMenu(this.dirNewFile, this.dirNewSubDir, this.dirRename, this.dirDelete, this.dirImportFile);
        this.fileContextMenu = new ContextMenu(this.fileOpen, this.fileInfo, this.fileExport, this.fileRename, this.fileDelete);
        this.rootContextMenu = new ContextMenu(this.rootNewFile, this.rootNewSubDir, this.rootImportFile);

        this.treeView.setCellFactory(new CellFactoryCallback(this));
    }

    public void bindEvents()
    {
        this.fileOpen.setOnAction(event -> this.handleCMFileOpen());
        this.fileRename.setOnAction(event -> this.handleCMRenameItem());
        this.fileDelete.setOnAction(event -> this.handleCMFileDelete());
        this.fileInfo.setOnAction(event -> this.handleCMFileInfo());

        this.dirDelete.setOnAction(event -> this.handleCMDirDelete());
        this.dirRename.setOnAction(event -> this.handleCMRenameItem());
        this.dirNewFile.setOnAction(event -> this.handleCMNewFileOnDir());
        this.dirNewSubDir.setOnAction(event -> this.handleCMNewSubDirOnDir());

        this.rootNewSubDir.setOnAction(event -> this.handleCMNewSubDirOnRoot());
        this.rootNewFile.setOnAction(event -> this.handleCMNewFileOnRoot());

        this.treeView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2)
            {
                this.handleCMFileOpen();
            }
        });
    }

    private void handleCMFileOpen()
    {
        try
        {
            // get selected tree item
            TreeItem<InventoryTreeData> selected = this.treeView.getSelectedTreeItem();
            // make sure it looks like a file
            if (selected.getValue().getType() != InventoryTreeData.Type.FILE)
            {
                throw new BaseBunkrException("Failed to open item %s. It is not a file.", selected.getValue().getName());
            }

            // get absolute file path
            String selectedPath = this.treeView.getPathForTreeItem(selected);

            // traverse down to correct file item
            IFFTraversalTarget selectedItem = InventoryPather.traverse(this.inventory, selectedPath);
            // double check that its a file
            if (! selectedItem.isAFile())
            {
                throw new BaseBunkrException("Failed to open item %s. It is not a file.", selected.getValue().getName());
            }

            this.onOpenFile.accept((FileInventoryItem) selectedItem);
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
            TreeItem<InventoryTreeData> selected = this.treeView.getSelectedTreeItem();

            if (!QuickDialogs.confirm(
                    String.format("Are you sure you want to delete '%s'?", selected.getValue().getName())))
                return;

            // find parent item
            TreeItem<InventoryTreeData> parent = selected.getParent();
            String parentPath = this.treeView.getPathForTreeItem(parent);

            // find inventory item
            IFFContainer parentContainer = (IFFContainer) InventoryPather.traverse(this.inventory, parentPath);

            // just get inventory item
            IFFTraversalTarget target = parentContainer.findFileOrFolder(selected.getValue().getName());
            if (target instanceof FileInventoryItem)
            {
                FileInventoryItem targetFile = (FileInventoryItem) target;
                parentContainer.removeFile(targetFile);
                parent.getChildren().remove(selected);

                this.onSaveInventoryRequest.accept(
                        String.format("Deleted file %s from %s", selected.getValue().getName(), parentPath));

                this.onDeleteFile.accept(targetFile);
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
            TreeItem<InventoryTreeData> selected = this.treeView.getSelectedTreeItem();

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
                parentContainer = this.inventory;
            }
            else
            {
                parentContainer = (IFFContainer) this.inventory.search(parent.getValue().getUuid());
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
            this.onSaveInventoryRequest.accept(String.format("Deleted directory %s from %s", selected.getValue().getName(),
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
            TreeItem<InventoryTreeData> selected = this.treeView.getSelectedTreeItem();

            // find parent item
            TreeItem<InventoryTreeData> oldParentItem = selected.getParent();
            IFFContainer oldParentContainer;
            if (oldParentItem.getValue().getType().equals(InventoryTreeData.Type.ROOT))
                oldParentContainer = this.inventory;
            else
                oldParentContainer = (IFFContainer) this.inventory.search(oldParentItem.getValue().getUuid());

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

            String oldParentPathString = this.treeView.getPathForTreeItem(oldParentItem);
            String newParentPathString = (traversalPathComponent.startsWith("/")) ? traversalPathComponent : InventoryPather.applyRelativePath(oldParentPathString, traversalPathComponent);

            IFFContainer newParentContainer = oldParentContainer;
            TreeItem<InventoryTreeData> newParentItem = oldParentItem;
            if (!newParentPathString.equals(oldParentPathString))
            {
                IFFTraversalTarget pt = InventoryPather.traverse(this.inventory, newParentPathString);
                if (pt.isAFile())
                {
                    QuickDialogs.error("Rename Error", "Cannot move folder to be a child of file '%s'.", InventoryPather.baseName(newParentPathString));
                    return;
                }
                newParentContainer = (IFFContainer) pt;
                newParentItem = this.treeView.traverseTo(newParentPathString);
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
            InventoryTreeData newValue = new InventoryTreeData(selected.getValue().getUuid(), newNameComponent, selected.getValue().getType());
            selected.setValue(newValue);
            if (oldParentItem != newParentItem)
            {
                newParentItem.getChildren().add(selected);
            }
            newParentItem.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
            Event.fireEvent(selected,
                            new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
            this.treeView.getSelectionModel().select(selected);
            this.onSaveInventoryRequest.accept(String.format("Renamed file %s", newNameComponent));
            if (renameSubject.isAFile() && renameSubject instanceof FileInventoryItem)
            {
                this.onRenameFile.accept((FileInventoryItem) renameSubject);
            }
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }

    private void handleCMNewSubDirOnDir()
    {
        try
        {
            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.treeView.getSelectedTreeItem();

            // get new file name
            String newName = QuickDialogs.input("Enter a new directory name:", "");
            if (newName == null) return;
            if (! InventoryPather.isValidName(newName))
            {
                QuickDialogs.error("Create Error", "'%s' is an invalid file name.", newName);
                return;
            }

            // find subject FolderInventoryItem
            IFFContainer subjectContainer = (IFFContainer) this.inventory.search(selected.getValue().getUuid());

            // check parent for the same name
            IFFTraversalTarget target = subjectContainer.findFileOrFolder(newName);
            if (target != null)
            {
                QuickDialogs.error("Create Error", "There is already an item named '%s' in the parent folder.", newName);
                return;
            }

            FolderInventoryItem newFolder = new FolderInventoryItem(newName);
            subjectContainer.addFolder(newFolder);

            // create the new tree item
            InventoryTreeData
                    newValue = new InventoryTreeData(newFolder.getUuid(), newFolder.getName(), InventoryTreeData.Type.FOLDER);
            TreeItem<InventoryTreeData> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);
            selected.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));

            Event.fireEvent(selected,
                            new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
            this.treeView.getSelectionModel().select(newItem);
            this.onSaveInventoryRequest.accept(String.format("Created new directory %s", newFolder.getName()));
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }

    private void handleCMNewSubDirOnRoot()
    {
        try
        {
            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.treeView.getRoot();

            // get new file name
            String newName = QuickDialogs.input("Enter a new directory name:", "");
            if (newName == null) return;
            if (! InventoryPather.isValidName(newName))
            {
                QuickDialogs.error("Create Error", "'%s' is an invalid file name.", newName);
                return;
            }

            // find subject FolderInventoryItem
            IFFContainer subjectContainer = this.inventory;

            // check parent for the same name
            IFFTraversalTarget target = subjectContainer.findFileOrFolder(newName);
            if (target != null)
            {
                QuickDialogs.error("Create Error", "There is already an item named '%s' in the parent folder.", newName);
                return;
            }

            FolderInventoryItem newFolder = new FolderInventoryItem(newName);
            subjectContainer.addFolder(newFolder);

            // create the new tree item
            InventoryTreeData
                    newValue = new InventoryTreeData(newFolder.getUuid(), newFolder.getName(), InventoryTreeData.Type.FOLDER);
            TreeItem<InventoryTreeData> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);
            selected.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
            selected.setExpanded(true);

            Event.fireEvent(selected,
                            new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
            this.treeView.getSelectionModel().select(newItem);
            this.onSaveInventoryRequest.accept(String.format("Created new directory %s", newFolder.getName()));
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }

    private void handleCMNewFileOnDir()
    {
        try
        {
            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.treeView.getSelectedTreeItem();

            // get new file name
            String newName = QuickDialogs.input("Enter a new file name:", "");
            if (newName == null) return;
            if (! InventoryPather.isValidName(newName))
            {
                QuickDialogs.error("Create Error", "'%s' is an invalid file name.", newName);
                return;
            }

            // find subject FolderInventoryItem
            IFFContainer subjectContainer = (IFFContainer) this.inventory.search(selected.getValue().getUuid());

            // check parent for the same name
            IFFTraversalTarget target = subjectContainer.findFileOrFolder(newName);
            if (target != null)
            {
                QuickDialogs.error("Create Error", "There is already an item named '%s' in the parent folder.", newName);
                return;
            }

            FileInventoryItem newFile = new FileInventoryItem(newName);
            subjectContainer.addFile(newFile);

            // create the new tree item
            InventoryTreeData newValue = new InventoryTreeData(newFile.getUuid(), newFile.getName(), InventoryTreeData.Type.FILE);
            TreeItem<InventoryTreeData> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);
            selected.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
            selected.setExpanded(true);

            Event.fireEvent(selected,
                            new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
            this.treeView.getSelectionModel().select(newItem);
            this.onSaveInventoryRequest.accept(String.format("Created new file %s", newFile.getName()));
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }

    private void handleCMNewFileOnRoot()
    {
        try
        {
            // get item for which the context menu was called from
            TreeItem<InventoryTreeData> selected = this.treeView.getRoot();

            // get new file name
            String newName = QuickDialogs.input("Enter a new file name:", "");
            if (newName == null) return;
            if (! InventoryPather.isValidName(newName))
            {
                QuickDialogs.error("Create Error", "'%s' is an invalid file name.", newName);
                return;
            }

            // find subject FolderInventoryItem
            IFFContainer subjectContainer = this.inventory;

            // check parent for the same name
            IFFTraversalTarget target = subjectContainer.findFileOrFolder(newName);
            if (target != null)
            {
                QuickDialogs.error("Create Error", "There is already an item named '%s' in the parent folder.", newName);
                return;
            }

            FileInventoryItem newFile = new FileInventoryItem(newName);
            subjectContainer.addFile(newFile);

            // create the new tree item
            InventoryTreeData newValue = new InventoryTreeData(newFile.getUuid(), newFile.getName(), InventoryTreeData.Type.FILE);
            TreeItem<InventoryTreeData> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);
            selected.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
            selected.setExpanded(true);

            Event.fireEvent(selected,
                            new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
            this.treeView.getSelectionModel().select(newItem);
            this.onSaveInventoryRequest.accept(String.format("Created new file %s", newFile.getName()));
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
            TreeItem<InventoryTreeData> selected = this.treeView.getSelectedTreeItem();
            String selectedPath = this.treeView.getPathForTreeItem(selected);

            IFFTraversalTarget selectedFile = InventoryPather.traverse(this.inventory, selectedPath);
            if (selectedFile.isAFile() && selectedFile instanceof FileInventoryItem)
            {
                FileInventoryItem fileItem = (FileInventoryItem) selectedFile;
                new FileInfoDialog(fileItem).getStage().showAndWait();
            }
        }
        catch (BaseBunkrException | IOException e)
        {
            QuickDialogs.exception(e);
        }
    }

    public void setOnSaveInventoryRequest(Consumer<String> eventHandler)
    {
        this.onSaveInventoryRequest = eventHandler;
    }

    public void setOnOpenFile(Consumer<FileInventoryItem> eventHandler)
    {
        this.onOpenFile = eventHandler;
    }

    public void setOnDeleteFile(Consumer<FileInventoryItem> onDeleteFile)
    {
        this.onDeleteFile = onDeleteFile;
    }

    public void setOnRenameFile(Consumer<FileInventoryItem> onRenameFile)
    {
        this.onRenameFile = onRenameFile;
    }
}
