package com.bunkr_beta.inventory;

import com.bunkr_beta.interfaces.IFFContainer;

import java.util.ArrayList;

/**
 * Creator: benmeier
 * Created At: 2015-11-22
 */
public class FFContainer implements IFFContainer
{
    private final ArrayList<FolderInventoryItem> folders;
    private final ArrayList<FileInventoryItem> files;

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
