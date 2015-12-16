package org.bunkr.cli.commands;

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.cli.CLI;
import org.bunkr.cli.ProgressBar;
import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.exceptions.CLIException;
import org.bunkr.inventory.FileInventoryItem;
import org.bunkr.inventory.IFFTraversalTarget;
import org.bunkr.inventory.InventoryPather;
import org.bunkr.streams.input.MultilayeredInputStream;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.digests.*;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;

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
        target.help("calculate a hash over the contents of a file in the archive");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("path of the file in the archive");
        target.addArgument("-a", "--algorithm")
                .dest(ARG_ALGORITHM)
                .type(String.class)
                .choices("md5", "sha1", "sha224", "sha256")
                .setDefault("md5")
                .help("the digest algorithm to use");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        PasswordProvider passProv = makePasswordProvider(args.get(CLI.ARG_PASSWORD_FILE));
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
            byte[] buffer = new byte[8196];
            int n;
            while ((n = ms.read(buffer)) != -1)
            {
                digest.update(buffer, 0, n);
                pb.inc(n);
            }
            Arrays.fill(buffer, (byte) 0);
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
