package org.bunkr.core.descriptor;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.generators.SCrypt;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.exceptions.IllegalPasswordException;
import org.bunkr.core.inventory.Inventory;
import org.bunkr.core.inventory.InventoryJSON;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.RandomMaker;
import org.bunkr.core.utils.SimpleAES;
import org.json.simple.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;

/**
 * Creator: benmeier
 * Created At: 2016-01-09
 */
public class ScryptDescriptor implements IDescriptor
{
    public static final String IDENTIFIER = "scrypt";

    public final int scryptN;
    public final byte[] scryptSalt;
    public final int scryptR;
    public final int scryptP;
    public final int keyLength;

    public ScryptDescriptor(int keyLength, int scryptN, byte[] scryptSalt, int scryptR, int scryptP)
    {
        this.keyLength = keyLength;
        this.scryptN = scryptN;
        this.scryptSalt = scryptSalt;
        this.scryptR = scryptR;
        this.scryptP = scryptP;
    }

    public ScryptDescriptor(JSONObject o)
    {
        this.keyLength = ((Long) o.get("keyLength")).intValue();
        this.scryptSalt =  DatatypeConverter.parseBase64Binary((String) o.get("scryptSalt"));
        this.scryptN = ((Long) o.get("scryptN")).intValue();
        this.scryptR = ((Long) o.get("scryptR")).intValue();
        this.scryptP = ((Long) o.get("scryptP")).intValue();
    }

    @Override
    public String getIdentifier()
    {
        return IDENTIFIER;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject getParams()
    {
        JSONObject out = new JSONObject();
        out.put("keyLength", this.keyLength);
        out.put("scryptSalt", DatatypeConverter.printBase64Binary(this.scryptSalt));
        out.put("scryptN", this.scryptN);
        out.put("scryptR", this.scryptR);
        out.put("scryptP", this.scryptP);
        return out;
    }

    @Override
    public Inventory readInventoryFromBytes(byte[] source, UserSecurityProvider usp) throws BaseBunkrException
    {
        try
        {
            // generate the encryption key and iv using scrypt
            byte[] data = SCrypt.generate(
                    usp.getHashedPassword(),
                    this.scryptSalt, this.scryptN, this.scryptR, this.scryptP,
                    this.keyLength / 8 + 16
            );

            // pull key and iv out of the data
            byte[] key = Arrays.copyOfRange(data, 0, this.keyLength / 8);
            byte[] iv = Arrays.copyOfRange(data, this.keyLength / 8, data.length);

            byte[] decryptedInv = SimpleAES.decrypt(source, key, iv);
            Arrays.fill(data, (byte) 0);
            Arrays.fill(key, (byte) 0);
            Arrays.fill(iv, (byte) 0);
            return InventoryJSON.decode(new String(decryptedInv));
        }
        catch (CryptoException e)
        {
            throw new BaseBunkrException(e);
        }
    }

    @Override
    public byte[] writeInventoryToBytes(Inventory source, UserSecurityProvider usp) throws BaseBunkrException
    {
        try
        {
            byte[] inventoryJsonBytes = InventoryJSON.encode(source).getBytes();

            // first refresh the salt
            RandomMaker.fill(this.scryptSalt);

            // generate the encryption key and iv using scrypt
            byte[] data = SCrypt.generate(
                    usp.getHashedPassword(),
                    this.scryptSalt, this.scryptN, this.scryptR, this.scryptP,
                    this.keyLength / 8 + 16
            );

            // pull key and iv out of the data
            byte[] key = Arrays.copyOfRange(data, 0, this.keyLength / 8);
            byte[] iv = Arrays.copyOfRange(data, this.keyLength / 8, data.length);

            byte[] encryptedInv = SimpleAES.encrypt(inventoryJsonBytes, key, iv);
            Arrays.fill(inventoryJsonBytes, (byte) 0);
            Arrays.fill(data, (byte) 0);
            Arrays.fill(key, (byte) 0);
            Arrays.fill(iv, (byte) 0);
            return encryptedInv;
        }
        catch (IllegalPasswordException | CryptoException e)
        {
            throw new BaseBunkrException(e);
        }
    }

    public static IDescriptor makeDefaults()
    {
        return new ScryptDescriptor(256, 2 << 15, RandomMaker.get(128), 8, 1);
    }
}
