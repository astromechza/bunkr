package org.bunkr.core.inventory;

import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class InventoryItem implements Comparable<InventoryItem>
{
    private final UUID uuid;
    private String name;
    private IFFContainer parent;

    public InventoryItem(String name, UUID uuid
    )
    {
        this.name = InventoryPather.assertValidName(name);
        this.uuid = uuid;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public IFFContainer getParent()
    {
        return parent;
    }

    public void setParent(IFFContainer parent)
    {
        this.parent = parent;
    }

    public String getAbsolutePath()
    {
        System.out.println(String.format("Getting abs path for %s parent type(%s)", this.getName(), this.getParent()));
        if (this.parent == null) throw new RuntimeException("Orphaned Inventory Item");
        IFFContainer p = this.getParent();
        if (p instanceof Inventory)
        {
            return "/" + this.getName();
        }
        else if (p instanceof FolderInventoryItem)
        {
            return ((FolderInventoryItem) p).getAbsolutePath() + "/" + this.getName();
        }
        else
        {
            throw new RuntimeException("Bad parent type " + p.getClass().getName());
        }
    }

    @Override
    public int compareTo(InventoryItem o)
    {
        return this.getName().compareTo(o.getName());
    }
}
