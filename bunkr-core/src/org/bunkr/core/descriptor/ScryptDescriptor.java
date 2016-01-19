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

    public static final int SALT_LENGTH = 128;
    public static final int MINIMUM_AES_KEY_LENGTH = 256;
    public static final int MINIMUM_SCRYPT_N = 2 << 13;
    public static final int DEFAULT_SCRYPT_R = 8;
    public static final int DEFAULT_SCRYPT_P = 1;

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
        return new ScryptDescriptor(MINIMUM_AES_KEY_LENGTH, MINIMUM_SCRYPT_N, RandomMaker.get(SALT_LENGTH), DEFAULT_SCRYPT_R, DEFAULT_SCRYPT_P);
    }
}
