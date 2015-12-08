package com.bunkr_beta.cli.commands;

import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.cli.CLIPasswordPrompt;
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
        if (args.get("password-file") != null)
        {
            passProv.setArchivePassword((File) args.get("password-file"));
        }
        return passProv;
    }


}
