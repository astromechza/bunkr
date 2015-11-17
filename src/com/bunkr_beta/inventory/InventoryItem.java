package com.bunkr_beta.inventory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class InventoryItem
{
    private final UUID uuid;
    private String name;

    @JsonCreator
    public InventoryItem(
            @JsonProperty("name") String name,
            @JsonProperty("uuid") UUID uuid
    )
    {
        this.name = name;
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
}
