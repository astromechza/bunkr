package com.bunkr_beta.interfaces;

import com.bunkr_beta.Descriptor;
import com.bunkr_beta.inventory.Inventory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public interface IArchiveInfoContext
{
    void refresh() throws IOException, NoSuchAlgorithmException;

    boolean isFresh();

    void invalidate();

    void assertFresh();

    int getBlockSize();

    Descriptor getDescriptor();

    Inventory getInventory();

    long getNumBlocks();

    long getBlockDataLength();
}
