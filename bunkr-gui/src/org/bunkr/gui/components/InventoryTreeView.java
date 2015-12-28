package org.bunkr.gui.components;

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
public class InventoryTreeView extends TreeView<IntermedInvTreeDS>
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
        TreeItem<IntermedInvTreeDS> root = new TreeItem<>(new IntermedInvTreeDS(null, "/", IntermedInvTreeDS.Type.ROOT));
        genFolder(root, this.archive.getInventory());
        root.setExpanded(true);
        this.setRoot(root);
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

    public String getPathForTreeItem(TreeItem<IntermedInvTreeDS> o)
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

    public TreeItem<IntermedInvTreeDS> traverseTo(String absolutePath) throws TraversalException
    {
        InventoryPather.assertValidPath(absolutePath);
        if (absolutePath.equals("/")) return this.getRoot();
        TreeItem<IntermedInvTreeDS> current = this.getRoot();
        String[] parts = absolutePath.substring(1).split("/");

        for (String part : parts)
        {
            boolean found = false;
            for (TreeItem<IntermedInvTreeDS> child : current.getChildren())
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

}
