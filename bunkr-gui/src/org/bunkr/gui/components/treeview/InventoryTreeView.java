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

package org.bunkr.gui.components.treeview;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.exceptions.TraversalException;
import org.bunkr.core.inventory.*;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

/**
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
        TreeItem<InventoryTreeData> root = new TreeItem<>(InventoryTreeData.makeRoot());
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
            parent.getChildren().add(new TreeItem<>(new InventoryTreeData(subfile)));
        }

        parent.getChildren().sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));
        return parent;
    }
    /**
     * Create the tree item that represents an inventory folder
     */
    private TreeItem<InventoryTreeData> genFolder(FolderInventoryItem folder)
    {
        return genFolder(new TreeItem<>(new InventoryTreeData(folder)), folder);
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

    public TreeItem<InventoryTreeData> search(UUID uuid) throws TraversalException
    {
        if (uuid == null) return getRoot();
        final Queue<TreeItem<InventoryTreeData>> queue = new ArrayDeque<>();
        queue.add(this.getRoot());

        while (! queue.isEmpty())
        {
            TreeItem<InventoryTreeData> item = queue.poll();
            if (item.getValue().getUuid() == uuid) return item;
            queue.addAll(item.getChildren());
        }
        throw new TraversalException(String.format("Could not find TreeItem with uuid: %s", uuid));
    }

    public TreeItem<InventoryTreeData> getSelectedTreeItem() throws BaseBunkrException
    {
        if (! this.getSelectionModel().isEmpty()) return this.getSelectionModel().getSelectedItem();
        throw new BaseBunkrException("No item selected");
    }

    public TreeItem<InventoryTreeData> getSelectedTreeItemOrNull()
    {
        if (! this.getSelectionModel().isEmpty()) return this.getSelectionModel().getSelectedItem();
        return null;
    }

    public TreeItem<InventoryTreeData> getSelectedTreeItemOrRoot()
    {
        if (! this.getSelectionModel().isEmpty()) return this.getSelectionModel().getSelectedItem();
        return this.getRoot();
    }
}
