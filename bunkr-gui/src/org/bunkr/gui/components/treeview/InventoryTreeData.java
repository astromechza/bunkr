package org.bunkr.gui.components.treeview;

import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.FolderInventoryItem;
import org.bunkr.core.inventory.MediaType;
import org.bunkr.gui.Icons;

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
    private String icon;

    public InventoryTreeData(FileInventoryItem file)
    {
        this.uuid = file.getUuid();
        this.name = file.getName();
        this.type = Type.FILE;
        this.icon = getIconForMediaType(file.getMediaType());
    }

    public InventoryTreeData(FolderInventoryItem folder)
    {
        this.uuid = folder.getUuid();
        this.name = folder.getName();
        this.type = Type.FOLDER;
        this.icon = Icons.ICON_FOLDER;
    }

    private InventoryTreeData(UUID uuid, String name, Type type, String icon)
    {
        this.uuid = uuid;
        this.name = name;
        this.type = type;
        this.icon = icon;
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

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public static InventoryTreeData makeRoot()
    {
        return new InventoryTreeData(null, "/", Type.ROOT, Icons.ICON_FOLDER);
    }

    public static String getIconForMediaType(String m)
    {
        if (m.equals(MediaType.IMAGE)) return Icons.ICON_FILE_IMAGE;
        if (m.equals(MediaType.TEXT)) return Icons.ICON_FILE_TEXT;
        return Icons.ICON_FILE;
    }
}
