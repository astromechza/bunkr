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
public class FFContainer
{
    public final ArrayList<FolderInventoryItem> folders;
    public final ArrayList<FileInventoryItem> files;

    public FFContainer(ArrayList<FileInventoryItem> files, ArrayList<FolderInventoryItem> folders)
    {
        this.folders = folders;
        this.files = files;
    }

    @JsonIgnore
    public InventoryIterator getIterator()
    {
        return new InventoryIterator(this);
    }

    public static class InventoryIterator implements Iterator<FileInventoryItem>
    {
        Queue<FileInventoryItem> queuedFiles = new ArrayDeque<>();
        Queue<FolderInventoryItem> queuedFolders = new ArrayDeque<>();

        public InventoryIterator(FFContainer inv)
        {
            queuedFolders.addAll(inv.folders);
            queuedFiles.addAll(inv.files);
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
