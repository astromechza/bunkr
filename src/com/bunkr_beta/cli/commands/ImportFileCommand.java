package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.cli.ProgressBar;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.IFFContainer;
import com.bunkr_beta.inventory.IFFTraversalTarget;
import com.bunkr_beta.inventory.InventoryPather;
import com.bunkr_beta.streams.output.MultilayeredOutputStream;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.*;
import java.util.ArrayList;
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
        PasswordProvider passProv = makePasswordProvider(args);
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
        if (inputFile.getPath().equals("-"))
        {
            readFileFromStream(aic, targetFile, System.in);
        }
        else
        {
            try (FileInputStream fis = new FileInputStream(inputFile))
            {
                readFileFromStream(aic, targetFile, fis);
            }
        }

        // TODO think about handling bad issues here.. how do we handle corrupted writes.. maybe it should just
        // write the metadata regardless of what happens so that at least it is recovered and files can be read
        // next time.
        MetadataWriter.write(aic, passProv);
    }

    private void readFileFromStream(ArchiveInfoContext context, FileInventoryItem target, InputStream is) throws IOException
    {
        ProgressBar pb = new ProgressBar(80, is.available(), "Importing file: ");

        try(BufferedInputStream bufferedInput = new BufferedInputStream(is))
        {
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, target))
            {
                byte[] buffer = new byte[4096];
                int n;
                while ((n = bufferedInput.read(buffer)) != -1)
                {
                    bwos.write(buffer, 0, n);
                    pb.inc(n);
                }
                pb.finish();
            }
        }
    }
}
