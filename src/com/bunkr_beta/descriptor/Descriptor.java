package com.bunkr_beta.descriptor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by benmeier on 15/10/25.
 *
 * This class contains publically readable plaintext information for how to read and decrypt the archive.
 */
public class Descriptor
{
    private final EncryptionDescriptor encryption;
    private final CompressionDescriptor compression;

    @JsonCreator
    public Descriptor(
            @JsonProperty("encryption") EncryptionDescriptor encryption,
            @JsonProperty("compression") CompressionDescriptor compression
    )
    {
        this.encryption = encryption;
        this.compression = compression;
    }

    public static Descriptor makeDefaults()
    {
        return new Descriptor(EncryptionDescriptor.makeDefaults(), CompressionDescriptor.makeDefaults());
    }

    public CompressionDescriptor getCompression()
    {
        return compression;
    }

    public EncryptionDescriptor getEncryption()
    {
        return encryption;
    }
}
