package com.bunkr_beta_tests;

import com.bunkr_beta.Version;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

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
    }
}
