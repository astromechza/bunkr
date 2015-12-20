package org.bunkr.descriptor;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bunkr.core.UserSecurityProvider;
import org.bunkr.exceptions.BaseBunkrException;
import org.bunkr.exceptions.IllegalPasswordException;
import org.bunkr.inventory.Inventory;
import org.bunkr.inventory.InventoryJSON;
import org.bunkr.utils.RandomMaker;
import org.bunkr.utils.SimpleAES;
import org.json.simple.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;

/**
 * Creator: benmeier
 * Created At: 2015-12-20
 */
public class PBKDF2Descriptor implements IDescriptor
{
    public static final String IDENTIFIER = "pbkdf2";

    private static final int MINIMUM_AES_KEY_LENGTH = 256;
    private static final int MINIMUM_PBKD2_ITERS = 4096;

    public final int pbkdf2Iterations;
    public final int aesKeyLength;
    public final byte[] pbkdf2Salt;

    public PBKDF2Descriptor(int aesKeyLength, int pbkdf2Iterations, byte[] pbkdf2Salt)
    {
        this.aesKeyLength = aesKeyLength;
        this.pbkdf2Iterations = pbkdf2Iterations;
        this.pbkdf2Salt = pbkdf2Salt;

        if (pbkdf2Iterations < MINIMUM_PBKD2_ITERS)
            throw new IllegalArgumentException(String.format("pbkdf2Iterations must be at least %d", MINIMUM_PBKD2_ITERS));

        if (aesKeyLength != MINIMUM_AES_KEY_LENGTH)
            throw new IllegalArgumentException(String.format("aesKeyLength must be %d", MINIMUM_AES_KEY_LENGTH));
    }

    public PBKDF2Descriptor(JSONObject params)
    {
        aesKeyLength = ((Long) params.get("aeskeylength")).intValue();
        pbkdf2Iterations = ((Long) params.get("iterations")).intValue();
        pbkdf2Salt =  DatatypeConverter.parseBase64Binary((String) params.get("salt"));

        if (pbkdf2Iterations < MINIMUM_PBKD2_ITERS)
            throw new IllegalArgumentException(String.format("pbkdf2Iterations must be at least %d", MINIMUM_PBKD2_ITERS));

        if (aesKeyLength != MINIMUM_AES_KEY_LENGTH)
            throw new IllegalArgumentException(String.format("aesKeyLength must be %d", MINIMUM_AES_KEY_LENGTH));
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
        out.put("aeskeylength", this.aesKeyLength);
        out.put("iterations", this.pbkdf2Iterations);
        out.put("salt", DatatypeConverter.printBase64Binary(this.pbkdf2Salt));
        return out;
    }

    @Override
    public Inventory readInventoryFromBytes(byte[] source, UserSecurityProvider usp) throws BaseBunkrException
    {
        try
        {
            PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator();
            g.init(usp.getHashedPassword(), this.pbkdf2Salt, this.pbkdf2Iterations);
            ParametersWithIV kp = ((ParametersWithIV) g.generateDerivedParameters(
                    this.aesKeyLength,
                    this.aesKeyLength)
            );

            byte[] decryptedInv = SimpleAES.decrypt(
                    source,
                    ((KeyParameter) kp.getParameters()).getKey(),
                    kp.getIV()
            );

            return InventoryJSON.decode(new String(decryptedInv));
        }
        catch (IllegalPasswordException | CryptoException e)
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
            PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator();
            g.init(usp.getHashedPassword(), this.pbkdf2Salt, this.pbkdf2Iterations);
            ParametersWithIV kp =
                    ((ParametersWithIV) g.generateDerivedParameters(this.aesKeyLength, this.aesKeyLength));

            // encrypt the inventory
            byte[] encryptedInv = SimpleAES.encrypt(
                    inventoryJsonBytes,
                    ((KeyParameter) kp.getParameters()).getKey(),
                    kp.getIV()
            );
            Arrays.fill(inventoryJsonBytes, (byte) 0);

            return encryptedInv;
        }
        catch (IllegalPasswordException | CryptoException e)
        {
            throw new BaseBunkrException(e);
        }
    }

    @Override
    public boolean mustEncryptFiles()
    {
        return true;
    }

    public static IDescriptor makeDefaults()
    {
        return new PBKDF2Descriptor(256, 10000, RandomMaker.get(128));
    }

}
