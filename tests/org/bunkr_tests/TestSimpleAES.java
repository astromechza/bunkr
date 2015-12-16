package org.bunkr_tests;

import org.bunkr.utils.RandomMaker;
import org.bunkr.utils.SimpleAES;
import org.bouncycastle.crypto.CryptoException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Creator: benmeier
 * Created At: 2015-11-27
 */
public class TestSimpleAES
{
    @Test
    public void testSimple() throws CryptoException
    {
        byte[] k = RandomMaker.get(256);
        byte[] iv = RandomMaker.get(256);

        byte[] p = ("'Ha, Watson! It would appear that our bait, cast though it was over unknown waters, may have " +
                    "brought in a catch!'").getBytes();

        byte[] c = SimpleAES.encrypt(p, k, iv);

        byte[] d = SimpleAES.decrypt(c, k, iv);

        assertThat(p, is(equalTo(d)));
    }

    @Test
    public void testNoPaddingRequired() throws CryptoException
    {
        byte[] k = RandomMaker.get(256);
        byte[] iv = RandomMaker.get(256);

        byte[] p = ("").getBytes();

        byte[] c = SimpleAES.encrypt(p, k, iv);

        byte[] d = SimpleAES.decrypt(c, k, iv);

        assertThat(p, is(equalTo(d)));
    }
}
