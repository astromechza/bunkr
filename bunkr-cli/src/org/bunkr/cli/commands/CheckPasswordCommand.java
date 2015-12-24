package org.bunkr.cli.commands;

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.cli.CLI;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.core.usersec.UserSecurityProvider;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public class CheckPasswordCommand implements ICLICommand
{
    @Override
    public void buildParser(Subparser target)
    {
        target.help("test a password or password file against the archive");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makePasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);
        System.out.println("Decryption Succeeded");
    }
}
