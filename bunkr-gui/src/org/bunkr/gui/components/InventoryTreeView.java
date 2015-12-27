package org.bunkr.gui.components;

import javafx.event.Event;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.FolderInventoryItem;
import org.bunkr.core.inventory.IFFContainer;
import org.bunkr.core.inventory.InventoryItem;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-12-27
 */
public class InventoryTreeView extends TreeView<IntermedInvTreeDS>
{
    private ArchiveInfoContext archive;

    public InventoryTreeView(ArchiveInfoContext archive)
    {
        this.archive = archive;
        this.setCellFactory(new CellFactoryCallback(new ContextMenus()));
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
}
