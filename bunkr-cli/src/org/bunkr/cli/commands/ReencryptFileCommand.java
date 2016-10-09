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
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.cli.CLI;
import org.bunkr.cli.ProgressBar;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter.ProtectedMetadataWrite;
import org.bunkr.core.exceptions.CLIException;
import org.bunkr.core.inventory.Algorithms;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.IFFTraversalTarget;
import org.bunkr.core.inventory.InventoryPather;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.Units;

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

        FileInventoryItem targetFile = (FileInventoryItem) target;
        ProgressBar pb = new ProgressBar(120, targetFile.getActualSize(), "Exporting file: ");
        pb.setEnabled(!(Boolean) args.get(ARG_NO_PROGRESS));
        pb.setUnitIsBytes(true);
        pb.startFresh();

        Algorithms.Encryption algorithmBefore = targetFile.getEncryptionAlgorithm();

        try (MultilayeredInputStream mis = new MultilayeredInputStream(archive, targetFile))
        {
            try(ProtectedMetadataWrite ignored = new ProtectedMetadataWrite(archive, usp))
            {
                try (MultilayeredOutputStream mos = new MultilayeredOutputStream(archive, targetFile))
                {
                    byte[] buffer = new byte[(int) Units.MEBIBYTE];
                    int n;
                    while ((n = mis.read(buffer)) != -1)
                    {
                        mos.write(buffer, 0, n);
                        pb.inc(n);
                    }
                }
            }
            pb.finish();
            System.out.println(String.format(
                    "Re-encrypted file %s with %s (was %s).",
                    targetFile.getName(), targetFile.getEncryptionAlgorithm(), algorithmBefore
            ));
        }
    }
}
