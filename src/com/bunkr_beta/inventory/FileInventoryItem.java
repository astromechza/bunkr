package com.bunkr_beta.inventory;

import com.bunkr_beta.fragmented_range.FragmentedRange;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class FileInventoryItem extends InventoryItem
{
    private long size;
    private long modifiedAt;
    private byte[] encryptionKey;
    private byte[] encryptionIV;
    private FragmentedRange blocks;

    @JsonCreator
    public FileInventoryItem(
            @JsonProperty("name") String name,
            @JsonProperty("uuid") UUID uuid,
            @JsonProperty("blocks") FragmentedRange blocks,
            @JsonProperty("size") long size,
            @JsonProperty("modifiedAt") long modifiedAt,
            @JsonProperty("encryptionKey") byte[] encryptionKey,
            @JsonProperty("encryptionIV") byte[] encryptionIV
    )
    {
        super(name, uuid);
        this.encryptionKey = encryptionKey;
        this.encryptionIV = encryptionIV;
        this.size = size;
        this.modifiedAt = modifiedAt;
        this.blocks = blocks;
    }

    public FileInventoryItem(String name)
    {
        super(name, UUID.randomUUID());
        this.size = 0;
        this.blocks = new FragmentedRange();
        this.modifiedAt = System.currentTimeMillis();
        SecureRandom r = new SecureRandom();
        int keysize =  256 / 8;
        this.encryptionKey = new byte[keysize];
        r.nextBytes(encryptionKey);
        this.encryptionIV = new byte[keysize];
        r.nextBytes(encryptionIV);
    }

    public FragmentedRange getBlocks()
    {
        return blocks;
    }

    public void setBlocks(FragmentedRange blocks)
    {
        this.blocks = blocks;
    }

    public byte[] getEncryptionIV()
    {
        return encryptionIV;
    }

    public void setEncryptionIV(byte[] encryptionIV)
    {
        this.encryptionIV = encryptionIV;
    }

    public byte[] getEncryptionKey()
    {
        return encryptionKey;
    }

    public void setEncryptionKey(byte[] encryptionKey)
    {
        this.encryptionKey = encryptionKey;
    }

    public long getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(long modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }
}
