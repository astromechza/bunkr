package org.bunkr_tests.cli.commands;

import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.UserSecurityProvider;
import org.bunkr.descriptor.PlaintextDescriptor;
import org.bunkr.utils.RandomMaker;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.ImportFileCommand;
import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.exceptions.CLIException;
import org.bunkr.inventory.FileInventoryItem;
import org.bunkr.inventory.FolderInventoryItem;
import org.bunkr.streams.output.MultilayeredOutputStream;
import org.bunkr_tests.XTemporaryFolder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.bunkr_tests.cli.OutputCapture;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;

/**
 * Creator: benmeier
 * Created At: 2015-12-11
 */
public class TestImportFileCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    @Test
    public void testBuildParser()
    {
        new ImportFileCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    @Test
    public void testImportFile() throws Exception
    {
        File archiveFile = folder.newFile();

        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveBuilder.createNewEmptyArchive(archiveFile, new PlaintextDescriptor(), usp, false);

        File fileToImport = folder.newFile();
        try(FileOutputStream fos = new FileOutputStream(fileToImport))
        {
            fos.write(RandomMaker.get(4433 * 8));
        }

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(ImportFileCommand.ARG_PATH, "/a.txt");
        args.put(ImportFileCommand.ARG_SOURCE_FILE, fileToImport);
        args.put(ImportFileCommand.ARG_TAGS, Arrays.asList("tag1", "tag2"));

        try (OutputCapture ignored = new OutputCapture())
        {
            new ImportFileCommand().handle(new Namespace(args));
        }
        ArchiveInfoContext context = new ArchiveInfoContext(archiveFile, usp);

        FileInventoryItem f = context.getInventory().findFile("a.txt");
        assertThat(f.getTags().size(), is(equalTo(2)));
        assertTrue(f.getTags().contains("tag1"));
        assertTrue(f.getTags().contains("tag2"));
        assertThat(f.getActualSize(), is(equalTo(4433L)));
    }

    @Test
    public void testImportStdin() throws Exception
    {
        File archiveFile = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveBuilder.createNewEmptyArchive(archiveFile, new PlaintextDescriptor(), usp, false);

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(ImportFileCommand.ARG_PATH, "/b.txt");
        args.put(ImportFileCommand.ARG_SOURCE_FILE, new File("-"));
        args.put(ImportFileCommand.ARG_TAGS, Arrays.asList("tag3", "tag4"));

        ByteArrayInputStream bais = new ByteArrayInputStream(
                "01234567890123456789012345678901234567890123456789012345678901234567890123456789".getBytes());

        System.setIn(bais);
        try (OutputCapture ignored = new OutputCapture())
        {
            new ImportFileCommand().handle(new Namespace(args));
        }
        System.setIn(null);

        ArchiveInfoContext context = new ArchiveInfoContext(archiveFile, usp);

        FileInventoryItem f = context.getInventory().findFile("b.txt");
        assertThat(f.getTags().size(), is(equalTo(2)));
        assertTrue(f.getTags().contains("tag3"));
        assertTrue(f.getTags().contains("tag4"));
        assertThat(f.getActualSize(), is(equalTo(80L)));
    }

    @Test
    public void testImportAsRootError() throws Exception
    {
        File archiveFile = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveBuilder.createNewEmptyArchive(archiveFile, new PlaintextDescriptor(), usp, false);

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(ImportFileCommand.ARG_PATH, "/");
        args.put(ImportFileCommand.ARG_SOURCE_FILE, new File("-"));
        args.put(ImportFileCommand.ARG_TAGS, Arrays.asList("tag3", "tag4"));

        System.setIn(new ByteArrayInputStream("0123456789012345678901234567890123456789".getBytes()));
        try
        {
            try (OutputCapture ignored = new OutputCapture())
            {
                new ImportFileCommand().handle(new Namespace(args));
                fail("Should fail");
            }
        }
        catch(CLIException ignored) {}
        System.setIn(null);
    }

    @Test
    public void testImportAsFileChild() throws Exception
    {
        File archiveFile = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(
                archiveFile, new PlaintextDescriptor(), usp, false
        );

        {
            FileInventoryItem fileOne = new FileInventoryItem("a.txt");
            context.getInventory().getFiles().add(fileOne);
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileOne))
            {
                bwos.write(RandomMaker.get(10 * 8));
            }
            MetadataWriter.write(context, usp);
        }

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(ImportFileCommand.ARG_PATH, "/a.txt/something");
        args.put(ImportFileCommand.ARG_SOURCE_FILE, new File("-"));
        args.put(ImportFileCommand.ARG_TAGS, Arrays.asList("tag3", "tag4"));

        System.setIn(new ByteArrayInputStream("0123456789012345678901234567890123456789".getBytes()));
        try
        {
            try (OutputCapture ignored = new OutputCapture())
            {
                new ImportFileCommand().handle(new Namespace(args));
                fail("Should fail");
            }
        }
        catch(CLIException ignored) {}
        System.setIn(null);
    }

    @Test
    public void testImportOverFile() throws Exception
    {
        File archiveFile = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(
                archiveFile, new PlaintextDescriptor(), usp, false
        );

        {
            FileInventoryItem fileOne = new FileInventoryItem("a.txt");
            context.getInventory().getFiles().add(fileOne);
            try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileOne))
            {
                bwos.write(RandomMaker.get(10 * 8));
            }
            MetadataWriter.write(context, usp);
        }

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(ImportFileCommand.ARG_PATH, "/a.txt");
        args.put(ImportFileCommand.ARG_SOURCE_FILE, new File("-"));
        args.put(ImportFileCommand.ARG_TAGS, Collections.emptyList());

        System.setIn(new ByteArrayInputStream("0123456789012345678901234567890123456789".getBytes()));
        try (OutputCapture ignored = new OutputCapture())
        {
            new ImportFileCommand().handle(new Namespace(args));
        }
        System.setIn(null);

        {
            context.refresh(usp);
            FileInventoryItem fileOne = context.getInventory().findFile("a.txt");
            assertThat(fileOne.getActualSize(), is(equalTo(40L)));
        }
    }

    @Test
    public void testImportOverFolder() throws Exception
    {
        File archiveFile = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(
                archiveFile, new PlaintextDescriptor(), usp, false
        );

        {
            FolderInventoryItem folderOne = new FolderInventoryItem("folder");
            context.getInventory().getFolders().add(folderOne);
            MetadataWriter.write(context, usp);
        }

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(ImportFileCommand.ARG_PATH, "/folder");
        args.put(ImportFileCommand.ARG_SOURCE_FILE, new File("-"));
        args.put(ImportFileCommand.ARG_TAGS, Collections.emptyList());

        System.setIn(new ByteArrayInputStream("0123456789012345678901234567890123456789".getBytes()));
        try (OutputCapture ignored = new OutputCapture())
        {
            new ImportFileCommand().handle(new Namespace(args));
            fail("Should fail");
        }
        catch(CLIException ignored) {}
        System.setIn(null);
    }

}
