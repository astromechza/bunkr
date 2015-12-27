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
    private final UUID uuid;
    private final String name;

    public IntermedInvTreeDS(UUID uuid, String name)
    {
        this.uuid = uuid;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public UUID getUuid()
    {
        return uuid;
    }
}
