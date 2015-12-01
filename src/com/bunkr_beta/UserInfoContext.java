package com.bunkr_beta;

import com.bunkr_beta.interfaces.IPasswordPrompter;

/**
 * Creator: benmeier
 * Created At: 2015-12-01
 */
public class UserInfoContext
{
    private byte[] archivePassword;
    private IPasswordPrompter prompter;

    public UserInfoContext()
    {
        this.archivePassword = null;
        this.prompter = null;
    }

    public UserInfoContext(IPasswordPrompter prompter)
    {
        this.archivePassword = null;
        this.prompter = prompter;
    }

    public UserInfoContext(byte[] password)
    {
        this.archivePassword = password;
        this.prompter = null;
    }

    public byte[] getArchivePassword()
    {
        if (archivePassword == null)
        {
            if (prompter != null)
            {
                this.archivePassword = prompter.getPassword();
            }
            else
            {
                throw new IllegalArgumentException("Password requested, but no password prompt available.");
            }
        }
        return archivePassword;
    }

    public void setArchivePassword(byte[] archivePassword)
    {
        this.archivePassword = archivePassword;
    }

    public IPasswordPrompter getPrompter()
    {
        return prompter;
    }

    public void setPrompter(IPasswordPrompter prompter)
    {
        this.prompter = prompter;
    }
}
