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

package org.bunkr.core.inventory;

/**
 * Creator: benmeier
 * Created At: 2016-01-10
 */
public class Algorithms
{
    public enum SYMMETRIC_CIPHER {AES, TWOFISH}
    public enum SYMMETRIC_MODE {CTR}

    public enum Encryption
    {
        NONE(null, 0, 0, null),
        AES128_CTR(SYMMETRIC_CIPHER.AES, 16, 16, SYMMETRIC_MODE.CTR),
        AES256_CTR(SYMMETRIC_CIPHER.AES, 32, 16,SYMMETRIC_MODE.CTR),
        TWOFISH128_CTR(SYMMETRIC_CIPHER.TWOFISH, 16, 16, SYMMETRIC_MODE.CTR),
        TWOFISH256_CTR(SYMMETRIC_CIPHER.TWOFISH, 32, 16, SYMMETRIC_MODE.CTR);

        public final SYMMETRIC_CIPHER c;
        public final int keyByteLength;
        public final int ivByteLength;
        public final SYMMETRIC_MODE m;

        Encryption(SYMMETRIC_CIPHER c, int keyByteLength, int ivByteLength, SYMMETRIC_MODE m)
        {
            this.c = c;
            this.keyByteLength = keyByteLength;
            this.ivByteLength = ivByteLength;
            this.m = m;
        }
    }
}
