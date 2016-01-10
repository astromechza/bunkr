package org.bunkr.cli.commands;

import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.cli.CLIPasswordPrompt;
import org.bunkr.core.exceptions.BaseBunkrException;
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

    default PasswordProvider makeCLIPasswordProvider(File pwFile, String prompt) throws IOException, BaseBunkrException
    {
        PasswordProvider passProv = new PasswordProvider(new CLIPasswordPrompt(prompt));
        if (pwFile != null && ! pwFile.getPath().equals("-")) passProv.setArchivePassword(pwFile);
        return passProv;
    }

    default PasswordProvider makeCLIPasswordProvider(File pwFile) throws IOException, BaseBunkrException
    {
        PasswordProvider passProv = new PasswordProvider(new CLIPasswordPrompt());
        if (pwFile != null && ! pwFile.getPath().equals("-")) passProv.setArchivePassword(pwFile);
        return passProv;
    }
}
