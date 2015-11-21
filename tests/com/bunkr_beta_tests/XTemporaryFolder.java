package com.bunkr_beta_tests;

import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

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
}
