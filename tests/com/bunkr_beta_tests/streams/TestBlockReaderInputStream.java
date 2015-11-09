package com.bunkr_beta_tests.streams;

import com.bunkr_beta.ArchiveBuilder;
import com.bunkr_beta.Descriptor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public class TestBlockReaderInputStream
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testBasicReading() throws IOException, NoSuchAlgorithmException
    {
        File tempFile = folder.newFile();

    }
}
