package com.bunkr_beta.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;

/**
 * Creator: benmeier
 * Created At: 2015-11-22
 */
public interface IFFContainer
{
    ArrayList<FolderInventoryItem> getFolders();

    ArrayList<FileInventoryItem> getFiles();

    @JsonIgnore
    default InventoryIterator getIterator()
    {
        return new InventoryIterator(this);
    }

    class InventoryIterator implements Iterator<FileInventoryItem>
    {
        Queue<FileInventoryItem> queuedFiles = new ArrayDeque<>();
        Queue<FolderInventoryItem> queuedFolders = new ArrayDeque<>();

        public InventoryIterator(IFFContainer inv)
        {
            queuedFolders.addAll(inv.getFolders());
            queuedFiles.addAll(inv.getFiles());
        }

        private void fillFileQueue()
        {
            while (queuedFiles.isEmpty() && ! queuedFolders.isEmpty())
            {
                queuedFiles.addAll(queuedFolders.poll().getFiles());
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
            return queuedFiles.poll();
        }
    }
}
