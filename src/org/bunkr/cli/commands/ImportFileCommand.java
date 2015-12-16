package org.bunkr.cli.commands;

import org.bunkr.utils.AbortableShutdownHook;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.cli.CLI;
import org.bunkr.cli.ProgressBar;
import org.bunkr.exceptions.BaseBunkrException;
import org.bunkr.exceptions.CLIException;
import org.bunkr.inventory.FileInventoryItem;
import org.bunkr.inventory.IFFContainer;
import org.bunkr.inventory.IFFTraversalTarget;
import org.bunkr.inventory.InventoryPather;
import org.bunkr.streams.output.MultilayeredOutputStream;
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
                .type(Arguments.fileType().acceptSystemIn().verifyCanRead())
                .help("file to import or - for stdin");
        target.addArgument("-t", "--tags")
                .dest(ARG_TAGS)
                .nargs("*")
                .setDefault(new ArrayList<>())
                .type(String.class)
                .help("a list of tags to associate with this file");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        PasswordProvider passProv = makePasswordProvider(args.get(CLI.ARG_PASSWORD_FILE));
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), passProv);

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

        AbortableShutdownHook emergencyShutdownThread = new EnsuredMetadataWriter(aic, passProv);
        Runtime.getRuntime().addShutdownHook(emergencyShutdownThread);

        try
        {
            if (inputFile.getPath().equals("-"))
            {
                importFileFromStream(aic, targetFile, System.in);
            }
            else
            {
                FileChannel fc = new RandomAccessFile(inputFile, "r").getChannel();
                try (InputStream fis = Channels.newInputStream(fc))
                {
                    importFileFromStream(aic, targetFile, fis, inputFile.length());
                }
            }
        }
        finally
        {
            // This finally block is a basic attempt at handling bad problems like corrupted writes when saving a file.
            // if an exception was raised due to some IO issue, then we still want to write a hopefully correct
            // metadata section so that the file can be correctly read in future.
            MetadataWriter.write(aic, passProv);
            Runtime.getRuntime().removeShutdownHook(emergencyShutdownThread);
        }
    }

    private void importFileFromStream(ArchiveInfoContext context, FileInventoryItem target, InputStream is) throws IOException
    {
        importFileFromStream(context, target, is, is.available());
    }

    private void importFileFromStream(ArchiveInfoContext context, FileInventoryItem target, InputStream is, long expectedBytes) throws IOException
    {
        ProgressBar pb = new ProgressBar(80, expectedBytes, "Importing file: ");

        try(BufferedInputStream bufferedInput = new BufferedInputStream(is))
        {
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, target))
            {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = bufferedInput.read(buffer)) != -1)
                {
                    bwos.write(buffer, 0, n);
                    pb.inc(n);
                }
                Arrays.fill(buffer, (byte) 0);
            }
        }

        pb.finish();
    }

    private static class EnsuredMetadataWriter extends AbortableShutdownHook
    {
        private final ArchiveInfoContext context;
        private final PasswordProvider prov;

        public EnsuredMetadataWriter(ArchiveInfoContext context, PasswordProvider prov)
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
}
