package com.bunkr_beta.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

/**
 * Creator: benmeier
 * Created At: 2015-11-22
 */
public interface IFFContainer
{
    List<FolderInventoryItem> getFolders();
    List<FileInventoryItem> getFiles();

    @JsonIgnore
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
        for (FolderInventoryItem item : this.getFolders())
        {
            if (item.getName().equals(name))
            {
                return item;
            }
        }
        for (FileInventoryItem item : this.getFiles())
        {
            if (item.getName().equals(name))
            {
                return item;
            }
        }
        return null;
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

    default boolean hasFile(String name)
    {
        for (FileInventoryItem item : this.getFiles())
        {
            if (item.getName().equals(name)) return true;
        }
        return false;
    }

    default boolean hasFolder(String name)
    {
        for (FolderInventoryItem item : this.getFolders())
        {
            if (item.getName().equals(name)) return true;
        }
        return false;
    }
}
