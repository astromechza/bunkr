package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.cli.ProgressBar;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.exceptions.IllegalPathException;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.IFFContainer;
import com.bunkr_beta.inventory.IFFTraversalTarget;
import com.bunkr_beta.inventory.InventoryPather;
import com.bunkr_beta.streams.output.MultilayeredOutputStream;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

import java.io.*;

/**
 * Creator: benmeier
 * Created At: 2015-12-08
 */
public class ImportFileCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_SOURCE_FILE = "source";
    public static final int PROGRESS_BAR_WIDTH = 78;


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
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        try
        {
            PasswordProvider passProv = makePasswordProvider(args);
            ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), passProv);
            IFFTraversalTarget parent = InventoryPather.traverse(aic.getInventory(), InventoryPather.dirname(args.getString(ARG_PATH)));
            if (parent.isAFile()) throw new CLIException("Cannot create file as a child of a file.");
            IFFContainer container = (IFFContainer) parent;

            IFFTraversalTarget target = container.findFileOrFolder(InventoryPather.baseName(args.getString(ARG_PATH)));
            if (target != null && target.isAFolder()) throw new CLIException("Cannot overwrite folder with a file.");

            FileInventoryItem targetFile = null;
            if (target != null) targetFile = (FileInventoryItem) target;
            if (targetFile == null)
            {
                targetFile = new FileInventoryItem(InventoryPather.baseName(args.getString(ARG_PATH)));
                ((IFFContainer) parent).getFiles().add(targetFile);
            }

            InputStream contentInputStream;
            File inputFile = args.get(ARG_SOURCE_FILE);
            if (inputFile.getPath().equals("-"))
                contentInputStream = System.in;
            else
                contentInputStream = new FileInputStream(inputFile);

            ProgressBar pb = new ProgressBar(80, contentInputStream.available());

            try(BufferedInputStream bufferedInput = new BufferedInputStream(contentInputStream))
            {
                try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(aic, targetFile))
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
            finally
            {
                contentInputStream.close();
            }
            // TODO think about handling bad issues here.. how do we handle corrupted writes.. maybe it should just
            // write the metadata regardless of what happens so that at least it is recovered and files can be read
            // next time.
            MetadataWriter.write(aic, passProv);
        }
        catch (IllegalPathException | TraversalException e)
        {
            throw new CLIException(e);
        }
        catch (CryptoException e)
        {
            throw new CLIException("Decryption failed: %s", e.getMessage());
        }


    }
}
