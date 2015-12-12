package com.bunkr_beta.cli.commands;

import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.cli.passwords.CLIPasswordPrompt;
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

    default PasswordProvider makePasswordProvider(Namespace args) throws IOException
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
