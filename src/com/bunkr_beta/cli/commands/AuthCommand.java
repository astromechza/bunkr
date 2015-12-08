package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.exceptions.CLIException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

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
        try
        {
            new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), makePasswordProvider(args));
            System.out.println("Decryption Succeeded");
        }
        catch (CryptoException e)
        {
            throw new CLIException("Decryption failed: %s", e.getMessage());
        }
    }
}
