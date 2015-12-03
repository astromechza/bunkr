package com.bunkr_beta.descriptor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Creator: benmeier
 * Created At: 2015-12-03
 */
public class CompressionDescriptor
{
    private static final String DEFAULT_COMPRESS_ALG = "ZLIB";
    public final String algorithm;

    @JsonCreator
    public CompressionDescriptor(
            @JsonProperty("algorithm") String algorithm)
    {
        if (!algorithm.toUpperCase().equals(DEFAULT_COMPRESS_ALG))
            throw new IllegalArgumentException("'ZLIB' is the only allowed compression algorithm");
        this.algorithm = algorithm;
    }

    public static CompressionDescriptor makeDefaults()
    {
        return new CompressionDescriptor(DEFAULT_COMPRESS_ALG);
    }
}
