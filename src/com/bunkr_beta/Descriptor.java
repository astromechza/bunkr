package com.bunkr_beta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by benmeier on 15/10/25.
 *
 * This class contains publically readable plaintext information for how to read and decrypt the archive.
 */
public class Descriptor
{
    public final EncryptionDescriptor encryption;
    public final CompressionDescriptor compression;

    @JsonCreator
    public Descriptor(
            @JsonProperty("encryption") EncryptionDescriptor encryption,
            @JsonProperty("compression") CompressionDescriptor compression
    )
    {
        this.encryption = encryption;
        this.compression = compression;
    }

    public static class EncryptionDescriptor
    {
        public final String symmetricAlgorithm;
        public final String asymmetricAlgorithm;
        public final byte[] encryptedInventoryKey;

        @JsonCreator
        public EncryptionDescriptor(
                @JsonProperty("symmetricAlgorithm") String symmetricAlgorithm,
                @JsonProperty("asymmetricAlgorithm") String asymmetricAlgorithm,
                @JsonProperty("encryptedInventoryKey") byte[] encryptedInventoryKey)
        {
            this.symmetricAlgorithm = symmetricAlgorithm;
            this.asymmetricAlgorithm = asymmetricAlgorithm;
            this.encryptedInventoryKey = encryptedInventoryKey;
        }
    }

    public static class CompressionDescriptor
    {
        public final String algorithm;

        @JsonCreator
        public CompressionDescriptor(
                @JsonProperty("symmetricAlgorithm") String algorithm)
        {
            if (!algorithm.toLowerCase().equals("zlib"))
                throw new IllegalArgumentException("'zlib' is the only allowed compression algorithm");
            this.algorithm = algorithm;
        }
    }
}
