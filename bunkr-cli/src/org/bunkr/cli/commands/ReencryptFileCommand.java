package org.bunkr.cli.commands;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.cli.CLI;
import org.bunkr.cli.ProgressBar;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.exceptions.CLIException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.IFFTraversalTarget;
import org.bunkr.core.inventory.InventoryPather;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.AbortableShutdownHook;
import org.bunkr.core.utils.Logging;
import org.bunkr.core.utils.Units;

import java.util.Arrays;

/**
 * Created At: 2016-01-23
 */
public class ReencryptFileCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_NO_PROGRESS = "noprogress";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("re-encrypt the given file using the current file security setting");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("source path in the archive");
        target.addArgument("--no-progress")
                .dest(ARG_NO_PROGRESS)
                .action(Arguments.storeTrue())
                .setDefault(false)
                .type(Boolean.class)
                .help("don't display a progress bar while importing the file");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        ArchiveInfoContext archive = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);
        IFFTraversalTarget target = InventoryPather.traverse(archive.getInventory(), args.getString(ARG_PATH));
        if (!target.isAFile()) throw new CLIException("'%s' is not a file.", args.getString(ARG_PATH));


        AbortableShutdownHook emergencyShutdownThread = new MetadataWriter.EnsuredMetadataWriter(archive, usp);
        Runtime.getRuntime().addShutdownHook(emergencyShutdownThread);

        FileInventoryItem targetFile = (FileInventoryItem) target;
        ProgressBar pb = new ProgressBar(120, targetFile.getActualSize(), "Exporting file: ");
        pb.setEnabled(!(Boolean) args.get(ARG_NO_PROGRESS));
        pb.startFresh();

        try
        {
            try (MultilayeredInputStream mis = new MultilayeredInputStream(archive, targetFile))
            {
                try (MultilayeredOutputStream mos = new MultilayeredOutputStream(archive, targetFile))
                {
                    byte[] buffer = new byte[(int) Units.MEBIBYTE];
                    int n;
                    while ((n = mis.read(buffer)) != -1)
                    {
                        Logging.debug("Read %d bytes", n);
                        mos.write(buffer, 0, n);
                        pb.inc(n);
                    }
                    Arrays.fill(buffer, (byte) 0);
                    Logging.debug("Finished reading");
                }
            }
            pb.finish();
        }
        finally
        {
            // This finally block is a basic attempt at handling bad problems like corrupted writes when saving a file.
            // if an exception was raised due to some IO issue, then we still want to write a hopefully correct
            // metadata section so that the file can be correctly read in future.
            MetadataWriter.write(archive, usp);
            Runtime.getRuntime().removeShutdownHook(emergencyShutdownThread);
        }
    }
}
