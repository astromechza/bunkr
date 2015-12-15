package org.bunkr_tests.streams;

import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bunkr.IO;
import org.bunkr.RandomMaker;
import org.bunkr.exceptions.IntegrityHashError;
import org.bunkr.fragmented_range.FragmentedRange;
import org.bunkr.inventory.FileInventoryItem;
import org.bunkr.streams.input.BlockReaderInputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public class TestBlockReaderInputStream
{
    public static final String content = "abababababababababababababababab" +
            "cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcd" +
            "efefefefefEfefefefefefefefefefef" +
            "ghghghghghghghghghghghghghghghgh" +
            "ijijijijijijijiji";

    public static final String suffixPad = "xxxxxxxxxxxxxxx";
    public static final String badSuffixPad = "yyyyyyyyyyyyyyy";

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    public byte[] hashUp(byte[] input)
    {
        GeneralDigest d = new SHA1Digest();
        d.update(input, 0, input.length);
        byte[] b = new byte[d.getDigestSize()];
        d.doFinal(b, 0);
        return b;
    }

    public File buildFake() throws IOException
    {
        File f = folder.newFile();
        try(DataOutputStream dos = new DataOutputStream(new FileOutputStream(f)))
        {
            dos.write(RandomMaker.get((int) BlockReaderInputStream.DATABLOCKS_START * 8));
            dos.write((content + suffixPad).getBytes());
        }
        return f;
    }

    @Test
    public void testBasicReading() throws IOException
    {
        File f = buildFake();
        FileInventoryItem fakeFile = new FileInventoryItem("fake");
        fakeFile.setSizeOnDisk(32 * 4 + 17);
        fakeFile.setBlocks(new FragmentedRange(0, 5));
        fakeFile.setIntegrityHash(this.hashUp((content + suffixPad).getBytes()));
        BlockReaderInputStream bis = new BlockReaderInputStream(f, 32, fakeFile);
        String all = IO.readNByteString(bis, 32 * 4 + 17);
        assertThat(all, is(equalTo(content)));
        assertTrue(bis.doesHashMatch());
    }

    @Test
    public void testReadingBadHash() throws IOException
    {
        File f = buildFake();
        FileInventoryItem fakeFile = new FileInventoryItem("fake");
        fakeFile.setSizeOnDisk(32 * 4 + 17);
        fakeFile.setBlocks(new FragmentedRange(0, 5));
        fakeFile.setIntegrityHash(this.hashUp((content + badSuffixPad).getBytes()));
        BlockReaderInputStream bis = new BlockReaderInputStream(f, 32, fakeFile);
        try
        {
            IO.readNByteString(bis, 32 * 4 + 17);
            fail("Should have raised integrityhasherror");
        }
        catch (IntegrityHashError ignored) {}
        assertFalse(bis.doesHashMatch());
    }

    @Test
    public void testSkip() throws IOException
    {
        File f = buildFake();
        FileInventoryItem fakeFile = new FileInventoryItem("fake");
        fakeFile.setSizeOnDisk(32 * 4 + 17);
        fakeFile.setBlocks(new FragmentedRange(0, 5));
        fakeFile.setIntegrityHash(this.hashUp((content + suffixPad).getBytes()));
        BlockReaderInputStream bis = new BlockReaderInputStream(f, 32, fakeFile);
        assertThat(bis.read(), is(equalTo(97)));
        assertThat(bis.read(), is(equalTo(98)));
        assertThat(bis.skip(72), is(equalTo(72L)));
        assertThat(bis.read(), is(equalTo(69)));
        assertThat(bis.available(), is(equalTo(70)));
        assertThat(bis.skip(10000000), is(equalTo(70L)));
        assertThat(bis.skip(1), is(equalTo(-1L)));
        bis.close();
    }

    @Test
    public void testRead() throws IOException
    {
        File f = buildFake();
        FileInventoryItem fakeFile = new FileInventoryItem("fake");
        fakeFile.setSizeOnDisk(32 * 4 + 17);
        fakeFile.setBlocks(new FragmentedRange(0, 5));
        fakeFile.setIntegrityHash(this.hashUp((content + suffixPad).getBytes()));
        BlockReaderInputStream bis = new BlockReaderInputStream(f, 32, fakeFile);
        assertThat(bis.read(), is(equalTo(97)));
        assertThat(bis.read(), is(equalTo(98)));
        byte[] buffer = new byte[200];
        assertThat(bis.read(buffer, 0, 72), is(equalTo(72)));
        assertThat(bis.read(), is(equalTo(69)));
        assertThat(bis.available(), is(equalTo(70)));
        assertThat(bis.read(buffer, 0, 100000), is(equalTo(70)));
        assertThat(bis.read(), is(equalTo(-1)));
        bis.close();
    }

    @Test
    public void testEdges() throws IOException
    {
        File f = buildFake();
        try
        {
            FileInventoryItem fakeFile = new FileInventoryItem("fake");
            fakeFile.setSizeOnDisk(32 * 10);
            fakeFile.setBlocks(new FragmentedRange(0, 5));
            fakeFile.setIntegrityHash(this.hashUp((content + suffixPad).getBytes()));
            new BlockReaderInputStream(f, 32, fakeFile);
            fail("bad datalength");
        }
        catch (IllegalArgumentException ignored) {}

        FileInventoryItem fakeFile = new FileInventoryItem("fake");
        fakeFile.setSizeOnDisk(32 * 4 + 17);
        fakeFile.setBlocks(new FragmentedRange(0, 5));
        BlockReaderInputStream bis = new BlockReaderInputStream(f, 32, fakeFile);
        assertFalse(bis.markSupported());
    }

}
