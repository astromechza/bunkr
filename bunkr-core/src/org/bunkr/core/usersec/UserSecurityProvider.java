package org.bunkr.core.usersec;

import org.bunkr.core.exceptions.IllegalPasswordException;

/**
 * Creator: benmeier
 * Created At: 2015-12-20
 */
public class UserSecurityProvider
{
    private PasswordProvider passwordProvider;

    public UserSecurityProvider(PasswordProvider passwordProvider)
    {
        this.passwordProvider = passwordProvider;
    }

    public byte[] getHashedPassword() throws IllegalPasswordException
    {
        return this.passwordProvider.getHashedArchivePassword();
    }
}
