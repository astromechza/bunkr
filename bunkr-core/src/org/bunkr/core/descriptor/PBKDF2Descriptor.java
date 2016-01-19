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

package org.bunkr.core.descriptor;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.exceptions.IllegalPasswordException;
import org.bunkr.core.inventory.Inventory;
import org.bunkr.core.inventory.InventoryJSON;
import org.bunkr.core.utils.RandomMaker;
import org.bunkr.core.utils.SimpleAES;
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

    public static final int MINIMUM_AES_KEY_LENGTH = 256;
    public static final int MINIMUM_PBKD2_ITERS = 4096;
    public static final int SALT_LENGTH = 128;

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
        pbkdf2Iterations = ((Long) params.get("timeComboBox")).intValue();
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
        out.put("timeComboBox", this.pbkdf2Iterations);
        out.put("salt", DatatypeConverter.printBase64Binary(this.pbkdf2Salt));
        return out;
    }

    @Override
    public Inventory readInventoryFromBytes(byte[] source, UserSecurityProvider usp) throws BaseBunkrException
    {
        try
        {
            PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator(new SHA256Digest());
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

            // first refresh the salt
            RandomMaker.fill(this.pbkdf2Salt);
            
            PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator(new SHA256Digest());
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

    public static IDescriptor makeDefaults()
    {
        return new PBKDF2Descriptor(256, 10000, RandomMaker.get(SALT_LENGTH));
    }

}
