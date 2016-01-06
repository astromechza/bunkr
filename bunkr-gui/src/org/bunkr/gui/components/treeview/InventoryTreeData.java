package org.bunkr.gui.components.treeview;

import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.FolderInventoryItem;

import java.util.UUID;

/**
 * Helper datastructure to hold UUID and name. Name is needed *only* for Display purposes. UUID is used for everything
 * else for indexing, lookups, equality, operations.. etc.
 *
 * Creator: benmeier
 * Created At: 2015-12-27
 */
public class InventoryTreeData implements Comparable<InventoryTreeData>
{
    @Override
    public int compareTo(InventoryTreeData o)
    {
        int r = this.getType().compareTo(o.getType());
        if (r == 0) return this.getName().toLowerCase().compareTo(o.getName().toLowerCase());
        return r;
    }

    public enum Type {ROOT, FOLDER, FILE}

    private final UUID uuid;
    private final Type type;
    private String name;

    public InventoryTreeData(FileInventoryItem file)
    {
        this.uuid = file.getUuid();
        this.name = file.getName();
        this.type = Type.FILE;
    }

    public InventoryTreeData(FolderInventoryItem folder)
    {
        this.uuid = folder.getUuid();
        this.name = folder.getName();
        this.type = Type.FOLDER;
    }

    public InventoryTreeData(UUID uuid, String name, Type type)
    {
        this.uuid = uuid;
        this.name = name;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public Type getType()
    {
        return type;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
