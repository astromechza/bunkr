package com.bunkr_beta.inventory;

import com.bunkr_beta.interfaces.IFFContainer;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class FolderInventoryItem extends InventoryItem implements IFFContainer
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
    public ArrayList<FolderInventoryItem> getFolders()
    {
        return this.ffcontainer.folders;
    }

    @Override
    public ArrayList<FileInventoryItem> getFiles()
    {
        return this.ffcontainer.files;
    }
}
