package org.bunkr_tests.cli.commands;

import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.LsCommand;
import org.bunkr.usersec.PasswordProvider;
import org.bunkr.usersec.UserSecurityProvider;
import org.bunkr.descriptor.PlaintextDescriptor;
import org.bunkr.exceptions.TraversalException;
import org.bunkr.inventory.FileInventoryItem;
import org.bunkr.inventory.FolderInventoryItem;
import org.bunkr_tests.XTemporaryFolder;
import org.bunkr_tests.cli.OutputCapture;
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
                .createNewEmptyArchive(archivePath, new PlaintextDescriptor(), usp, false);

        FileInventoryItem untaggedFile = new FileInventoryItem("untagged-file");

        FileInventoryItem taggedFile = new FileInventoryItem("tagged-file");
        taggedFile.addTag("john");
        taggedFile.addTag("bob");

        context.getInventory().getFiles().add(untaggedFile);
        context.getInventory().getFiles().add(taggedFile);

        FolderInventoryItem folder = new FolderInventoryItem("some-folder");
        folder.getFiles().add(new FileInventoryItem("subfile"));
        folder.getFiles().add(new FileInventoryItem("subfile2"));
        context.getInventory().getFolders().add(folder);

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
            assertThat(lines.get(0).trim(), is(equalTo("SIZE  MODIFIED      NAME           TAGS")));
            assertThat(lines.get(1).trim(), is(equalTo("some-folder/")));
            assertTrue(lines.get(2).trim().startsWith("0B"));
            assertTrue(lines.get(2).trim().endsWith("tagged-file    bob john"));
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
            assertTrue(lines.get(1).trim().endsWith("tagged-file    bob john"));
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
            assertThat(lines.get(0).trim(), is(equalTo("SIZE  MODIFIED      NAME         TAGS")));
            assertTrue(lines.get(1).trim().startsWith("0B"));
            assertTrue(lines.get(1).trim().endsWith("tagged-file  bob john"));
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
