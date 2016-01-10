package org.bunkr.cli;

import org.bunkr.core.usersec.IPasswordPrompter;

import java.io.Console;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public class CLIPasswordPrompt implements IPasswordPrompter
{
    private final String prompt;

    public CLIPasswordPrompt(String prompt)
    {
        this.prompt = prompt;
    }

    public CLIPasswordPrompt()
    {
        this("Enter password:");
    }

    @Override
    public byte[] getPassword()
    {
        Console console = System.console();
        if (console == null)
        {
            System.err.println(
                    "Couldn't get Console instance. You might not be in a pseudo terminal or may be " +
                    "redirecting stdout or stdin in some way.\n\n" +

                    "Please use the '--password-file' cli option instead in this case."
            );
            System.exit(1);
        }
        return new String(console.readPassword(this.prompt)).getBytes();
    }
}
