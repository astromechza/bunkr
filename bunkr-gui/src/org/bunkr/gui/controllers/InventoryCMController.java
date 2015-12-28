package org.bunkr.gui.controllers;

import javafx.event.Event;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.inventory.*;
import org.bunkr.gui.components.CellFactoryCallback;
import org.bunkr.gui.components.IntermedInvTreeDS;
import org.bunkr.gui.components.InventoryTreeView;
import org.bunkr.gui.dialogs.QuickDialogs;

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

    public final ContextMenu dirContextMenu, fileContextMenu, rootContextMenu;
    public final MenuItem rootNewFile, rootNewSubDir, dirNewFile, dirNewSubDir, dirDelete, dirRename, fileDelete, fileRename;

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

        // dir
        this.dirDelete = new MenuItem(STR_DELETE);
        this.dirRename = new MenuItem(STR_RENAME);
        this.dirNewFile = new MenuItem(STR_NEW_FILE);
        this.dirNewSubDir = new MenuItem(STR_NEW_FOLDER);

        this.dirContextMenu = new ContextMenu(this.dirNewFile, this.dirNewSubDir, this.dirRename, this.dirDelete);
        this.fileContextMenu = new ContextMenu(this.fileRename, this.fileDelete);
        this.rootContextMenu = new ContextMenu(this.rootNewFile, this.rootNewSubDir);

        this.treeView.setCellFactory(new CellFactoryCallback(this));
    }

    public void bindEvents()
    {
        this.fileDelete.setOnAction(event -> this.handleCMFileDelete());
        this.dirDelete.setOnAction(event -> this.handleCMDirDelete());

        this.fileRename.setOnAction(event -> this.handleCMRenameItem());
        this.dirRename.setOnAction(event -> this.handleCMRenameItem());

        this.dirNewSubDir.setOnAction(event -> this.handleCMNewSubDirOnDir());
        this.rootNewSubDir.setOnAction(event -> this.handleCMNewSubDirOnRoot());

        this.dirNewFile.setOnAction(event -> this.handleCMNewFileOnDir());
        this.rootNewFile.setOnAction(event -> this.handleCMNewFileOnRoot());
    }


    private TreeItem<IntermedInvTreeDS> getSelectedTreeItem() throws BaseBunkrException
    {
        if (! this.treeView.getSelectionModel().isEmpty()) return this.treeView.getSelectionModel().getSelectedItem();
        throw new BaseBunkrException("No item selected");
    }

    private void handleCMFileDelete()
    {
        try
        {
            TreeItem<IntermedInvTreeDS> selected = this.getSelectedTreeItem();

            if (!QuickDialogs.confirm(
                    String.format("Are you sure you want to delete '%s'?", selected.getValue().getName())))
                return;

            // find parent item
            TreeItem<IntermedInvTreeDS> parent = selected.getParent();

            // find inventory item
            IFFContainer parentContainer;
            if (parent.getValue().getType().equals(IntermedInvTreeDS.Type.ROOT))
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
            TreeItem<IntermedInvTreeDS> selected = this.getSelectedTreeItem();

            if (! QuickDialogs.confirm(
                    String.format("Are you sure you want to delete '%s' and all of its children?",
                                  selected.getValue().getName())))
                return;

            // find parent item
            TreeItem<IntermedInvTreeDS> parent = selected.getParent();

            // find inventory item
            IFFContainer parentContainer;
            if (parent.getValue().getType().equals(IntermedInvTreeDS.Type.ROOT))
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
            TreeItem<IntermedInvTreeDS> selected = this.getSelectedTreeItem();

            // get new file name
            String newName = QuickDialogs.input("Enter a new file name:", selected.getValue().getName());
            if (newName == null) return;
            if (! InventoryPather.isValidName(newName))
            {
                QuickDialogs.error("Rename Error", "'%s' is an invalid file name.", newName);
                return;
            }

            // find parent item
            TreeItem<IntermedInvTreeDS> parent = selected.getParent();
            IFFContainer parentContainer;
            if (parent.getValue().getType().equals(IntermedInvTreeDS.Type.ROOT))
                parentContainer = this.inventory;
            else
                parentContainer = (IFFContainer) this.inventory.search(parent.getValue().getUuid());

            // check parent for the same name
            IFFTraversalTarget target = parentContainer.findFileOrFolder(newName);
            if (target != null)
            {
                QuickDialogs.error("Rename Error", "There is already an item named '%s' in the parent folder.", newName);
                return;
            }

            // get subject item that we can rename
            IFFTraversalTarget subject = parentContainer.findFileOrFolder(selected.getValue().getName());
            if (subject == null)
            {
                QuickDialogs.error("Rename Error", "Critical! no subject item.");
                return;
            }

            // rename the subject
            if (subject.isAFolder())
            {
                ((FolderInventoryItem) subject).setName(newName);
            }
            else if (subject.isAFile())
            {
                ((FileInventoryItem) subject).setName(newName);
            }
            else
            {
                QuickDialogs.error("Rename Error", "Critical! cannot rename a root.");
                return;
            }

            // rename the tree item
            IntermedInvTreeDS newValue = new IntermedInvTreeDS(selected.getValue().getUuid(), newName, selected.getValue().getType());
            selected.setValue(newValue);
            Event.fireEvent(selected,
                            new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
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
            TreeItem<IntermedInvTreeDS> selected = this.getSelectedTreeItem();

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
            IntermedInvTreeDS newValue = new IntermedInvTreeDS(newFolder.getUuid(), newFolder.getName(), IntermedInvTreeDS.Type.FOLDER);
            TreeItem<IntermedInvTreeDS> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);

            Event.fireEvent(selected, new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
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
            TreeItem<IntermedInvTreeDS> selected = this.treeView.getRoot();

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
            IntermedInvTreeDS newValue = new IntermedInvTreeDS(newFolder.getUuid(), newFolder.getName(), IntermedInvTreeDS.Type.FOLDER);
            TreeItem<IntermedInvTreeDS> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);
            selected.setExpanded(true);

            Event.fireEvent(selected, new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
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
            TreeItem<IntermedInvTreeDS> selected = this.getSelectedTreeItem();

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

            FileInventoryItem newFile = new FileInventoryItem(newName);
            subjectContainer.getFiles().add(newFile);

            // create the new tree item
            IntermedInvTreeDS newValue = new IntermedInvTreeDS(newFile.getUuid(), newFile.getName(), IntermedInvTreeDS.Type.FILE);
            TreeItem<IntermedInvTreeDS> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);
            selected.setExpanded(true);

            Event.fireEvent(selected, new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
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
            TreeItem<IntermedInvTreeDS> selected = this.treeView.getRoot();

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

            FileInventoryItem newFile = new FileInventoryItem(newName);
            subjectContainer.getFiles().add(newFile);

            // create the new tree item
            IntermedInvTreeDS newValue = new IntermedInvTreeDS(newFile.getUuid(), newFile.getName(), IntermedInvTreeDS.Type.FILE);
            TreeItem<IntermedInvTreeDS> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);
            selected.setExpanded(true);

            Event.fireEvent(selected, new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }
}
