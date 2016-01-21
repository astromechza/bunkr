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

package org.bunkr.core.usersec;

import org.bunkr.core.exceptions.IllegalPasswordException;

import javax.xml.bind.DatatypeConverter;

/**
 * Created At: 2015-12-25
 */
public class PasswordRequirements
{
    public static final int MINIMUM_PASSWORD_LENGTH = 8;
    public static final int CHAR_SPACE = 0x20;
    public static final int CHAR_TILDE = 0x7E;

    public static void checkPasses(byte[] input) throws IllegalPasswordException
    {
        if (input.length < MINIMUM_PASSWORD_LENGTH) throw new IllegalPasswordException(
                "Password must be at least %d characters.",
                MINIMUM_PASSWORD_LENGTH
        );

        for (byte b : input)
        {
            if (b < (byte) CHAR_SPACE) throw new IllegalPasswordException(
                    "Password cannot contain byte %s",
                    DatatypeConverter.printByte(b)
            );
            if (b > (byte) CHAR_TILDE) throw new IllegalPasswordException(
                    "Password cannot contain byte %s",
                    DatatypeConverter.printByte(b)
            );
        }
    }
}
