package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.cli.CLIPasswordPrompt;
import com.bunkr_beta.interfaces.ICLICommand;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
    public void handle(Namespace args) throws IOException
    {
        File archiveFile = new File(args.getString("archive"));
        PasswordProvider passProv = new PasswordProvider(new CLIPasswordPrompt());
        if (args.getString("password-file") != null)
        {
            try(BufferedReader br = new BufferedReader(new FileReader(args.getString("password-file"))))
            {
                passProv.setArchivePassword(br.readLine().getBytes());
            }
        }

        try
        {
            new ArchiveInfoContext(archiveFile, passProv);
            System.out.println("Decryption Succeeded");
        }
        catch (CryptoException e)
        {
            System.err.println("Decryption Failed");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
