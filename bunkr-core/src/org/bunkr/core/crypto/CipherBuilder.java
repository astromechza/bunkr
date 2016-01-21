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

package org.bunkr.core.crypto;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.TwofishEngine;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bunkr.core.inventory.Algorithms;
import org.bunkr.core.inventory.Algorithms.Encryption;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.utils.RandomMaker;

import java.util.Arrays;

/**
 * Created At: 2016-01-20
 */
public class CipherBuilder
{
    /**
     * Build a block cipher for encrypting or decrypting the target file.
     *
     * Intelligently create the correct cipher and initialize it correctly from the encryptionData present on the
     * target file. If the goal is encryption, the encryption data for the file is reinitialized from random.
     *
     * @param file the target FileInventoryItem
     * @param encrypting boolean indicating encryption (true) or decryption (false)
     * @return a BlockCipher
     */
    public static BlockCipher buildCipherForFile(FileInventoryItem file, boolean encrypting)
    {
        Encryption alg = file.getEncryptionAlgorithm();

        if (alg.c.equals(Algorithms.SYMMETRIC_CIPHER.AES))
        {
            if (alg.m.equals(Algorithms.SYMMETRIC_MODE.CTR))
            {
                SICBlockCipher fileCipher = new SICBlockCipher(new AESEngine());
                byte[] edata = file.getEncryptionData();
                if (encrypting)
                {
                    edata = new byte[alg.keyByteLength + fileCipher.getBlockSize()];
                    RandomMaker.fill(edata);
                    file.setEncryptionData(edata);
                }
                byte[] ekey = Arrays.copyOfRange(edata, 0, alg.keyByteLength);
                byte[] eiv = Arrays.copyOfRange(edata, alg.keyByteLength, alg.keyByteLength + fileCipher.getBlockSize());
                fileCipher.init(encrypting, new ParametersWithIV(new KeyParameter(ekey), eiv));
                return fileCipher;
            }
        }

        else if (alg.c.equals(Algorithms.SYMMETRIC_CIPHER.TWOFISH))
        {
            if (alg.m.equals(Algorithms.SYMMETRIC_MODE.CTR))
            {
                SICBlockCipher fileCipher = new SICBlockCipher(new TwofishEngine());
                byte[] edata = file.getEncryptionData();
                if (encrypting)
                {
                    edata = new byte[alg.keyByteLength + fileCipher.getBlockSize()];
                    RandomMaker.fill(edata);
                    file.setEncryptionData(edata);
                }
                byte[] ekey = Arrays.copyOfRange(edata, 0, alg.keyByteLength);
                byte[] eiv = Arrays.copyOfRange(edata, alg.keyByteLength, alg.keyByteLength + fileCipher.getBlockSize());
                fileCipher.init(encrypting, new ParametersWithIV(new KeyParameter(ekey), eiv));
                return fileCipher;
            }
        }

        throw new IllegalArgumentException(String.format("Unsupported algorithm: %s", alg));
    }

    /**
     * Simple version of buildCipherForFile, this time without the encryption data manipulation or file object.
     *
     * @param alg the encryption algorithm being used
     * @param key encryption key data bytes
     * @param iv initialization vector data bytes
     * @return a BlockCipher
     */
    public static BlockCipher buildCipher(Encryption alg, boolean encrypting, byte[] key, byte[] iv)
    {
        if (key.length != alg.keyByteLength) throw new IllegalArgumentException(
                String.format("Supplied key length %s != required key length %s", key.length, alg.keyByteLength)
        );
        if (alg.c.equals(Algorithms.SYMMETRIC_CIPHER.AES))
        {
            SICBlockCipher fileCipher = new SICBlockCipher(new AESEngine());
            if (iv.length != fileCipher.getBlockSize()) throw new IllegalArgumentException(
                    String.format("Supplied iv length %s != required iv length %s", iv.length, fileCipher.getBlockSize())
            );
            if (alg.m.equals(Algorithms.SYMMETRIC_MODE.CTR))
            {
                fileCipher.init(encrypting, new ParametersWithIV(new KeyParameter(key), iv));
                return fileCipher;
            }
        }

        else if (alg.c.equals(Algorithms.SYMMETRIC_CIPHER.TWOFISH))
        {
            SICBlockCipher fileCipher = new SICBlockCipher(new TwofishEngine());
            if (iv.length != fileCipher.getBlockSize()) throw new IllegalArgumentException(
                    String.format("Supplied iv length %s != required iv length %s", iv.length, fileCipher.getBlockSize())
            );
            if (alg.m.equals(Algorithms.SYMMETRIC_MODE.CTR))
            {
                fileCipher.init(encrypting, new ParametersWithIV(new KeyParameter(key), iv));
                return fileCipher;
            }
        }

        throw new IllegalArgumentException(String.format("Unsupported algorithm: %s", alg));
    }
}
