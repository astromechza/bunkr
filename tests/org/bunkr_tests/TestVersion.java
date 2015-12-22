package org.bunkr_tests;

import org.bunkr.Version;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

/**
 * Creator: benmeier
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
