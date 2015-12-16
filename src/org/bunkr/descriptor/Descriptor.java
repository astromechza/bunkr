package org.bunkr.descriptor;

/**
 * Created by benmeier on 15/10/25.
 *
 * This class contains publically readable plaintext information for how to read and decrypt the archive.
 */
public class Descriptor
{
    private final EncryptionDescriptor encryption;
    private final CompressionDescriptor compression;

    public Descriptor(EncryptionDescriptor encryption, CompressionDescriptor compression
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

    public boolean hasEncryption()
    {
        return this.encryption != null;
    }

    public boolean hasCompression()
    {
        return this.compression != null;
    }
}
