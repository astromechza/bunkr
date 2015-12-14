package org.bunkr.cli.passwords;

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
            System.err.println(
                    "Couldn't get Console instance. You might not be in a pseudo terminal or may be " +
                    "redirecting stdout or stdin in some way.\n\n" +

                    "Please use the '--password-file' cli option instead in this case."
            );
            System.exit(1);
        }
        return new String(console.readPassword("Enter password for archive: ")).getBytes();
    }
}
