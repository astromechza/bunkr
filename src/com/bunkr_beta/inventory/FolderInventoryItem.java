package com.bunkr_beta.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class FolderInventoryItem extends InventoryItem implements IFFContainer, IFFTraversalTarget
{
    private final FFContainer ffcontainer;

    public FolderInventoryItem(String name, UUID uuid, List<FileInventoryItem> files, List<FolderInventoryItem> folders)
    {
        super(name, uuid);
        this.ffcontainer = new FFContainer(files, folders);
    }

    public FolderInventoryItem(String name)
    {
        super(name, UUID.randomUUID());
        this.ffcontainer = new FFContainer(new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public List<FolderInventoryItem> getFolders()
    {
        return this.ffcontainer.getFolders();
    }

    @Override
    public List<FileInventoryItem> getFiles()
    {
        return this.ffcontainer.getFiles();
    }

    @Override
    public boolean isAFolder()
    {
        return true;
    }
}
