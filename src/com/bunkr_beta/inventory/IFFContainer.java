package com.bunkr_beta.inventory;

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
}
