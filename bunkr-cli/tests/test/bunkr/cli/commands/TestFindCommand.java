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
import org.bunkr.cli.commands.FindCommand;
import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.exceptions.CLIException;
import org.bunkr.core.exceptions.TraversalException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.FolderInventoryItem;
import test.bunkr.core.XTemporaryFolder;
import test.bunkr.cli.OutputCapture;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Creator: benmeier
 * Created At: 2015-12-12
 */
public class TestFindCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    public ArchiveInfoContext buildSampleArchive() throws Exception
    {
        File archivePath = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(
                archivePath,
                new PlaintextDescriptor(),
                usp
        );

        FileInventoryItem ff1 = new FileInventoryItem("abc");
        context.getInventory().addFile(ff1);
        context.getInventory().addFile(new FileInventoryItem("aabbcc"));
        context.getInventory().addFile(new FileInventoryItem("aaabbbccc"));

        FolderInventoryItem folderOne = new FolderInventoryItem("afolder");

        FileInventoryItem ff2 = new FileInventoryItem("abc");
        folderOne.addFile(ff2);
        folderOne.addFile(new FileInventoryItem("aabbcc"));
        folderOne.addFile(new FileInventoryItem("aaabbbccc"));

        FolderInventoryItem folderTwo = new FolderInventoryItem("folderc");
        folderTwo.addFile(new FileInventoryItem("example"));
        folderOne.addFolder(folderTwo);
        context.getInventory().addFolder(folderOne);

        MetadataWriter.write(context, usp);

        return context;
    }

    @Test
    public void testBuildParser()
    {
        new FindCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    @Test
    public void testFindAll() throws Exception
    {
        ArchiveInfoContext c = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, c.filePath);
        args.put(FindCommand.ARG_PATH, "/");
        try(OutputCapture oc = new OutputCapture())
        {
            new FindCommand().handle(new Namespace(args));
            List<String> lines = oc.getLines();
            assertThat(lines.get(0), is(equalTo("/aaabbbccc")));
            assertThat(lines.get(1), is(equalTo("/aabbcc")));
            assertThat(lines.get(2), is(equalTo("/abc")));
            assertThat(lines.get(3), is(equalTo("/afolder/")));
            assertThat(lines.get(4), is(equalTo("/afolder/aaabbbccc")));
            assertThat(lines.get(5), is(equalTo("/afolder/aabbcc")));
            assertThat(lines.get(6), is(equalTo("/afolder/abc")));
            assertThat(lines.get(7), is(equalTo("/afolder/folderc/")));
            assertThat(lines.get(8), is(equalTo("/afolder/folderc/example")));
        }
    }

    @Test
    public void testFindAllSubFolder() throws Exception
    {
        ArchiveInfoContext c = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, c.filePath);
        args.put(FindCommand.ARG_PATH, "/afolder");
        try(OutputCapture oc = new OutputCapture())
        {
            new FindCommand().handle(new Namespace(args));
            List<String> lines = oc.getLines();
            assertThat(lines.get(0), is(equalTo("/afolder/aaabbbccc")));
            assertThat(lines.get(1), is(equalTo("/afolder/aabbcc")));
            assertThat(lines.get(2), is(equalTo("/afolder/abc")));
            assertThat(lines.get(3), is(equalTo("/afolder/folderc/")));
            assertThat(lines.get(4), is(equalTo("/afolder/folderc/example")));
        }
    }

    @Test
    public void testFindAllPrefix() throws Exception
    {
        ArchiveInfoContext c = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, c.filePath);
        args.put(FindCommand.ARG_PATH, "/");
        args.put(FindCommand.ARG_PREFIX, "aa");
        try(OutputCapture oc = new OutputCapture())
        {
            new FindCommand().handle(new Namespace(args));
            List<String> lines = oc.getLines();
            assertThat(lines.get(0), is(equalTo("/aaabbbccc")));
            assertThat(lines.get(1), is(equalTo("/aabbcc")));
            assertThat(lines.get(2), is(equalTo("/afolder/aaabbbccc")));
            assertThat(lines.get(3), is(equalTo("/afolder/aabbcc")));
        }
    }

    @Test
    public void testFindAllSuffix() throws Exception
    {
        ArchiveInfoContext c = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, c.filePath);
        args.put(FindCommand.ARG_PATH, "/");
        args.put(FindCommand.ARG_SUFFIX, "c");
        try(OutputCapture oc = new OutputCapture())
        {
            new FindCommand().handle(new Namespace(args));
            List<String> lines = oc.getLines();
            assertThat(lines.get(0), is(equalTo("/aaabbbccc")));
            assertThat(lines.get(1), is(equalTo("/aabbcc")));
            assertThat(lines.get(2), is(equalTo("/abc")));
            assertThat(lines.get(3), is(equalTo("/afolder/aaabbbccc")));
            assertThat(lines.get(4), is(equalTo("/afolder/aabbcc")));
            assertThat(lines.get(5), is(equalTo("/afolder/abc")));
        }
    }


    @Test
    public void testFindAllFiles() throws Exception
    {
        ArchiveInfoContext c = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, c.filePath);
        args.put(FindCommand.ARG_PATH, "/");
        args.put(FindCommand.ARG_TYPE, "file");
        try(OutputCapture oc = new OutputCapture())
        {
            new FindCommand().handle(new Namespace(args));
            List<String> lines = oc.getLines();
            assertThat(lines.get(0), is(equalTo("/aaabbbccc")));
            assertThat(lines.get(1), is(equalTo("/aabbcc")));
            assertThat(lines.get(2), is(equalTo("/abc")));
            assertThat(lines.get(3), is(equalTo("/afolder/aaabbbccc")));
            assertThat(lines.get(4), is(equalTo("/afolder/aabbcc")));
            assertThat(lines.get(5), is(equalTo("/afolder/abc")));
            assertThat(lines.get(6), is(equalTo("/afolder/folderc/example")));
        }
    }

    @Test
    public void testFindAllFolders() throws Exception
    {
        ArchiveInfoContext c = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, c.filePath);
        args.put(FindCommand.ARG_PATH, "/");
        args.put(FindCommand.ARG_TYPE, "folder");
        try(OutputCapture oc = new OutputCapture())
        {
            new FindCommand().handle(new Namespace(args));
            List<String> lines = oc.getLines();
            assertThat(lines.get(0), is(equalTo("/afolder/")));
            assertThat(lines.get(1), is(equalTo("/afolder/folderc/")));
        }
    }

    @Test
    public void testFindAllDepth() throws Exception
    {
        ArchiveInfoContext c = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, c.filePath);
        args.put(FindCommand.ARG_PATH, "/");
        args.put(FindCommand.ARG_DEPTH, 0);
        try(OutputCapture oc = new OutputCapture())
        {
            new FindCommand().handle(new Namespace(args));
            List<String> lines = oc.getLines();
            assertThat(lines.get(0), is(equalTo("/aaabbbccc")));
            assertThat(lines.get(1), is(equalTo("/aabbcc")));
            assertThat(lines.get(2), is(equalTo("/abc")));
            assertThat(lines.get(3), is(equalTo("/afolder/")));
        }
    }

    @Test
    public void testFindAllOnFile() throws Exception
    {
        ArchiveInfoContext c = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, c.filePath);
        args.put(FindCommand.ARG_PATH, "/abc");
        try
        {
            new FindCommand().handle(new Namespace(args));
            fail("should have failed");
        }
        catch (CLIException ignored) {}
    }

    @Test
    public void testFindAllOnMissing() throws Exception
    {
        ArchiveInfoContext c = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, c.filePath);
        args.put(FindCommand.ARG_PATH, "/unknown-path-item");
        try
        {
            new FindCommand().handle(new Namespace(args));
            fail("should have failed");
        }
        catch (TraversalException ignored) {}
    }
}
