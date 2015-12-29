package org.bunkr.gui.controllers;

import javafx.event.Event;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.inventory.*;
import org.bunkr.gui.components.treeview.CellFactoryCallback;
import org.bunkr.gui.components.treeview.InventoryTreeData;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.dialogs.FileInfoDialog;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;

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

    public final ContextMenu dirContextMenu, fileContextMenu, rootContextMenu;
    public final MenuItem rootNewFile, rootNewSubDir, dirNewFile, dirNewSubDir, dirDelete, dirRename, fileDelete, fileRename, fileInfo;

    private final Inventory inventory;
    private final InventoryTreeView treeView;

    public InventoryCMController(Inventory inventory, InventoryTreeView treeView)
    {
        this.inventory = inventory;
        this.treeView = treeView;

        // root
        this.rootNewFile = new MenuItem(STR_NEW_FILE);
        this.rootNewSubDir = new MenuItem(STR_NEW_FOLDER);

        // file
        this.fileDelete = new MenuItem(STR_DELETE);
        this.fileRename = new MenuItem(STR_RENAME);
        this.fileInfo = new MenuItem(STR_INFO);

        // dir
        this.dirDelete = new MenuItem(STR_DELETE);
        this.dirRename = new MenuItem(STR_RENAME);
        this.dirNewFile = new MenuItem(STR_NEW_FILE);
        this.dirNewSubDir = new MenuItem(STR_NEW_FOLDER);

        this.dirContextMenu = new ContextMenu(this.dirNewFile, this.dirNewSubDir, this.dirRename, this.dirDelete);
        this.fileContextMenu = new ContextMenu(this.fileInfo, this.fileRename, this.fileDelete);
        this.rootContextMenu = new ContextMenu(this.rootNewFile, this.rootNewSubDir);

        this.treeView.setCellFactory(new CellFactoryCallback(this));
    }

    public void bindEvents()
    {
        this.fileRename.setOnAction(event -> this.handleCMRenameItem());
        this.fileDelete.setOnAction(event -> this.handleCMFileDelete());
        this.fileInfo.setOnAction(event -> this.handleCMFileInfo());

        this.dirDelete.setOnAction(event -> this.handleCMDirDelete());
        this.dirRename.setOnAction(event -> this.handleCMRenameItem());
        this.dirNewFile.setOnAction(event -> this.handleCMNewFileOnDir());
        this.dirNewSubDir.setOnAction(event -> this.handleCMNewSubDirOnDir());

        this.rootNewSubDir.setOnAction(event -> this.handleCMNewSubDirOnRoot());
        this.rootNewFile.setOnAction(event -> this.handleCMNewFileOnRoot());
    }

    private TreeItem<InventoryTreeData> getSelectedTreeItem() throws BaseBunkrException
    {
        if (! this.treeView.getSelectionModel().isEmpty()) return this.treeView.getSelectionModel().getSelectedItem();
        throw new BaseBunkrException("No item selected");
    }

    private void handleCMFileDelete()
    {
        try
        {
            TreeItem<InventoryTreeData> selected = this.getSelectedTreeItem();

            if (!QuickDialogs.confirm(
                    String.format("Are you sure you want to delete '%s'?", selected.getValue().getName())))
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

            if (target instanceof FileInventoryItem)
            {
                FileInventoryItem targetFile = (FileInventoryItem) target;
                parentContainer.getFiles().remove(targetFile);
                parent.getChildren().remove(selected);
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
            TreeItem<InventoryTreeData> selected = this.getSelectedTreeItem();

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
            TreeItem<InventoryTreeData> selected = this.getSelectedTreeItem();

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
                    oldParentContainer.getFolders().remove(renameSubject);
                    newParentContainer.getFolders().add((FolderInventoryItem) renameSubject);
                }
                else if (renameSubject.isAFile() && renameSubject instanceof FileInventoryItem)
                {
                    oldParentContainer.getFiles().remove(renameSubject);
                    newParentContainer.getFiles().add((FileInventoryItem) renameSubject);
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
            TreeItem<InventoryTreeData> selected = this.getSelectedTreeItem();

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
            subjectContainer.getFolders().add(newFolder);

            // create the new tree item
            InventoryTreeData
                    newValue = new InventoryTreeData(newFolder.getUuid(), newFolder.getName(), InventoryTreeData.Type.FOLDER);
            TreeItem<InventoryTreeData> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);
            selected.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));

            Event.fireEvent(selected,
                            new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
            this.treeView.getSelectionModel().select(newItem);
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
            subjectContainer.getFolders().add(newFolder);

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
            TreeItem<InventoryTreeData> selected = this.getSelectedTreeItem();

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
            subjectContainer.getFiles().add(newFile);

            // create the new tree item
            InventoryTreeData newValue = new InventoryTreeData(newFile.getUuid(), newFile.getName(), InventoryTreeData.Type.FILE);
            TreeItem<InventoryTreeData> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);
            selected.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
            selected.setExpanded(true);

            Event.fireEvent(selected,
                            new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
            this.treeView.getSelectionModel().select(newItem);
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
            subjectContainer.getFiles().add(newFile);

            // create the new tree item
            InventoryTreeData newValue = new InventoryTreeData(newFile.getUuid(), newFile.getName(), InventoryTreeData.Type.FILE);
            TreeItem<InventoryTreeData> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);
            selected.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
            selected.setExpanded(true);

            Event.fireEvent(selected,
                            new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
            this.treeView.getSelectionModel().select(newItem);
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
            TreeItem<InventoryTreeData> selected = this.getSelectedTreeItem();
            String selectedPath = this.treeView.getPathForTreeItem(selected);

            IFFTraversalTarget selectedFile = InventoryPather.traverse(this.inventory, selectedPath);
            if (selectedFile.isAFile() && selectedFile instanceof FileInventoryItem)
            {
                FileInventoryItem fileItem = (FileInventoryItem) selectedFile;
                new FileInfoDialog(fileItem, selectedPath).getStage().showAndWait();
            }
        }
        catch (BaseBunkrException | IOException e)
        {
            QuickDialogs.exception(e);
        }
    }
}
