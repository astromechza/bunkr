package com.bunkr_beta.inventory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class Inventory
{
    public final ArrayList<FolderInventoryItem> folders;
    public final ArrayList<FileInventoryItem> files;

    @JsonCreator
    public Inventory(
            @JsonProperty("files") ArrayList<FileInventoryItem> files,
            @JsonProperty("folders") ArrayList<FolderInventoryItem> folders
    )
    {
        this.files = files;
        this.folders = folders;
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

        public InventoryIterator(Inventory inv)
        {
            queuedFolders.addAll(inv.folders);
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
