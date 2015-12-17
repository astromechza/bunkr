package org.bunkr.utils;

import java.security.SecureRandom;

/**
 * Creator: benmeier
 * Created At: 2015-11-22
 */
public class RandomMaker
{
    private static SecureRandom randomSource = null;

    public static SecureRandom getRandomSource()
    {
        if (randomSource == null) randomSource = new SecureRandom();
        return randomSource;
    }

    public static byte[] get(int bits)
    {
        byte[] data = new byte[bits / 8];
        RandomMaker.getRandomSource().nextBytes(data);
        return data;
    }

    public static void fill(byte[] edata)
    {
        RandomMaker.getRandomSource().nextBytes(edata);
    }
}
