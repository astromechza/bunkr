package org.bunkr;

import org.bunkr.descriptor.Descriptor;
import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.inventory.Inventory;
import org.bouncycastle.crypto.CryptoException;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public interface IArchiveInfoContext
{
    void refresh(PasswordProvider uic) throws IOException, CryptoException;

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
