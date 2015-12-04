package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.cli.CLIException;
import com.bunkr_beta.cli.CLIPasswordPrompt;
import com.bunkr_beta.interfaces.ICLICommand;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

import java.io.File;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public class AuthCommand implements ICLICommand
{
    @Override
    public void buildParser(Subparser target)
    {
        target.help("check for password authentication to open the archive");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        File archiveFile = new File(args.getString("archive"));
        PasswordProvider passProv = new PasswordProvider(new CLIPasswordPrompt());
        if (args.getString("password-file") != null)
        {
            passProv.setArchivePassword(new File(args.getString("password-file")));
        }

        try
        {
            new ArchiveInfoContext(archiveFile, passProv);
            System.out.println("Decryption Succeeded");
        }
        catch (CryptoException e)
        {
            throw new CLIException("Decryption failed: %s", e.getMessage());
        }
    }
}
