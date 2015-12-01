package com.bunkr_beta.interfaces;

import com.bunkr_beta.Descriptor;
import com.bunkr_beta.UserInfoContext;
import com.bunkr_beta.inventory.Inventory;
import org.bouncycastle.crypto.CryptoException;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public interface IArchiveInfoContext
{
    void refresh(UserInfoContext uic) throws IOException, CryptoException;

    boolean isFresh();

    void invalidate();

    void assertFresh();

    long getBlockDataLength();

    int getBlockSize();

    Descriptor getDescriptor();

    Inventory getInventory();

    default long getNumBlocks()
    {
        return this.getBlockDataLength() / this.getBlockSize();
    }
}
