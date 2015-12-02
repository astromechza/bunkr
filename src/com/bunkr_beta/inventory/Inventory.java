package com.bunkr_beta.inventory;

import com.bunkr_beta.interfaces.IFFContainer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class Inventory implements IFFContainer
{
    private final FFContainer ffcontainer;

    @JsonCreator
    public Inventory(
            @JsonProperty("files") ArrayList<FileInventoryItem> files,
            @JsonProperty("folders") ArrayList<FolderInventoryItem> folders
    )
    {
        this.ffcontainer = new FFContainer(files, folders);
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

}
