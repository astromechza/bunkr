package com.bunkr_beta_tests;

import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * Creator: benmeier
 * Created At: 2015-11-21
 */
public class XTemporaryFolder extends TemporaryFolder
{
    public File newPrefixedFile(String prefix) throws IOException
    {
        return File.createTempFile("junit-" + prefix , "", this.getRoot());
    }

    private static final SecureRandom random = new SecureRandom();
    public File newFilePath(String prefix, String suffix)
    {
        String name = "" + prefix + Long.toString(random.nextLong()) + suffix + ".tmp";
        return new File(this.getRoot(), name);
    }
    public File newFilePath() throws IOException
    {
        return this.newFilePath("", "");
    }
}
