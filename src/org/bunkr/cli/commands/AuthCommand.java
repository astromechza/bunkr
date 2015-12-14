package org.bunkr.cli.commands;

import org.bunkr.ArchiveInfoContext;
import org.bunkr.cli.CLI;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

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
        new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), makePasswordProvider(args));
        System.out.println("Decryption Succeeded");
    }
}
