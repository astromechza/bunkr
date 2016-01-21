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

import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.utils.Formatters;
import org.bunkr.cli.TabularLayout;
import org.bunkr.cli.CLI;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.inventory.*;

import java.util.Collections;
import java.util.List;

/**
 * Created At: 2015-12-06
 */
public class LsCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_NOHEADINGS = "noheadings";
    public static final String ARG_MACHINEREADABLE = "machinereadable";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("list the contents of a folder");
        target.description(
            "List the contents of a path in the archive. Use / to list the contents of the root directory. If the " +
            "path is a file then the attributes of only that file will be printed. File sizes are formatted as IEC " +
            "bytes (powers of 2) unless --machine-readable is used which will show the unformatted number of bytes."
        );
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("directory path to list");
        target.addArgument("-H", "--no-headings")
                .dest(ARG_NOHEADINGS)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("disable headings in the output");
        target.addArgument("-M", "--machine-readable")
                .dest(ARG_MACHINEREADABLE)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("format data in machine readable form");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);
        IFFTraversalTarget t = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_PATH));

        TabularLayout table = new TabularLayout();
        if (! args.getBoolean(ARG_NOHEADINGS)) table.setHeaders("SIZE", "MODIFIED", "NAME");

        if (t.isAFile())
        {
            FileInventoryItem file = (FileInventoryItem) t;
            addFileRow(file, table, args.getBoolean(ARG_MACHINEREADABLE));
        }
        else
        {
            IFFContainer c = (IFFContainer) t;
            List<FolderInventoryItem> folders = c.getFolders();
            Collections.sort(folders);
            for (FolderInventoryItem folder : folders)
            {
                table.addRow("", "", folder.getName() + "/");
            }
            List<FileInventoryItem> files = c.getFiles();
            Collections.sort(files);
            for (FileInventoryItem file : files)
            {
                addFileRow(file, table, args.getBoolean(ARG_MACHINEREADABLE));
            }
        }
        table.printOut();
    }

    private void addFileRow(FileInventoryItem file, TabularLayout table, boolean machinereadable)
    {
        String sizeCell;
        if (machinereadable)
            sizeCell = "" + file.getActualSize();
        else
            sizeCell = Formatters.formatBytes(file.getActualSize());
        String dateCell;
        if (machinereadable)
            dateCell = Formatters.formatIso8601utc(file.getModifiedAt());
        else
            dateCell = Formatters.formatPrettyDate(file.getModifiedAt());

        table.addRow(sizeCell, dateCell, file.getName());
    }
}
