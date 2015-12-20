package org.bunkr.cli.commands;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.cli.CLI;
import org.bunkr.core.UserSecurityProvider;

import java.io.File;

/**
 * Creator: benmeier
 * Created At: 2015-12-16
 */
public class ChangePasswordCommand implements ICLICommand
{
    public static final String ARG_NEW_PASSWORD_FILE = "newpassword";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("change the password protecting the archive");

        target.addArgument("new-password")
                .dest(ARG_NEW_PASSWORD_FILE)
                .type(Arguments.fileType().verifyExists().verifyCanRead())
                .setDefault(new File("-"))
                .help("read the new archive password from the given file or '-' for stdin");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider currentSecurity = new UserSecurityProvider(makePasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        // force user to enter this BEFORE we load the file
        currentSecurity.getHashedPassword();

        ArchiveInfoContext context = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), currentSecurity);

        UserSecurityProvider newSecurity = new UserSecurityProvider(makePasswordProvider(args.get(ARG_NEW_PASSWORD_FILE)));
        MetadataWriter.write(context, newSecurity);
        System.out.println("Successfully changed password for achive");
    }
}
