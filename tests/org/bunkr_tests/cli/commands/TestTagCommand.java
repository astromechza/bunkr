package org.bunkr_tests.cli.commands;

import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.TagCommand;
import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.descriptor.Descriptor;
import org.bunkr.exceptions.CLIException;
import org.bunkr.exceptions.TraversalException;
import org.bunkr.inventory.FileInventoryItem;
import org.bunkr.inventory.FolderInventoryItem;
import org.bunkr_tests.XTemporaryFolder;
import org.bunkr_tests.cli.OutputCapture;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Rule;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Creator: benmeier
 * Created At: 2015-12-12
 */
public class TestTagCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    public ArchiveInfoContext buildSampleArchive() throws Exception
    {
        File archivePath = folder.newFile();
        ArchiveInfoContext context = ArchiveBuilder
                .createNewEmptyArchive(archivePath, new Descriptor(null), new PasswordProvider(), false, false);

        FileInventoryItem untaggedFile = new FileInventoryItem("untagged-file");

        FileInventoryItem taggedFile = new FileInventoryItem("tagged-file");
        taggedFile.addTag("john");
        taggedFile.addTag("bob");

        context.getInventory().getFiles().add(untaggedFile);
        context.getInventory().getFiles().add(taggedFile);

        context.getInventory().getFolders().add(new FolderInventoryItem("some-folder"));

        MetadataWriter.write(context, new PasswordProvider());

        return context;
    }

    @Test
    public void testBuildParser()
    {
        new TagCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    @Test
    public void testAddTags() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(TagCommand.ARG_PATH, "/untagged-file");
        args.put(TagCommand.ARG_CLEAR, false);
        args.put(TagCommand.ARG_TAGS, Arrays.asList("newtag-one", "newtag-two"));

        new TagCommand().handle(new Namespace(args));

        context.refresh(new PasswordProvider());

        FileInventoryItem fi = context.getInventory().findFile("untagged-file");
        assertTrue(fi.hasTag("newtag-one"));
        assertTrue(fi.hasTag("newtag-two"));
    }

    @Test
    public void testAddTagsBadTags() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(TagCommand.ARG_PATH, "/untagged-file");
        args.put(TagCommand.ARG_CLEAR, false);
        args.put(TagCommand.ARG_TAGS, Arrays.asList("!(@*#&!@(", " 12983"));

        try
        {
            new TagCommand().handle(new Namespace(args));
            fail("should fail");
        }
        catch(CLIException ignored) {}
    }

    @Test
    public void testClearTags() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(TagCommand.ARG_PATH, "/tagged-file");
        args.put(TagCommand.ARG_CLEAR, true);
        args.put(TagCommand.ARG_TAGS, Collections.emptyList());

        new TagCommand().handle(new Namespace(args));

        context.refresh(new PasswordProvider());

        FileInventoryItem fi = context.getInventory().findFile("tagged-file");
        assertThat(fi.getTags().size(), is(equalTo(0)));
    }

    @Test
    public void testPrintTags() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(TagCommand.ARG_PATH, "/tagged-file");
        args.put(TagCommand.ARG_CLEAR, false);
        args.put(TagCommand.ARG_TAGS, Collections.emptyList());

        try(OutputCapture c = new OutputCapture())
        {
            new TagCommand().handle(new Namespace(args));
            String output = c.getContent();
            output = output.replace("\r", "");
            List<String> tagList = Arrays.asList(output.split("\n"));
            assertThat(tagList.size(), is(equalTo(2)));
            assertTrue(tagList.contains("bob"));
            assertTrue(tagList.contains("john"));
        }
    }

    @Test
    public void testTagAFolder() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(TagCommand.ARG_PATH, "/some-folder");
        args.put(TagCommand.ARG_CLEAR, false);
        args.put(TagCommand.ARG_TAGS, Arrays.asList("newtag-one", "newtag-two"));

        try
        {
            new TagCommand().handle(new Namespace(args));
            fail("should fail");
        }
        catch(CLIException ignored) {}
    }

    @Test
    public void testTagMissingFile() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(TagCommand.ARG_PATH, "/missing-file");
        args.put(TagCommand.ARG_CLEAR, false);
        args.put(TagCommand.ARG_TAGS, Arrays.asList("newtag-one", "newtag-two"));

        try
        {
            new TagCommand().handle(new Namespace(args));
            fail("should fail");
        }
        catch(TraversalException ignored) {}
    }


    @Test
    public void testBadArgs() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(TagCommand.ARG_PATH, "/tagged-file");
        args.put(TagCommand.ARG_CLEAR, true);
        args.put(TagCommand.ARG_TAGS, Arrays.asList("newtag-one", "newtag-two"));

        try
        {
            new TagCommand().handle(new Namespace(args));
            fail("should fail");
        }
        catch(CLIException ignored) {}
    }
}
