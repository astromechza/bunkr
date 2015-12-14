package org.bunkr.descriptor;

/**
 * Creator: benmeier
 * Created At: 2015-12-03
 */
public class CompressionDescriptor
{
    private static final String DEFAULT_COMPRESS_ALG = "ZLIB";
    public final String algorithm;

    public CompressionDescriptor(String algorithm)
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
