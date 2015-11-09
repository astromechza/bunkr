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
    public final ArrayList<FolderInventoryItem> folders;
    public final ArrayList<FileInventoryItem> files;

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
}
