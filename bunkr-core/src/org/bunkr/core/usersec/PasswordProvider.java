package org.bunkr.core.usersec;

import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bunkr.core.exceptions.IllegalPasswordException;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Set;

/**
 * Creator: benmeier
 * Created At: 2015-12-01
 */
public class PasswordProvider
{
    private static final int MINIMUM_PASSWORD_LENGTH = 8;

    private byte[] hashedArchivePassword;
    private IPasswordPrompter prompter;

    public PasswordProvider()
    {
        this.hashedArchivePassword = null;
        this.prompter = null;
    }

    public PasswordProvider(IPasswordPrompter prompter)
    {
        this.hashedArchivePassword = null;
        this.prompter = prompter;
    }

    public byte[] getHashedArchivePassword() throws IllegalPasswordException
    {
        if (hashedArchivePassword == null)
        {
            if (prompter != null)
            {
                setArchivePassword(prompter.getPassword());
            }
            else
            {
                throw new IllegalArgumentException("Password requested, but no password prompt available.");
            }
        }
        return hashedArchivePassword;
    }

    public void setArchivePassword(byte[] archivePassword) throws IllegalPasswordException
    {
        checkPassword(archivePassword);
        this.hashedArchivePassword = hash(archivePassword);
    }

    public void setArchivePassword(File passwordFile) throws IOException, IllegalPasswordException
    {
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(passwordFile.toPath());
        if (permissions.contains(PosixFilePermission.OTHERS_READ))
            throw new IllegalPasswordException("For security reasons, the password file may not be world-readable.");
        if (permissions.contains(PosixFilePermission.OTHERS_WRITE))
            throw new IllegalPasswordException("For security reasons, the password file may not be world-writeable.");
        try(BufferedReader br = new BufferedReader(new FileReader(passwordFile)))
        {
            this.setArchivePassword(br.readLine().getBytes());
        }
    }

    public void clearArchivePassword()
    {
        if (this.hashedArchivePassword != null) Arrays.fill(this.hashedArchivePassword, (byte) 0);
        this.hashedArchivePassword = null;
    }

    public IPasswordPrompter getPrompter()
    {
        return prompter;
    }

    public void setPrompter(IPasswordPrompter prompter)
    {
        this.prompter = prompter;
    }

    @Override
    protected void finalize() throws Throwable
    {
        this.clearArchivePassword();
        super.finalize();
    }

    private byte[] hash(byte[] input)
    {
        GeneralDigest digest = new SHA256Digest();
        digest.update(input, 0, input.length);
        byte[] buffer = new byte[digest.getDigestSize()];
        digest.doFinal(buffer, 0);
        Arrays.fill(input, (byte) 0);
        return buffer;
    }

    /**
     * This method makes sure the password meets the required complexity checks. It should be purely printable
     * ascii characters.
     */
    private void checkPassword(byte[] input) throws IllegalPasswordException
    {
        if (input.length < MINIMUM_PASSWORD_LENGTH) throw new IllegalPasswordException(
            "Password does not meet complexity requirement: it must be at least %d characters.",
            MINIMUM_PASSWORD_LENGTH
        );

        for (byte b : input)
        {
            if (b < (byte) 0x20) throw new IllegalPasswordException(
                "Password does not meet validity requirement: cannot contain byte %s",
                DatatypeConverter.printByte(b)
            );
            if (b > (byte) 0x7E) throw new IllegalPasswordException(
                "Password does not meet validity requirement: cannot contain byte %s",
                DatatypeConverter.printByte(b)
            );
        }
    }
}
