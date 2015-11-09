package com.bunkr_beta;

import com.bunkr_beta.demos.BuildExampleArchive;
import com.bunkr_beta.demos.BuildExampleArchive2;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;

public class Main
{
    public static void main(String[] args)
            throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException,
            NoSuchPaddingException, InvalidAlgorithmParameterException
    {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        BuildExampleArchive2.run();
    }
}
