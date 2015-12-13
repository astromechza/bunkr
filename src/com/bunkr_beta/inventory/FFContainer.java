package com.bunkr_beta.inventory;

import java.util.List;

/**
 * Creator: benmeier
 * Created At: 2015-11-22
 */
public class FFContainer implements IFFContainer
{
    private final List<FolderInventoryItem> folders;
    private final List<FileInventoryItem> files;

    public FFContainer(List<FileInventoryItem> files, List<FolderInventoryItem> folders)
    {
        this.folders = folders;
        this.files = files;
    }

    @Override
    public List<FolderInventoryItem> getFolders()
    {
        return this.folders;
    }

    @Override
    public List<FileInventoryItem> getFiles()
    {
        return this.files;
    }
}
