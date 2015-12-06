package com.bunkr_beta.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public FolderInventoryItem(
            @JsonProperty("name") String name,
            @JsonProperty("uuid") UUID uuid,
            @JsonProperty("files") ArrayList<FileInventoryItem> files,
            @JsonProperty("folders") ArrayList<FolderInventoryItem> folders
    )
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

    @JsonIgnore
    @Override
    public boolean isAFolder()
    {
        return true;
    }
}
