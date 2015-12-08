package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.cli.CLIPasswordPrompt;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Creator: benmeier
 * Created At: 2015-12-08
 */
public class ImportFileCommand implements ICLICommand
{
    @Override
    public void buildParser(Subparser target)
    {
        target.addArgument("path")
                .type(String.class)
                .help("destination path in the archive");
        target.addArgument("source")
                .type(Arguments.fileType().acceptSystemIn().verifyCanRead())
                .help("file to import or - for stdin");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        try
        {
            PasswordProvider passProv = makePasswordProvider(args);
            ArchiveInfoContext aic = new ArchiveInfoContext(args.get("archive"), passProv);
            IFFTraversalTarget parent = InventoryPather.traverse(aic.getInventory(), InventoryPather.dirname(args.getString("path")));
            if (parent.isAFile()) throw new CLIException("Cannot create file as a child of a file.");
            IFFContainer container = (IFFContainer) parent;

            IFFTraversalTarget target = container.findFileOrFolder(InventoryPather.baseName(args.getString("path")));
            if (target != null && target.isAFolder()) throw new CLIException("Cannot overwrite folder with a file.");

            FileInventoryItem targetFile = null;
            if (target != null) targetFile = (FileInventoryItem) target;
            if (targetFile == null)
            {
                targetFile = new FileInventoryItem(InventoryPather.baseName(args.getString("path")));
                ((IFFContainer) parent).getFiles().add(targetFile);
            }

            InputStream contentInputStream;
            File inputFile = args.get("source");
            if (inputFile.getPath().equals("-"))
                contentInputStream = System.in;
            else
                contentInputStream = new FileInputStream(inputFile);

            try
            {
                // TODO a progress bar could be fun?
                try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(aic, targetFile))
                {
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = contentInputStream.read(buffer)) != -1)
                    {
                        bwos.write(buffer, 0, n);
                    }
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
