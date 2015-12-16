package org.bunkr;

import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.descriptor.Descriptor;
import org.bunkr.descriptor.DescriptorJSON;
import org.bunkr.exceptions.BaseBunkrException;
import org.bunkr.exceptions.IllegalPasswordException;
import org.bunkr.inventory.Inventory;
import org.bunkr.inventory.InventoryJSON;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.*;

public class ArchiveInfoContext implements IArchiveInfoContext
{
    public final File filePath;
    private Inventory inventory;
    private Descriptor descriptor;
    private int blockSize;
    private long blockDataLength;
    private boolean fresh = false;

    public ArchiveInfoContext(File filePath, PasswordProvider uic) throws IOException, CryptoException,
            BaseBunkrException
    {
        this.filePath = filePath;
        this.refresh(uic);
    }

    @Override
    public void refresh(PasswordProvider uic) throws IOException, CryptoException, IllegalPasswordException
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
                this.descriptor = DescriptorJSON.decode(descjson);

                if (this.descriptor.hasEncryption())
                {
                    byte[] encryptedInventory = IO.readNBytes(dis, dis.readInt());

                    PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator();
                    g.init(uic.getHashedArchivePassword(), this.descriptor.getEncryption().pbkdf2Salt,
                           this.descriptor.getEncryption().pbkdf2Iterations);
                    ParametersWithIV kp = ((ParametersWithIV)g.generateDerivedParameters(
                            this.descriptor.getEncryption().aesKeyLength,
                            this.descriptor.getEncryption().aesKeyLength)
                    );

                    byte[] decryptedInv = SimpleAES.decrypt(
                            encryptedInventory,
                            ((KeyParameter) kp.getParameters()).getKey(),
                            kp.getIV()
                    );

                    this.inventory = InventoryJSON.decode(new String(decryptedInv));
                }
                else
                {
                    this.inventory = InventoryJSON.decode(IO.readString(dis));
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
    public long getBlockDataLength()
    {
        return blockDataLength;
    }
}
