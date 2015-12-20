package org.bunkr.inventory;

import org.bunkr.streams.AlgorithmIdentifier;

import java.util.List;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class Inventory implements IFFContainer, IFFTraversalTarget
{
    private final FFContainer ffcontainer;
    private final AlgorithmIdentifier defaultAlgorithms;

    public Inventory(AlgorithmIdentifier defaults, List<FileInventoryItem> files, List<FolderInventoryItem> folders)
    {
        this.defaultAlgorithms = defaults;
        this.ffcontainer = new FFContainer(files, folders);
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

    public AlgorithmIdentifier getDefaultAlgorithms()
    {
        return defaultAlgorithms;
    }
}
