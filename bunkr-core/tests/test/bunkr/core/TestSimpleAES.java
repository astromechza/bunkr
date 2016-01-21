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

package test.bunkr.core;

import org.bunkr.core.inventory.Algorithms;
import org.bunkr.core.inventory.Algorithms.Encryption;
import org.bunkr.core.utils.RandomMaker;
import org.bunkr.core.utils.SimpleAES;
import org.bouncycastle.crypto.CryptoException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created At: 2015-11-27
 */
public class TestSimpleAES
{
    @Test
    public void testSimple() throws CryptoException
    {
        byte[] k = RandomMaker.get(256);
        byte[] iv = RandomMaker.get(128);

        byte[] p = ("'Ha, Watson! It would appear that our bait, cast though it was over unknown waters, may have " +
                    "brought in a catch!'").getBytes();

        byte[] c = SimpleAES.encrypt(Encryption.AES256_CTR, p, k, iv);

        byte[] d = SimpleAES.decrypt(Encryption.AES256_CTR, c, k, iv);

        assertThat(p, is(equalTo(d)));
    }

    @Test
    public void testNoPaddingRequired() throws CryptoException
    {
        byte[] k = RandomMaker.get(256);
        byte[] iv = RandomMaker.get(128);

        byte[] p = ("").getBytes();

        byte[] c = SimpleAES.encrypt(Encryption.AES256_CTR, p, k, iv);

        byte[] d = SimpleAES.decrypt(Encryption.AES256_CTR, c, k, iv);

        assertThat(p, is(equalTo(d)));
    }
}
