package org.bunkr.gui.components;

import java.util.UUID;

/**
 * Helper datastructure to hold UUID and name. Name is needed *only* for Display purposes. UUID is used for everything
 * else for indexing, lookups, equality, operations.. etc.
 *
 * Creator: benmeier
 * Created At: 2015-12-27
 */
public class IntermedInvTreeDS
{
    public enum Type {ROOT, FOLDER, FILE}

    private final UUID uuid;
    private final String name;
    private final Type type;

    public IntermedInvTreeDS(UUID uuid, String name, Type type)
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
