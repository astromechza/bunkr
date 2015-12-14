package org.bunkr_tests.cli.commands;

import org.bunkr.ArchiveBuilder;
import org.bunkr.ArchiveInfoContext;
import org.bunkr.MetadataWriter;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.FindCommand;
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
import org.bouncycastle.crypto.CryptoException;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
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

    public ArchiveInfoContext buildSampleArchive() throws IOException, CryptoException
    {
        File archivePath = folder.newFile();
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(
                archivePath,
                new Descriptor(null, null),
                new PasswordProvider()
        );

        FileInventoryItem ff1 = new FileInventoryItem("abc");
        ff1.addTag("tag1"); ff1.addTag("tag2");
        context.getInventory().getFiles().add(ff1);
        context.getInventory().getFiles().add(new FileInventoryItem("aabbcc"));
        context.getInventory().getFiles().add(new FileInventoryItem("aaabbbccc"));

        FolderInventoryItem folderOne = new FolderInventoryItem("afolder");

        FileInventoryItem ff2 = new FileInventoryItem("abc");
        ff2.addTag("tag1"); ff2.addTag("tag3");
        folderOne.getFiles().add(ff2);
        folderOne.getFiles().add(new FileInventoryItem("aabbcc"));
        folderOne.getFiles().add(new FileInventoryItem("aaabbbccc"));

        FolderInventoryItem folderTwo = new FolderInventoryItem("folderc");
        folderTwo.getFiles().add(new FileInventoryItem("example"));
        folderOne.getFolders().add(folderTwo);
        context.getInventory().getFolders().add(folderOne);

        MetadataWriter.write(context, new PasswordProvider());

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
    public void testFindAllTags() throws Exception
    {
        ArchiveInfoContext c = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, c.filePath);
        args.put(FindCommand.ARG_PATH, "/");
        args.put(FindCommand.ARG_TAG, "tag1");
        try(OutputCapture oc = new OutputCapture())
        {
            new FindCommand().handle(new Namespace(args));
            List<String> lines = oc.getLines();
            assertThat(lines.get(0), is(equalTo("/abc")));
            assertThat(lines.get(1), is(equalTo("/afolder/abc")));
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
