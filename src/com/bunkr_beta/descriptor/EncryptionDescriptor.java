package com.bunkr_beta.descriptor;

import com.bunkr_beta.RandomMaker;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Creator: benmeier
 * Created At: 2015-12-03
 */
public class EncryptionDescriptor
{
    private static final int MINIMUM_AES_KEY_LENGTH = 256;
    private static final int DEFAULT_AES_KEY_LENGTH = 256;
    private static final int MINIMUM_PBKD2_ITERS = 4096;
    private static final int DEFAULT_PBKD2_ITERS = 10000;
    private static final int DEFAULT_PBKD2_SALT_LEN = 128;

    public final int pbkdf2Iterations;
    public final int aesKeyLength;
    public final byte[] pbkdf2Salt;

    @JsonCreator
    public EncryptionDescriptor(
            @JsonProperty("pbkdf2Iterations") int pbkdf2Iterations,
            @JsonProperty("aesKeyLength") int aesKeyLength,
            @JsonProperty("pbkdf2Salt") byte[] pbkdf2Salt
    )
    {
        if (pbkdf2Iterations < MINIMUM_PBKD2_ITERS)
            throw new IllegalArgumentException("pbkdf2Iterations must be at least 4096");

        if (aesKeyLength != MINIMUM_AES_KEY_LENGTH)
            throw new IllegalArgumentException("aesKeyLength must be 256");

        this.pbkdf2Iterations = pbkdf2Iterations;
        this.aesKeyLength = aesKeyLength;
        this.pbkdf2Salt = pbkdf2Salt;
    }

    public static EncryptionDescriptor makeDefaults()
    {
        return new EncryptionDescriptor(DEFAULT_PBKD2_ITERS, DEFAULT_AES_KEY_LENGTH, RandomMaker.get(DEFAULT_PBKD2_SALT_LEN));
    }
}
