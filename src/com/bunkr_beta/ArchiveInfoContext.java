package com.bunkr_beta;

import com.bunkr_beta.interfaces.IArchiveInfoContext;
import com.bunkr_beta.inventory.Inventory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.crypto.engines.HC256Engine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.*;
import java.security.NoSuchAlgorithmException;

public class ArchiveInfoContext implements IArchiveInfoContext
{
    public final File filePath;
    private Inventory inventory;
    private Descriptor descriptor;
    private int blockSize;
    private long blockDataLength;
    private boolean fresh = false;

    public ArchiveInfoContext(File filePath) throws IOException, NoSuchAlgorithmException
    {
        this.filePath = filePath;
        this.refresh();
    }

    @Override
    public void refresh() throws IOException, NoSuchAlgorithmException
    {
        try(FileInputStream fis = new FileInputStream(this.filePath))
        {
            try(DataInputStream dis = new DataInputStream(fis))
            {
                String fivebytes = IO.readNByteString(dis, 5);
                if (! fivebytes.equals("BUNKR")) throw new IOException("File format header does not match 'BUNKR'");
                dis.readByte();
                dis.readByte();
                dis.readByte();
                this.blockSize = dis.readInt();
                this.blockDataLength = dis.readLong();
                IO.reliableSkip(dis, this.blockDataLength);
                String descjson = IO.readString(dis);
                this.descriptor = new ObjectMapper().readValue(descjson, Descriptor.class);

                if (this.descriptor.encryption == null)
                {
                    this.inventory = new ObjectMapper().readValue(IO.readString(dis), Inventory.class);
                }
                else
                {
                    int l = dis.readInt();
                    byte[] encryptedInventory = new byte[l];
                    if (dis.read(encryptedInventory) != l) throw new IOException("Did not read enough bytes");

                    PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator();
                    g.init("password".getBytes(), this.descriptor.encryption.pbkdf2Salt,
                           this.descriptor.encryption.pbkdf2Iterations);
                    ParametersWithIV kp = ((ParametersWithIV)g.generateDerivedParameters(
                            this.descriptor.encryption.aesKeyLength,
                            this.descriptor.encryption.aesKeyLength)
                    );

                    HC256Engine cipher = new HC256Engine();
                    cipher.init(false, kp);
                    byte[] decryptedInv = new byte[l];
                    cipher.processBytes(encryptedInventory, 0, l, decryptedInv, 0);

                    this.inventory = new ObjectMapper().readValue(new String(decryptedInv), Inventory.class);
                }
            }
        }
        this.fresh = true;
    }

    @Override
    public boolean isFresh()
    {
        return this.fresh;
    }

    @Override
    public void invalidate()
    {
        this.fresh = false;
    }

    @Override
    public void assertFresh()
    {
        if (! isFresh())
        {
            throw new AssertionError("ArchiveInfoContext is no longer fresh");
        }
    }


    @Override
    public int getBlockSize()
    {
        return blockSize;
    }

    @Override
    public Descriptor getDescriptor()
    {
        return descriptor;
    }

    @Override
    public Inventory getInventory()
    {
        return inventory;
    }

    @Override
    public long getNumBlocks()
    {
        return this.blockDataLength / this.blockSize;
    }

    @Override
    public long getBlockDataLength()
    {
        return blockDataLength;
    }
}
