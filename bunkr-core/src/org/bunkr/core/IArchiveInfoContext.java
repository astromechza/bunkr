package org.bunkr.core;

import org.bunkr.core.descriptor.IDescriptor;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.inventory.Inventory;
import org.bunkr.core.usersec.UserSecurityProvider;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public interface IArchiveInfoContext
{
    void refresh(UserSecurityProvider uic) throws IOException, BaseBunkrException;

    long getBlockDataLength();

    int getBlockSize();

    IDescriptor getDescriptor();

    void setDescriptor(IDescriptor descriptor);

    Inventory getInventory();
}
