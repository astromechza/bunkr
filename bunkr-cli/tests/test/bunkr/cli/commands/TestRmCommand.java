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

package test.bunkr.cli.commands;

import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.RmCommand;
import org.bunkr.core.inventory.*;
import org.bunkr.core.inventory.Algorithms.Encryption;
import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.exceptions.TraversalException;
import test.bunkr.core.XTemporaryFolder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;

/**
 * Created At: 2015-12-06
 */
public class TestRmCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    public Inventory makeSampleInventory()
    {
        Inventory i = new Inventory(
                new ArrayList<>(),
                new ArrayList<>(),
                Encryption.NONE
        );
        FolderInventoryItem d1 = new FolderInventoryItem("t1");
        i.addFolder(d1);

        FolderInventoryItem d2 = new FolderInventoryItem("t2");
        i.addFolder(d2);
        d2.addFile(new FileInventoryItem("file"));

        FolderInventoryItem d3 = new FolderInventoryItem("t3");
        i.addFolder(d3);
        d3.addFolder(new FolderInventoryItem("file"));

        FileInventoryItem t4 = new FileInventoryItem("t4");
        i.addFile(t4);

        return i;
    }

    public ArchiveInfoContext buildSampleArchive() throws Exception
    {
        File archivePath = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveInfoContext context = ArchiveBuilder
                .createNewEmptyArchive(archivePath, new PlaintextDescriptor(), usp);

        FolderInventoryItem d1 = new FolderInventoryItem("t1");
        context.getInventory().addFolder(d1);

        FolderInventoryItem d2 = new FolderInventoryItem("t2");
        context.getInventory().addFolder(d2);
        d2.addFile(new FileInventoryItem("file"));

        FolderInventoryItem d3 = new FolderInventoryItem("t3");
        context.getInventory().addFolder(d3);
        d3.addFolder(new FolderInventoryItem("file"));

        FileInventoryItem t4 = new FileInventoryItem("t4");
        context.getInventory().addFile(t4);

        MetadataWriter.write(context, usp);

        return context;
    }

    @Test
    public void testBuildParser()
    {
        new RmCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    @Test
    public void testRmNonExistant() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(RmCommand.ARG_PATH, "/t9");
        args.put(RmCommand.ARG_RECURSIVE, false);

        try
        {
            new RmCommand().handle(new Namespace(args));
            fail("should not be able to remove a non-existant file");
        }
        catch(TraversalException ignored) {}
    }

    @Test
    public void testRmEmpty() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(RmCommand.ARG_PATH, "/t1");
        args.put(RmCommand.ARG_RECURSIVE, false);
        args.put(RmCommand.ARG_NOPROGRESS, false);
        args.put(RmCommand.ARG_NOWIPE, false);
        new RmCommand().handle(new Namespace(args));

        context.refresh(new UserSecurityProvider(new PasswordProvider()));
        try
        {
            InventoryPather.traverse(context.getInventory(), "/t1");
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}
    }

    @Test
    public void testRmNonEmptyWithFolder() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        InventoryPather.traverse(inv, "/t2").isAFolder();
        try
        {
            new RmCommand().deleteItem(inv, "/t2", false);
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}
        InventoryPather.traverse(inv, "/t2").isAFolder();
        new RmCommand().deleteItem(inv, "/t2", true);
        try
        {
            InventoryPather.traverse(inv, "/t2");
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}
    }

    @Test
    public void testRmNonEmptyWithFile() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        InventoryPather.traverse(inv, "/t3").isAFolder();
        try
        {
            new RmCommand().deleteItem(inv, "/t3", false);
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}
        InventoryPather.traverse(inv, "/t3").isAFolder();
        new RmCommand().deleteItem(inv, "/t3", true);
        try
        {
            InventoryPather.traverse(inv, "/t3");
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}
    }

    @Test
    public void testRmAFile() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        InventoryPather.traverse(inv, "/t4").isAFile();
        try
        {
            new RmCommand().deleteItem(inv, "/t4", false);
        }
        catch(TraversalException ignored) {}
    }

    @Test
    public void testRmInAFile() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        try
        {
            new RmCommand().deleteItem(inv, "/t4/t4", false);
        }
        catch(TraversalException ignored) {}
    }

    @Test
    public void testRmRoot()
    {
        Inventory inv = makeSampleInventory();
        try
        {
            new RmCommand().deleteItem(inv, "/", true);
        }
        catch(TraversalException ignored) {}
    }
}
