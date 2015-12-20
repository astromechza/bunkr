package org.bunkr.descriptor;

/**
 * Created by benmeier on 15/10/25.
 *
 * This class contains publically readable plaintext information for how to read and decrypt the archive.
 */
public class Descriptor
{
    private final EncryptionDescriptor encryption;

    public Descriptor(EncryptionDescriptor encryption)
    {
        this.encryption = encryption;
    }

    public EncryptionDescriptor getEncryption()
    {
        return encryption;
    }

    public boolean hasEncryption()
    {
        return this.encryption != null;
    }
}
