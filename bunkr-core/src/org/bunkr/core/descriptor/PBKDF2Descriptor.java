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
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bunkr.core.inventory.Algorithms.Encryption;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.exceptions.IllegalPasswordException;
import org.bunkr.core.inventory.Inventory;
import org.bunkr.core.inventory.InventoryJSON;
import org.bunkr.core.utils.Logging;
import org.bunkr.core.utils.RandomMaker;
import org.bunkr.core.utils.SimpleAES;
import org.bunkr.core.utils.Units;
import org.json.simple.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Creator: benmeier
 * Created At: 2015-12-20
 */
public class PBKDF2Descriptor implements IDescriptor
{
    public static final String IDENTIFIER = "pbkdf2";
    public static final int MINIMUM_PBKD2_ITERS = 4096;
    public static final int SALT_LENGTH = 128;
    public static final List<Integer> SUGGESTED_ITERATION_TIME_LIST = Collections.unmodifiableList(Arrays.asList(
            100 * Units.MILLISECOND, Units.SECOND / 2, Units.SECOND,
            2 * Units.SECOND, 3 * Units.SECOND, 5 * Units.SECOND, 10 * Units.SECOND
    ));

    public final Encryption encryptionAlgorithm;
    public final int pbkdf2Iterations;
    public final byte[] pbkdf2Salt;

    public PBKDF2Descriptor(Encryption encryptionAlgorithm, int pbkdf2Iterations, byte[] pbkdf2Salt)
    {
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.pbkdf2Iterations = pbkdf2Iterations;
        this.pbkdf2Salt = pbkdf2Salt;

        if (pbkdf2Iterations < MINIMUM_PBKD2_ITERS)
            throw new IllegalArgumentException(String.format("pbkdf2Iterations must be at least %d", MINIMUM_PBKD2_ITERS));
    }

    public PBKDF2Descriptor(JSONObject params)
    {
        encryptionAlgorithm = Encryption.valueOf((String) params.get("encryptionAlgorithm"));
        pbkdf2Iterations = ((Long) params.get("timeComboBox")).intValue();
        pbkdf2Salt =  DatatypeConverter.parseBase64Binary((String) params.get("salt"));

        if (pbkdf2Iterations < MINIMUM_PBKD2_ITERS)
            throw new IllegalArgumentException(String.format("pbkdf2Iterations must be at least %d", MINIMUM_PBKD2_ITERS));
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
        out.put("encryptionAlgorithm", this.encryptionAlgorithm.toString());
        out.put("timeComboBox", this.pbkdf2Iterations);
        out.put("salt", DatatypeConverter.printBase64Binary(this.pbkdf2Salt));
        return out;
    }

    @Override
    public Inventory readInventoryFromBytes(byte[] source, UserSecurityProvider usp) throws BaseBunkrException
    {
        try
        {
            if (this.encryptionAlgorithm == Encryption.NONE)
                throw new IllegalArgumentException("PBKDF2Descriptor requires an active encryption mode");

            PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator(new SHA256Digest());
            g.init(usp.getHashedPassword(), this.pbkdf2Salt, this.pbkdf2Iterations);
            ParametersWithIV kp = (ParametersWithIV) g.generateDerivedParameters(
                    this.encryptionAlgorithm.keyByteLength * 8,
                    this.encryptionAlgorithm.ivByteLength * 8);

            byte[] decryptedInv = SimpleAES.decrypt(
                    this.encryptionAlgorithm,
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

            if (this.encryptionAlgorithm == Encryption.NONE)
                throw new IllegalArgumentException("PBKDF2Descriptor requires an active encryption mode");

            // first refresh the salt
            RandomMaker.fill(this.pbkdf2Salt);

            PKCS5S2ParametersGenerator g = new PKCS5S2ParametersGenerator(new SHA256Digest());
            g.init(usp.getHashedPassword(), this.pbkdf2Salt, this.pbkdf2Iterations);
            ParametersWithIV kp = (ParametersWithIV) g.generateDerivedParameters(
                    this.encryptionAlgorithm.keyByteLength * 8,
                    this.encryptionAlgorithm.ivByteLength * 8);

            // encrypt the inventory
            byte[] encryptedInv = SimpleAES.encrypt(
                    this.encryptionAlgorithm,
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

    public static IDescriptor make(Encryption algorithm, int iterations)
    {
        return new PBKDF2Descriptor(algorithm, iterations, RandomMaker.get(SALT_LENGTH));
    }

    public static int calculateRounds(int milliseconds)
    {
        Logging.info("Calculating how many SHA1 rounds we can do in %d millis.", milliseconds);
        HMac mac = new HMac(new SHA256Digest());
        byte[] state = new byte[mac.getMacSize()];
        long startTime = System.currentTimeMillis();
        int pbkdf2Iterations = 0;
        while((System.currentTimeMillis() - startTime) < milliseconds)
        {
            mac.update(state, 0, state.length);
            mac.doFinal(state, 0);
            pbkdf2Iterations++;
        }
        pbkdf2Iterations = Math.max(pbkdf2Iterations, PBKDF2Descriptor.MINIMUM_PBKD2_ITERS);
        Logging.info("Got %d", pbkdf2Iterations);
        return pbkdf2Iterations;
    }
}
