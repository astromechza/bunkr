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
import org.bunkr.cli.commands.LsCommand;
import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.descriptor.PlaintextDescriptor;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;

/**
 * Creator: benmeier
 * Created At: 2015-12-12
 */
public class TestLsCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    public ArchiveInfoContext buildSampleArchive() throws Exception
    {
        File archivePath = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveInfoContext context = ArchiveBuilder
                .createNewEmptyArchive(archivePath, new PlaintextDescriptor(), usp);

        FileInventoryItem untaggedFile = new FileInventoryItem("untagged-file");

        FileInventoryItem taggedFile = new FileInventoryItem("tagged-file");

        context.getInventory().addFile(untaggedFile);
        context.getInventory().addFile(taggedFile);

        FolderInventoryItem folder = new FolderInventoryItem("some-folder");
        folder.addFile(new FileInventoryItem("subfile"));
        folder.addFile(new FileInventoryItem("subfile2"));
        context.getInventory().addFolder(folder);

        MetadataWriter.write(context, usp);

        return context;
    }

    @Test
    public void testBuildParser()
    {
        new LsCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    @Test
    public void testLsRoot() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(LsCommand.ARG_PATH, "/");
        args.put(LsCommand.ARG_NOHEADINGS, false);
        args.put(LsCommand.ARG_MACHINEREADABLE, false);
        try(OutputCapture oc = new OutputCapture())
        {
            new LsCommand().handle(new Namespace(args));

            String output = oc.getContent();
            output = output.replace("\r", "");
            List<String> lines = Arrays.asList(output.split("\n"));
            assertThat(lines.get(0).trim(), is(equalTo("SIZE  MODIFIED      NAME")));
            assertThat(lines.get(1).trim(), is(equalTo("some-folder/")));
            assertTrue(lines.get(2).trim().startsWith("0B"));
            assertTrue(lines.get(2).trim().endsWith("tagged-file"));
            assertTrue(lines.get(3).trim().startsWith("0B"));
            assertTrue(lines.get(3).trim().endsWith("untagged-file"));
        }
    }

    @Test
    public void testLsRootTweaks() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(LsCommand.ARG_PATH, "/");
        args.put(LsCommand.ARG_NOHEADINGS, true);
        args.put(LsCommand.ARG_MACHINEREADABLE, true);
        try(OutputCapture oc = new OutputCapture())
        {
            new LsCommand().handle(new Namespace(args));

            String output = oc.getContent();
            output = output.replace("\r", "");
            List<String> lines = Arrays.asList(output.split("\n"));
            assertThat(lines.get(0).trim(), is(equalTo("some-folder/")));
            assertTrue(lines.get(1).trim().startsWith("0"));
            assertTrue(lines.get(1).trim().endsWith("tagged-file"));
            assertTrue(lines.get(2).trim().startsWith("0"));
            assertTrue(lines.get(2).trim().endsWith("untagged-file"));
        }
    }

    @Test
    public void testLsFile() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(LsCommand.ARG_PATH, "/tagged-file");
        args.put(LsCommand.ARG_NOHEADINGS, false);
        args.put(LsCommand.ARG_MACHINEREADABLE, false);
        try(OutputCapture oc = new OutputCapture())
        {
            new LsCommand().handle(new Namespace(args));

            String output = oc.getContent();
            output = output.replace("\r", "");
            List<String> lines = Arrays.asList(output.split("\n"));
            assertThat(lines.get(0).trim(), is(equalTo("SIZE  MODIFIED      NAME")));
            assertTrue(lines.get(1).trim().startsWith("0B"));
            assertTrue(lines.get(1).trim().endsWith("tagged-file"));
        }
    }


    @Test
    public void testLsMissingFile() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(LsCommand.ARG_PATH, "/awdawd-awdawd-awdawd");
        args.put(LsCommand.ARG_NOHEADINGS, false);
        args.put(LsCommand.ARG_MACHINEREADABLE, false);
        try
        {
            new LsCommand().handle(new Namespace(args));
            fail("should not succeed");
        }
        catch (TraversalException ignored) {}
    }

}
