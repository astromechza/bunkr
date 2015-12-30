package org.bunkr.core.inventory;

import java.util.*;

/**
 * Creator: benmeier
 * Created At: 2015-11-22
 */
public interface IFFContainer
{
    List<FolderInventoryItem> getFolders();
    List<FileInventoryItem> getFiles();

    default Iterator<FileInventoryItem> getIterator()
    {
        return new InventoryIterator(this);
    }

    class InventoryIterator implements Iterator<FileInventoryItem>
    {
        final Queue<FileInventoryItem> queuedFiles = new ArrayDeque<>();
        final Queue<FolderInventoryItem> queuedFolders = new ArrayDeque<>();

        public InventoryIterator(IFFContainer inv)
        {
            queuedFolders.addAll(inv.getFolders());
            queuedFiles.addAll(inv.getFiles());
        }

        private void fillFileQueue()
        {
            while (queuedFiles.isEmpty() && ! queuedFolders.isEmpty())
            {
                FolderInventoryItem f = queuedFolders.poll();
                queuedFiles.addAll(f.getFiles());
                queuedFolders.addAll(f.getFolders());
            }
        }

        @Override
        public boolean hasNext()
        {
            this.fillFileQueue();
            return !queuedFiles.isEmpty();
        }

        @Override
        public FileInventoryItem next()
        {
            this.fillFileQueue();
            return queuedFiles.poll();
        }
    }

    default IFFTraversalTarget findFileOrFolder(String name)
    {
        IFFContainer c = findFolder(name);
        if (c != null) return (IFFTraversalTarget) c;
        return findFile(name);
    }

    default IFFContainer findFolder(String name)
    {
        for (FolderInventoryItem item : this.getFolders())
        {
            if (item.getName().equals(name))
            {
                return item;
            }
        }
        return null;
    }

    default FileInventoryItem findFile(String name)
    {
        for (FileInventoryItem item : this.getFiles())
        {
            if (item.getName().equals(name))
            {
                return item;
            }
        }
        return null;
    }

    default boolean hasFile(String name)
    {
        return (this.findFile(name) != null);
    }

    default boolean hasFolder(String name)
    {
        return (this.findFolder(name) != null);
    }

    /**
     * Do a breadth first search to find the item with the given UUID.
     * This can be optimised in the future if needed.
     * @param uuid item uuid to find
     * @return an inventory item, either a file or a folder
     */
    default InventoryItem search(UUID uuid)
    {
        final Queue<FileInventoryItem> queuedFiles = new ArrayDeque<>(this.getFiles());
        final Queue<FolderInventoryItem> queuedFolders = new ArrayDeque<>(this.getFolders());

        while ( (! queuedFolders.isEmpty()) || (! queuedFiles.isEmpty()) )
        {
            if (queuedFiles.isEmpty())
            {
                FolderInventoryItem f = queuedFolders.poll();
                if (f.getUuid().equals(uuid)) return f;

                queuedFolders.addAll(f.getFolders());
                queuedFiles.addAll(f.getFiles());
            }
            else
            {
                FileInventoryItem f = queuedFiles.poll();
                if (f.getUuid().equals(uuid)) return f;
            }
        }
        return null;
    }

    default void addFile(FileInventoryItem item)
    {
        this.getFiles().add(item);
        item.setParent(this);
    }

    default void removeFile(FileInventoryItem item)
    {
        this.getFiles().remove(item);
        item.setParent(null);
    }

    default void addFolder(FolderInventoryItem item)
    {
        this.getFolders().add(item);
        item.setParent(this);
    }

    default void removeFolder(FolderInventoryItem item)
    {
        this.getFolders().remove(item);
        item.setParent(null);
    }
}
