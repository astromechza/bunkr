package com.bunkr_beta.cli;

import com.bunkr_beta.interfaces.IPasswordPrompter;

import java.io.Console;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public class CLIPasswordPrompt implements IPasswordPrompter
{
    @Override
    public byte[] getPassword()
    {
        Console console = System.console();
        if (console == null)
        {
            System.err.println("Couldn't get Console instance");
            System.exit(1);
        }
        return new String(console.readPassword("Enter password for archive: ")).getBytes();
    }
}
