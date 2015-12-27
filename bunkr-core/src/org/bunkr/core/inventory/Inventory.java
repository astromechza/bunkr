package org.bunkr.core.inventory;

import java.util.List;
import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class Inventory implements IFFContainer, IFFTraversalTarget
{
    private final boolean filesCompressed;
    private final boolean filesEncrypted;
    private final FFContainer ffcontainer;

    public Inventory(List<FileInventoryItem> files, List<FolderInventoryItem> folders,
                     boolean encrypted, boolean compressed
    )
    {
        this.ffcontainer = new FFContainer(files, folders);
        this.filesCompressed = compressed;
        this.filesEncrypted = encrypted;
    }

    public List<FolderInventoryItem> getFolders()
    {
        return this.ffcontainer.getFolders();
    }

    public List<FileInventoryItem> getFiles()
    {
        return this.ffcontainer.getFiles();
    }

    @Override
    public boolean isAFolder()
    {
        return true;
    }

    @Override
    public boolean isRoot()
    {
        return true;
    }

    public boolean areFilesCompressed()
    {
        return filesCompressed;
    }

    public boolean areFilesEncrypted()
    {
        return filesEncrypted;
    }
}
