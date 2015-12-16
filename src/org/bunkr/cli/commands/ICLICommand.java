package org.bunkr.cli.commands;

import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.cli.CLI;
import org.bunkr.cli.passwords.CLIPasswordPrompt;
import org.bunkr.exceptions.BaseBunkrException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.File;
import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public interface ICLICommand
{
    void buildParser(Subparser target);
    void handle(Namespace args) throws Exception;

    default PasswordProvider makePasswordProvider(Namespace args) throws IOException, BaseBunkrException
    {
        PasswordProvider passProv = new PasswordProvider(new CLIPasswordPrompt());
        File f = args.get(CLI.ARG_PASSWORD_FILE);
        if (f != null && ! f.getPath().equals("-"))
        {
            passProv.setArchivePassword((File) args.get(CLI.ARG_PASSWORD_FILE));
        }
        return passProv;
    }
}
