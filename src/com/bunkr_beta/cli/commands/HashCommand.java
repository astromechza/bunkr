package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.cli.ProgressBar;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.IFFTraversalTarget;
import com.bunkr_beta.inventory.InventoryPather;
import com.bunkr_beta.streams.input.MultilayeredInputStream;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.digests.*;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-13
 */
public class HashCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_ALGORITHM = "algorithm";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("calculate a integrity hash for a file in the archive");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("archive path to create the new directory");
        target.addArgument("-a", "--algorithm")
                .dest(ARG_ALGORITHM)
                .type(String.class)
                .choices("md5", "sha1", "sha224", "sha256")
                .help("the digest to use");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        PasswordProvider passProv = makePasswordProvider(args);
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), passProv);
        IFFTraversalTarget target = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_PATH));
        if (!target.isAFile()) throw new CLIException("'%s' is not a file.", args.getString(ARG_PATH));

        FileInventoryItem targetFile = (FileInventoryItem) target;

        System.out.println(DatatypeConverter.printHexBinary(calculateHash(aic, targetFile, args.getString(ARG_ALGORITHM))).toLowerCase());
    }

    private byte[] calculateHash(ArchiveInfoContext context, FileInventoryItem target, String algorithm)
            throws IOException, CLIException
    {
        ProgressBar pb = new ProgressBar(80, target.getActualSize(), "Calculating hash: ");

        GeneralDigest digest = getDigest(algorithm);
        digest.reset();
        try (MultilayeredInputStream ms = new MultilayeredInputStream(context, target))
        {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = ms.read(buffer)) != -1)
            {
                digest.update(buffer, 0, n);
                pb.inc(n);
            }
        }
        pb.finish();

        int length = digest.getDigestSize();
        byte[] buffer = new byte[length];
        digest.doFinal(buffer, 0);

        return buffer;
    }

    private GeneralDigest getDigest(String algorithm) throws CLIException
    {
        if (algorithm.equals("md5")) return new MD5Digest();
        if (algorithm.equals("sha1")) return new SHA1Digest();
        if (algorithm.equals("sha224")) return new SHA224Digest();
        if (algorithm.equals("sha256")) return new SHA256Digest();
        throw new CLIException("unsupported algorithm: " + algorithm);
    }
}
