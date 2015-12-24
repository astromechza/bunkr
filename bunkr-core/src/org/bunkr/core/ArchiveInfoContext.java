package org.bunkr.core;

import org.bunkr.core.descriptor.DescriptorBuilder;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.IO;
import org.bunkr.core.descriptor.IDescriptor;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.inventory.Inventory;
import org.bouncycastle.crypto.CryptoException;

import java.io.*;

public class ArchiveInfoContext implements IArchiveInfoContext
{
    public final File filePath;
    private Inventory inventory;
    private IDescriptor descriptor;
    private int blockSize;
    private long blockDataLength;

    public ArchiveInfoContext(File filePath, UserSecurityProvider uic) throws IOException, CryptoException,
            BaseBunkrException
    {
        this.filePath = filePath;
        this.refresh(uic);
    }

    @Override
    public void refresh(UserSecurityProvider uic) throws IOException, BaseBunkrException
    {
        try(FileInputStream fis = new FileInputStream(this.filePath))
        {
            try(DataInputStream dis = new DataInputStream(fis))
            {
                String fivebytes = IO.readNByteString(dis, 5);
                if (! fivebytes.equals("BUNKR")) throw new IOException("File format header does not match 'BUNKR'");
                Version.assertCompatible(dis.readByte(), dis.readByte(), dis.readByte(), false);
                this.blockSize = dis.readInt();
                this.blockDataLength = dis.readLong();
                IO.reliableSkip(dis, this.blockDataLength);
                this.descriptor = DescriptorBuilder.fromJSON(IO.readString(dis));
                this.inventory = this.descriptor.readInventoryFromBytes(IO.readNBytes(dis, dis.readInt()), uic);
            }
        }
    }

    @Override
    public int getBlockSize()
    {
        return blockSize;
    }

    @Override
    public IDescriptor getDescriptor()
    {
        return descriptor;
    }

    @Override
    public Inventory getInventory()
    {
        return inventory;
    }

    @Override
    public long getBlockDataLength()
    {
        return blockDataLength;
    }
}
