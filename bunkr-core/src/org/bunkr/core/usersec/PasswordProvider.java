/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.core.usersec;

import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bunkr.core.exceptions.IllegalPasswordException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Set;

/**
 * Created At: 2015-12-01
 */
public class PasswordProvider
{
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
        PasswordRequirements.checkPasses(archivePassword);
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
}
