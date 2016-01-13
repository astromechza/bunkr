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
import org.bunkr.core.MetadataWriter;
import org.bunkr.cli.CLI;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.exceptions.CLIException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.IFFTraversalTarget;
import org.bunkr.core.inventory.InventoryPather;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Creator: benmeier
 * Created At: 2015-12-10
 */
public class TagCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_TAGS = "tags";
    public static final String ARG_CLEAR = "clear";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("list, set or add tags to a file");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("path of the file to tag");
        target.addArgument("-t", "--tags")
                .dest(ARG_TAGS)
                .type(String.class)
                .nargs("*")
                .setDefault(new ArrayList<>())
                .help("a list of tags to associate with this file");
        target.addArgument("-c", "--clear")
                .dest(ARG_CLEAR)
                .type(Boolean.class)
                .setDefault(false)
                .action(Arguments.storeTrue())
                .help("remove all tags associated with the file");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        // first do some arg checking
        if (args.getBoolean(ARG_CLEAR) && args.getList(ARG_TAGS).size() > 0)
        {
            throw new CLIException("Please use either --tags or --clear, not both.");
        }

        // load up initial archive
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);

        IFFTraversalTarget target = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_PATH));
        if (target.isAFolder()) throw new CLIException("Cannot tag a folder.");

        FileInventoryItem targetFile = (FileInventoryItem) target;

        if (args.getBoolean(ARG_CLEAR))
        {
            targetFile.setTags(new HashSet<>());
            MetadataWriter.write(aic, usp);
            System.out.println(String.format("Cleared tags on file %s", args.getString(ARG_PATH)));
        }
        else if (args.getList(ARG_TAGS).size() > 0)
        {
            try
            {
                targetFile.setCheckTags(new HashSet<>(args.getList(ARG_TAGS)));
                MetadataWriter.write(aic, usp);
                System.out.println(String.format("Set %d tags on file %s", args.getList(ARG_TAGS).size(),
                                                 args.getString(ARG_PATH)));
            }
            catch (IllegalArgumentException e)
            {
                throw new CLIException(e.getMessage());
            }
        }
        else
        {
            for (String s : targetFile.getTags())
            {
                System.out.println(s);
            }
        }
    }
}
