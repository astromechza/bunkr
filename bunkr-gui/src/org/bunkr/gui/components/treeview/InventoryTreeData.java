package org.bunkr.gui.components.treeview;

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
        if (r == 0) return this.getName().compareTo(o.getName());
        return r;
    }

    public enum Type {ROOT, FOLDER, FILE}

    private final UUID uuid;
    private final String name;
    private final Type type;

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
}
