package com.bunkr_beta;

import java.security.SecureRandom;

/**
 * Creator: benmeier
 * Created At: 2015-11-22
 */
public class KeyMaker
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
        KeyMaker.getRandomSource().nextBytes(data);
        return data;
    }
}
