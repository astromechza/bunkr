package org.bunkr.cli.commands;

import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.AbortableShutdownHook;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.cli.CLI;
import org.bunkr.cli.ProgressBar;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.exceptions.CLIException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.IFFContainer;
import org.bunkr.core.inventory.IFFTraversalTarget;
import org.bunkr.core.inventory.InventoryPather;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Creator: benmeier
 * Created At: 2015-12-08
 */
public class ImportFileCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_SOURCE_FILE = "source";
    public static final String ARG_TAGS = "tags";
    public static final String ARG_NO_PROGRESS = "noprogress";


    @Override
    public void buildParser(Subparser target)
    {
        target.help("write or import a file");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("destination path in the archive");
        target.addArgument("source")
                .dest(ARG_SOURCE_FILE)
                .type(Arguments.fileType().acceptSystemIn().verifyExists().verifyCanRead())
                .help("file to import or - for stdin");
        target.addArgument("-t", "--tags")
                .dest(ARG_TAGS)
                .nargs("*")
                .setDefault(new ArrayList<>())
                .type(String.class)
                .help("a list of tags to associate with this file");
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
        UserSecurityProvider usp = new UserSecurityProvider(makePasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);

        if (args.getString(ARG_PATH).equals("/")) throw new CLIException("Cannot import as /.");

        IFFTraversalTarget parent = InventoryPather.traverse(aic.getInventory(),
                                                             InventoryPather.dirname(args.getString(ARG_PATH)));
        if (parent.isAFile()) throw new CLIException("Cannot create file as a child of a file.");
        IFFContainer container = (IFFContainer) parent;

        IFFTraversalTarget target = container.findFileOrFolder(InventoryPather.baseName(args.getString(ARG_PATH)));

        FileInventoryItem targetFile;
        if (target != null)
        {
            if (target.isAFolder()) throw new CLIException("Cannot overwrite folder with a file.");
            targetFile = (FileInventoryItem) target;
        }
        else
        {
            targetFile = new FileInventoryItem(InventoryPather.baseName(args.getString(ARG_PATH)));
            ((IFFContainer) parent).getFiles().add(targetFile);
        }

        // if tags have been supplied, change the tags associated with the target file
        if (args.getList(ARG_TAGS).size() > 0) targetFile.setCheckTags(new HashSet<>(args.getList(ARG_TAGS)));

        File inputFile = args.get(ARG_SOURCE_FILE);

        AbortableShutdownHook emergencyShutdownThread = new EnsuredMetadataWriter(aic, usp);
        Runtime.getRuntime().addShutdownHook(emergencyShutdownThread);

        boolean noProgress = (Boolean) getOrDefault(args.get(ARG_NO_PROGRESS), false);
        try
        {
            if (inputFile.getPath().equals("-"))
            {
                importFileFromStream(aic, targetFile, System.in, !noProgress);
            }
            else
            {
                FileChannel fc = new RandomAccessFile(inputFile, "r").getChannel();
                try (InputStream fis = Channels.newInputStream(fc))
                {
                    importFileFromStream(aic, targetFile, fis, inputFile.length(), !noProgress);
                }
            }
        }
        finally
        {
            // This finally block is a basic attempt at handling bad problems like corrupted writes when saving a file.
            // if an exception was raised due to some IO issue, then we still want to write a hopefully correct
            // metadata section so that the file can be correctly read in future.
            MetadataWriter.write(aic, usp);
            Runtime.getRuntime().removeShutdownHook(emergencyShutdownThread);
        }
    }

    private void importFileFromStream(ArchiveInfoContext context, FileInventoryItem target,
                                      InputStream is, boolean showProgress) throws IOException
    {
        importFileFromStream(context, target, is, is.available(), showProgress);
    }

    private void importFileFromStream(ArchiveInfoContext context, FileInventoryItem target,
                                      InputStream is, long expectedBytes, boolean showProgress) throws IOException
    {
        ProgressBar pb = new ProgressBar(120, expectedBytes, "Importing file: ", showProgress);
        pb.startFresh();

        try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, target))
        {
            byte[] buffer = new byte[1024 * 1024];
            int n;
            while ((n = is.read(buffer)) != -1)
            {
                bwos.write(buffer, 0, n);
                pb.inc(n);
            }
            Arrays.fill(buffer, (byte) 0);
        }

        pb.finish();
    }

    private static class EnsuredMetadataWriter extends AbortableShutdownHook
    {
        private final ArchiveInfoContext context;
        private final UserSecurityProvider prov;

        public EnsuredMetadataWriter(ArchiveInfoContext context, UserSecurityProvider prov)
        {
            this.context = context;
            this.prov = prov;
        }

        @Override
        public void innerRun()
        {
            try
            {
                System.err.println("Performing emergency metadata write for recovery");
                MetadataWriter.write(this.context, this.prov);
            }
            catch (CryptoException | IOException | BaseBunkrException e)
            {
                e.printStackTrace();
            }
        }
    }

    private Object getOrDefault(Object v, Object d)
    {
        if (v == null) return d;
        return v;
    }
}
