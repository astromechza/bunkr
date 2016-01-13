/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.core;

import org.bunkr.core.descriptor.DescriptorBuilder;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.IO;
import org.bunkr.core.descriptor.IDescriptor;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.inventory.Inventory;

import java.io.*;

public class ArchiveInfoContext implements IArchiveInfoContext
{
    public final File filePath;
    private Inventory inventory;
    private IDescriptor descriptor;
    private int blockSize;
    private long blockDataLength;

    public ArchiveInfoContext(File filePath, UserSecurityProvider uic) throws IOException, BaseBunkrException
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
    public void setDescriptor(IDescriptor descriptor)
    {
        this.descriptor = descriptor;
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
