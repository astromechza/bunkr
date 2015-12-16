package org.bunkr.cli.commands;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.ArchiveInfoContext;
import org.bunkr.MetadataWriter;
import org.bunkr.cli.CLI;
import org.bunkr.cli.passwords.PasswordProvider;

import java.io.File;

/**
 * Creator: benmeier
 * Created At: 2015-12-16
 */
public class ChangePasswordCommand implements ICLICommand
{
    public static final String ARG_NEW_PASSWORD = "newpassword";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("change the password protecting the archive");

        target.addArgument("new-password")
                .dest(ARG_NEW_PASSWORD)
                .type(Arguments.fileType().verifyExists().verifyCanRead())
                .setDefault(new File("-"))
                .help("read the new archive password from the given file or '-' for stdin");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        ArchiveInfoContext context = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), makePasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        PasswordProvider newPassword = makePasswordProvider(args.get(ARG_NEW_PASSWORD));
        MetadataWriter.write(context, newPassword);
        System.out.println("Successfully changed password for achive");
    }
}
