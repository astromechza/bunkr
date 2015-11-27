package com.bunkr_beta.inventory;

import com.bunkr_beta.interfaces.IFFContainer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;

/**
 * Creator: benmeier
 * Created At: 2015-11-22
 */
public class FFContainer implements IFFContainer
{
    public final ArrayList<FolderInventoryItem> folders;
    public final ArrayList<FileInventoryItem> files;

    public FFContainer(ArrayList<FileInventoryItem> files, ArrayList<FolderInventoryItem> folders)
    {
        this.folders = folders;
        this.files = files;
    }

    @Override
    public ArrayList<FolderInventoryItem> getFolders()
    {
        return this.folders;
    }

    @Override
    public ArrayList<FileInventoryItem> getFiles()
    {
        return this.files;
    }
}
