package com.bunkr_beta.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class FolderInventoryItem extends InventoryItem
{
    private final ArrayList<FolderInventoryItem> folders;
    private final ArrayList<FileInventoryItem> files;

    public FolderInventoryItem(
            @JsonProperty("name") String name,
            @JsonProperty("uuid") UUID uuid,
            @JsonProperty("files") ArrayList<FileInventoryItem> files,
            @JsonProperty("folders") ArrayList<FolderInventoryItem> folders
    )
    {
        super(name, uuid);
        this.files = files;
        this.folders = folders;
    }

    public ArrayList<FileInventoryItem> getFiles()
    {
        return files;
    }

    public ArrayList<FolderInventoryItem> getFolders()
    {
        return folders;
    }
}
