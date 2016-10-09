/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.cli.commands;

import net.sourceforge.argparse4j.impl.Arguments;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.cli.CLI;
import org.bunkr.cli.ProgressBar;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.exceptions.CLIException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.IFFTraversalTarget;
import org.bunkr.core.inventory.InventoryPather;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.digests.*;
import org.bunkr.core.utils.Units;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created At: 2015-12-13
 */
public class HashCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_ALGORITHM = "algorithm";
    public static final String ARG_NO_PROGRESS = "noprogress";

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
        target.addArgument("--no-progress")
                .dest(ARG_NO_PROGRESS)
                .action(Arguments.storeTrue())
                .setDefault(false)
                .type(Boolean.class)
                .help("don't display a progress bar while hashing the file");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);
        IFFTraversalTarget target = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_PATH));
        if (!target.isAFile()) throw new CLIException("'%s' is not a file.", args.getString(ARG_PATH));

        FileInventoryItem targetFile = (FileInventoryItem) target;
        byte[] digest = calculateHash(aic, targetFile, args.getString(ARG_ALGORITHM), !args.getBoolean(ARG_NO_PROGRESS));
        System.out.println(DatatypeConverter.printHexBinary(digest).toLowerCase());
    }

    private byte[] calculateHash(ArchiveInfoContext context, FileInventoryItem target, String algorithm, boolean showProgress)
            throws IOException, CLIException
    {
        ProgressBar pb = new ProgressBar(120, target.getActualSize(), "Calculating hash: ");
        pb.setEnabled(showProgress);
        pb.setUnitIsBytes(true);
        pb.startFresh();
        GeneralDigest digest = getDigest(algorithm);
        digest.reset();
        try (MultilayeredInputStream ms = new MultilayeredInputStream(context, target))
        {
            byte[] buffer = new byte[(int) Units.MEBIBYTE];
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
