package org.bunkr.cli.commands;

import org.bunkr.ArchiveBuilder;
import org.bunkr.cli.CLI;
import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.descriptor.CompressionDescriptor;
import org.bunkr.descriptor.EncryptionDescriptor;
import org.bunkr.exceptions.CLIException;
import org.bunkr.descriptor.Descriptor;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.File;

/**
 * Creator: benmeier
 * Created At: 2015-12-03
 */
public class CreateCommand implements ICLICommand
{
    public static final String ARG_OVERWRITE = "overwrite";
    public static final String ARG_NOENCRYPTION = "noencryption";
    public static final String ARG_NOCOMPRESSION = "nodecryption";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("create a new empty archive");
        target.addArgument("-o", "--overwrite")
                .dest(ARG_OVERWRITE)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("overwrite the existing file");
        target.addArgument("--no-encryption")
                .dest(ARG_NOENCRYPTION)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("the archive will not require a password");
        target.addArgument("--no-compression")
                .dest(ARG_NOCOMPRESSION)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("the archive will not use compression on its files");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        PasswordProvider passProv = makePasswordProvider(args.get(CLI.ARG_PASSWORD_FILE));
        passProv.getHashedArchivePassword();

        File archiveFile = args.get(CLI.ARG_ARCHIVE_PATH);
        if (archiveFile.exists() && !args.getBoolean(ARG_OVERWRITE))
            throw new CLIException("File %s already exists. Pass --overwrite in order to overwrite it.", archiveFile.getAbsolutePath());

        EncryptionDescriptor ed = (args.getBoolean(ARG_NOENCRYPTION)) ? null : EncryptionDescriptor.makeDefaults();
        CompressionDescriptor cd = (args.getBoolean(ARG_NOCOMPRESSION)) ? null : CompressionDescriptor.makeDefaults();

        ArchiveBuilder.createNewEmptyArchive(archiveFile, new Descriptor(ed, cd), passProv);
        System.out.println(String.format("Created new archive %s", archiveFile.getAbsolutePath()));
    }


}
