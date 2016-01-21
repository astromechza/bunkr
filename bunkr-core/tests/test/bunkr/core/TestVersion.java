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

import org.bunkr.core.Version;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

/**
 * Created At: 2015-12-14
 */
public class TestVersion
{
    @Test
    public void testValuesAssigned()
    {
        assertNotNull(Version.versionString);
        assertNotNull(Version.gitDate);
        assertNotNull(Version.gitHash);
        assertTrue(Version.versionMajor >= 0);
        assertTrue(Version.versionMinor >= 0);
        assertTrue(Version.versionBugfix >= 0);
    }

    @Test
    public void testVersionCheck()
    {
        assertTrue(Version.isCompatible(Version.versionMajor, Version.versionMinor, Version.versionBugfix, false));
        assertTrue(Version.isCompatible(Version.versionMajor, Version.versionMinor, Version.versionBugfix, true));

        assertTrue(Version.isCompatible(
                Version.versionMajor,
                Version.versionMinor,
                (byte) (Version.versionBugfix + 1),
                false)
        );
        assertFalse(Version.isCompatible(
                            Version.versionMajor,
                            Version.versionMinor,
                            (byte) (Version.versionBugfix + 1),
                            true)
        );
    }

    @Test
    public void testVersionCheckAssert() throws IOException
    {
        Version.assertCompatible(Version.versionMajor, Version.versionMinor, Version.versionBugfix, false);
        try
        {
            Version.assertCompatible((byte) (Version.versionMajor + 1), Version.versionMinor, Version.versionBugfix, true);
            fail("Should fail");
        }
        catch (IOException ignored) { }
    }
}
