package org.bunkr.core.inventory;

import java.util.List;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class Inventory implements IFFContainer, IFFTraversalTarget
{
    private Algorithms.Encryption defaultEncryption;
    private final FFContainer ffcontainer;

    public Inventory(List<FileInventoryItem> files, List<FolderInventoryItem> folders, Algorithms.Encryption encrypted)
    {
        this.ffcontainer = new FFContainer(files, folders);
        this.defaultEncryption = encrypted;
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

    public boolean areFilesEncrypted()
    {
        return defaultEncryption != null && defaultEncryption != Algorithms.Encryption.NONE;
    }

    public Algorithms.Encryption getDefaultEncryption()
    {
        return defaultEncryption;
    }

    public void setDefaultEncryption(Algorithms.Encryption defaultEncryption)
    {
        this.defaultEncryption = defaultEncryption;
    }
}
