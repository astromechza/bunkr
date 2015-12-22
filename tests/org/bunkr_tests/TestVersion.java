package org.bunkr_tests;

import org.bunkr.Version;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Creator: benmeier
 * Created At: 2015-12-14
 */
public class TestVersion
{
    @Test
    public void testValuesAssigned()
    {
        assertNotNull(Version.versionNumber);
        assertNotNull(Version.gitDate);
        assertNotNull(Version.gitHash);
        assertTrue(Version.versionMajor >= 0);
        assertTrue(Version.versionMinor >= 0);
        assertTrue(Version.versionBugfix >= 0);
    }
}
