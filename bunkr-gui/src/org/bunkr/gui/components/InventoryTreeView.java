package org.bunkr.gui.components;

import javafx.event.Event;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.inventory.*;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-12-27
 */
public class InventoryTreeView extends TreeView<IntermedInvTreeDS>
{
    private final ContextMenus callbackMenus;
    private final ArchiveInfoContext archive;

    public InventoryTreeView(ArchiveInfoContext archive)
    {
        this.archive = archive;
        this.callbackMenus = new ContextMenus();
        this.setCellFactory(new CellFactoryCallback(callbackMenus));
        this.bindEvents();
    }

    private void bindEvents()
    {
        this.callbackMenus.fileDelete.setOnAction(event -> this.handleCMFileDelete());
        this.callbackMenus.dirDelete.setOnAction(event -> this.handleCMDirDelete());

        this.callbackMenus.fileRename.setOnAction(event -> this.handleCMRenameItem());
        this.callbackMenus.dirRename.setOnAction(event -> this.handleCMRenameItem());

        this.callbackMenus.dirNewSubDir.setOnAction(event -> this.handleCMNewSubDirOnDir());
        this.callbackMenus.rootNewSubDir.setOnAction(event -> this.handleCMNewSubDirOnRoot());

        this.callbackMenus.dirNewFile.setOnAction(event -> this.handleCMNewFileOnDir());
        this.callbackMenus.rootNewFile.setOnAction(event -> this.handleCMNewFileOnRoot());
    }

    /**
     * Rebuild the tree of TreeItems based on the Inventory.
     * This will reset all expanded items!
     */
    public void refreshAll()
    {
        TreeItem<IntermedInvTreeDS> root = new TreeItem<>(new IntermedInvTreeDS(null, "/", IntermedInvTreeDS.Type.ROOT));
        genFolder(root, this.archive.getInventory());
        root.setExpanded(true);
        this.setRoot(root);
    }

    /**
     * Refresh a particular tree cell if you have the treeItem.
     * This may be expensive so dont use it to refresh more than just a bit of the tree.
     */
    public void refreshSpecific(TreeItem<IntermedInvTreeDS> subject, boolean rebuildChildren) throws BaseBunkrException
    {
        UUID subjectUUID = subject.getValue().getUuid();
        if (subjectUUID == null) throw new BaseBunkrException("Cannot refresh root, to do this, use refreshAll()");
        InventoryItem item = this.archive.getInventory().search(subjectUUID);
        if (item == null) throw new BaseBunkrException("Could not find item for %s", subject.getValue().getUuid());
        if (rebuildChildren && item instanceof FolderInventoryItem)
        {
            subject.getChildren().clear();
            genFolder(subject, (IFFContainer) item);

        }

        IntermedInvTreeDS newValue = new IntermedInvTreeDS(
                item.getUuid(), item.getName(),
                (item instanceof FolderInventoryItem) ? IntermedInvTreeDS.Type.FOLDER : IntermedInvTreeDS.Type.FILE
        );

        Event.fireEvent(subject, new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), subject, newValue));
    }

    /**
     * Rebuild all the nodes beneath the node 'parent' by mirroring the data held by 'mirror'
     */
    private TreeItem<IntermedInvTreeDS> genFolder(TreeItem<IntermedInvTreeDS> parent, IFFContainer mirror)
    {
        for (FolderInventoryItem subfolder : mirror.getFolders())
        {
            parent.getChildren().add(genFolder(subfolder));
        }
        for (FileInventoryItem subfile : mirror.getFiles())
        {
            parent.getChildren().add(new TreeItem<>(new IntermedInvTreeDS(
                    subfile.getUuid(), subfile.getName(), IntermedInvTreeDS.Type.FILE
            )));
        }
        return parent;
    }
    /**
     * Create the tree item that represents an inventory folder
     */
    private TreeItem<IntermedInvTreeDS> genFolder(FolderInventoryItem folder)
    {
        return genFolder(new TreeItem<>(new IntermedInvTreeDS(
                folder.getUuid(), folder.getName(), IntermedInvTreeDS.Type.FOLDER)), folder
        );
    }

    /**
     * Search the treeview for the node identified by this uuid.
     * Hopefully we will need this, otherwise YAGNI.
     */
    public TreeItem<IntermedInvTreeDS> search(UUID uuid)
    {
        final Queue<TreeItem<IntermedInvTreeDS>> queue = new ArrayDeque<>();
        queue.add(this.getRoot());

        while (! queue.isEmpty())
        {
            TreeItem<IntermedInvTreeDS> item = queue.poll();
            if (item.getValue().getUuid().equals(uuid)) return item;
            queue.addAll(item.getChildren());
        }
        return null;
    }

    private TreeItem<IntermedInvTreeDS> getSelectedTreeItem() throws BaseBunkrException
    {
        if (! this.getSelectionModel().isEmpty()) return this.getSelectionModel().getSelectedItem();
        throw new BaseBunkrException("No item selected");
    }


    // ================== Context Menu handlers ====================

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
                parentContainer = this.archive.getInventory();
            }
            else
            {
                parentContainer = (IFFContainer) this.archive.getInventory().search(parent.getValue().getUuid());
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
                parentContainer = this.archive.getInventory();
            else
                parentContainer = (IFFContainer) this.archive.getInventory().search(parent.getValue().getUuid());

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
            Event.fireEvent(selected, new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));

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
                QuickDialogs.error("Rename Error", "'%s' is an invalid file name.", newName);
                return;
            }

            // find subject FolderInventoryItem
            IFFContainer subjectContainer = (IFFContainer) this.archive.getInventory().search(selected.getValue().getUuid());

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
            TreeItem<IntermedInvTreeDS> selected = this.getRoot();

            // get new file name
            String newName = QuickDialogs.input("Enter a new directory name:", "");
            if (newName == null) return;
            if (! InventoryPather.isValidName(newName))
            {
                QuickDialogs.error("Rename Error", "'%s' is an invalid file name.", newName);
                return;
            }

            // find subject FolderInventoryItem
            IFFContainer subjectContainer = this.archive.getInventory();

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
                QuickDialogs.error("Rename Error", "'%s' is an invalid file name.", newName);
                return;
            }

            // find subject FolderInventoryItem
            IFFContainer subjectContainer = (IFFContainer) this.archive.getInventory().search(selected.getValue().getUuid());

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
            IntermedInvTreeDS newValue = new IntermedInvTreeDS(newFile.getUuid(), newFile.getName(), IntermedInvTreeDS.Type.FOLDER);
            TreeItem<IntermedInvTreeDS> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);

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
            TreeItem<IntermedInvTreeDS> selected = this.getRoot();

            // get new file name
            String newName = QuickDialogs.input("Enter a new directory name:", "");
            if (newName == null) return;
            if (! InventoryPather.isValidName(newName))
            {
                QuickDialogs.error("Rename Error", "'%s' is an invalid file name.", newName);
                return;
            }

            // find subject FolderInventoryItem
            IFFContainer subjectContainer = this.archive.getInventory();

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
            IntermedInvTreeDS newValue = new IntermedInvTreeDS(newFile.getUuid(), newFile.getName(), IntermedInvTreeDS.Type.FOLDER);
            TreeItem<IntermedInvTreeDS> newItem = new TreeItem<>(newValue);
            selected.getChildren().add(newItem);

            Event.fireEvent(selected, new TreeItem.TreeModificationEvent<>(TreeItem.valueChangedEvent(), selected, newValue));
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
        }
    }
}
