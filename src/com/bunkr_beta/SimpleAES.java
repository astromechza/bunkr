package com.bunkr_beta;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.util.Arrays;

/**
 * Creator: benmeier
 * Created At: 2015-11-27
 */
public class SimpleAES
{
    private static byte[] runAES(boolean doEncrypt, byte[] subject, byte[] key, byte[] iv) throws CryptoException
    {
        // set up padded buffered cipher
        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                new SICBlockCipher(new AESEngine()), new PKCS7Padding()
        );

        // init with key and if
        cipher.init(doEncrypt, new ParametersWithIV(new KeyParameter(key), iv));

        // construct output buffer
        byte[] output = new byte[cipher.getOutputSize(subject.length)];

        // process all da bytes
        int cursor = cipher.processBytes(subject, 0, subject.length, output, 0);

        // process the last bytes from the buffer
        cipher.doFinal(output, cursor);
        return output;
    }

    public static byte[] encrypt(byte[] input, byte[] key, byte[] iv) throws CryptoException
    {
        return runAES(true, input, key, iv);
    }

    public static byte[] decrypt(byte[] input, byte[] key, byte[] iv) throws CryptoException
    {
        byte[] withPadding = runAES(false, input, key, iv);
        int trueLength = input.length;
        while(trueLength > 0 && withPadding[trueLength - 1] == 0x0) trueLength--;
        byte[] nonPadded = new byte[trueLength];
        System.arraycopy(withPadding, 0, nonPadded, 0, trueLength);
        Arrays.fill(withPadding, (byte) 0);
        return nonPadded;
    }
}
