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

import org.bunkr.cli.ProgressBar;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.cli.CLI;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.exceptions.CLIException;
import org.bunkr.core.exceptions.IntegrityHashError;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.IFFTraversalTarget;
import org.bunkr.core.inventory.InventoryPather;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.core.utils.Units;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Created At: 2015-12-08
 */
public class ExportFileCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_DESTINATION_FILE = "destination";
    public static final String ARG_IGNORE_INTEGRITY_CHECK = "ignoreintegrity";
    public static final String ARG_NO_PROGRESS = "noprogress";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("read or export a file from the archive");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("source path in the archive");
        target.addArgument("destination")
                .dest(ARG_DESTINATION_FILE)
                .type(Arguments.fileType().acceptSystemIn())
                .help("file to export to or - for stdout");
        target.addArgument("--ignore-integrity-error")
                .dest(ARG_IGNORE_INTEGRITY_CHECK)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("ignore integrity check error caused by data corruption");
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
        try
        {
            UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));

            ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);
            IFFTraversalTarget target = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_PATH));
            if (!target.isAFile()) throw new CLIException("'%s' is not a file.", args.getString(ARG_PATH));

            FileInventoryItem targetFile = (FileInventoryItem) target;

            File inputFile = args.get(ARG_DESTINATION_FILE);
            boolean checkHash = (!args.getBoolean(ARG_IGNORE_INTEGRITY_CHECK));
            if (inputFile.getPath().equals("-"))
            {
                writeBlockFileToStream(aic, targetFile, System.out, checkHash, false);
            }
            else
            {
                if (inputFile.exists()) throw new CLIException("'%s' already exists. Will not overwrite.", inputFile.getCanonicalPath());
                FileChannel fc = new RandomAccessFile(inputFile, "rw").getChannel();
                try (OutputStream contentOutputStream = Channels.newOutputStream(fc))
                {
                    writeBlockFileToStream(aic, targetFile, contentOutputStream, checkHash, !args.getBoolean(ARG_NO_PROGRESS));
                }
            }
        }
        catch (IntegrityHashError e)
        {
            throw new CLIException(e);
        }
    }

    private void writeBlockFileToStream(ArchiveInfoContext ctxt, FileInventoryItem targetFile, OutputStream os, boolean checkHash, boolean showProgress)
            throws IOException
    {
        ProgressBar pb = new ProgressBar(120, targetFile.getActualSize(), "Exporting file: ");
        pb.setEnabled(showProgress);
        pb.startFresh();

        try (MultilayeredInputStream ms = new MultilayeredInputStream(ctxt, targetFile))
        {
            ms.setCheckHashOnFinish(checkHash);
            byte[] buffer = new byte[(int) Units.MEBIBYTE];
            int n;
            while ((n = ms.read(buffer)) != -1)
            {
                os.write(buffer, 0, n);
                pb.inc(n);
            }
            Arrays.fill(buffer, (byte) 0);
        }

        pb.finish();
    }
}
