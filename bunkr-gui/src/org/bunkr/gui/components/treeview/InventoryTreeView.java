package org.bunkr.gui.components.treeview;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.exceptions.TraversalException;
import org.bunkr.core.inventory.*;

/**
 * Creator: benmeier
 * Created At: 2015-12-27
 */
public class InventoryTreeView extends TreeView<InventoryTreeData>
{
    private final ArchiveInfoContext archive;

    public InventoryTreeView(ArchiveInfoContext archive)
    {
        this.archive = archive;
    }

    /**
     * Rebuild the tree of TreeItems based on the Inventory.
     * This will reset all expanded items!
     */
    public void refreshAll()
    {
        TreeItem<InventoryTreeData> root = new TreeItem<>(new InventoryTreeData(null, "/", InventoryTreeData.Type.ROOT));
        genFolder(root, this.archive.getInventory());
        root.setExpanded(true);
        this.setRoot(root);
    }

    /**
     * Rebuild all the nodes beneath the node 'parent' by mirroring the data held by 'mirror'
     */
    private TreeItem<InventoryTreeData> genFolder(TreeItem<InventoryTreeData> parent, IFFContainer mirror)
    {
        for (FolderInventoryItem subfolder : mirror.getFolders())
        {
            parent.getChildren().add(genFolder(subfolder));
        }
        for (FileInventoryItem subfile : mirror.getFiles())
        {
            parent.getChildren().add(new TreeItem<>(new InventoryTreeData(
                    subfile.getUuid(), subfile.getName(), InventoryTreeData.Type.FILE
            )));
        }

        parent.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        return parent;
    }
    /**
     * Create the tree item that represents an inventory folder
     */
    private TreeItem<InventoryTreeData> genFolder(FolderInventoryItem folder)
    {
        return genFolder(new TreeItem<>(new InventoryTreeData(
                folder.getUuid(), folder.getName(), InventoryTreeData.Type.FOLDER)), folder
        );
    }

    public String getPathForTreeItem(TreeItem<InventoryTreeData> o)
    {
        if (o == this.getRoot()) return "/";
        String path = "";
        while (o != this.getRoot())
        {
            path = "/" + o.getValue().getName() + path;
            o = o.getParent();
        }
        return path;
    }

    public TreeItem<InventoryTreeData> traverseTo(String absolutePath) throws TraversalException
    {
        InventoryPather.assertValidPath(absolutePath);
        if (absolutePath.equals("/")) return this.getRoot();
        TreeItem<InventoryTreeData> current = this.getRoot();
        String[] parts = absolutePath.substring(1).split("/");

        for (String part : parts)
        {
            boolean found = false;
            for (TreeItem<InventoryTreeData> child : current.getChildren())
            {
                if (child.getValue().getName().equals(part))
                {
                    current = child;
                    found = true;
                    break;
                }
            }
            if (! found)
            {
                throw new TraversalException("Could not find item '%s' in '%s'", part, current.getValue().getName());
            }
        }

        return current;
    }

    public TreeItem<InventoryTreeData> getSelectedTreeItem() throws BaseBunkrException
    {
        if (! this.getSelectionModel().isEmpty()) return this.getSelectionModel().getSelectedItem();
        throw new BaseBunkrException("No item selected");
    }
}
