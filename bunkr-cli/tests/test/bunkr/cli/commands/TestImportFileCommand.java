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
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.utils.RandomMaker;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.ImportFileCommand;
import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.exceptions.CLIException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.FolderInventoryItem;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import test.bunkr.core.XTemporaryFolder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import test.bunkr.cli.OutputCapture;
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
import static org.junit.Assert.assertFalse;
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
        ArchiveBuilder.createNewEmptyArchive(archiveFile, new PlaintextDescriptor(), usp);

        File fileToImport = folder.newFile();
        try(FileOutputStream fos = new FileOutputStream(fileToImport))
        {
            fos.write(RandomMaker.get(4433 * 8));
        }

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(ImportFileCommand.ARG_PATH, "/a.txt");
        args.put(ImportFileCommand.ARG_SOURCE_FILE, fileToImport);

        try (OutputCapture ignored = new OutputCapture())
        {
            new ImportFileCommand().handle(new Namespace(args));
        }
        ArchiveInfoContext context = new ArchiveInfoContext(archiveFile, usp);

        FileInventoryItem f = context.getInventory().findFile("a.txt");
        assertThat(f.getActualSize(), is(equalTo(4433L)));
        assertThat(f.getAbsolutePath(), is(equalTo("/a.txt")));
    }

    @Test
    public void testImportStdin() throws Exception
    {
        File archiveFile = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(archiveFile, new PlaintextDescriptor(), usp);
        FolderInventoryItem subF = new FolderInventoryItem("sub");
        context.getInventory().addFolder(subF);
        MetadataWriter.write(context, usp);

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(ImportFileCommand.ARG_PATH, "/sub/b.txt");
        args.put(ImportFileCommand.ARG_SOURCE_FILE, new File("-"));

        ByteArrayInputStream bais = new ByteArrayInputStream(
                "01234567890123456789012345678901234567890123456789012345678901234567890123456789".getBytes());

        System.setIn(bais);
        try (OutputCapture ignored = new OutputCapture())
        {
            new ImportFileCommand().handle(new Namespace(args));
            assertTrue(ignored.getContent().contains("==================="));
        }
        System.setIn(null);

        context = new ArchiveInfoContext(archiveFile, usp);
        FolderInventoryItem s = (FolderInventoryItem) context.getInventory().findFolder("sub");
        assertThat(s.getAbsolutePath(), is(equalTo("/sub")));
        FileInventoryItem f = s.findFile("b.txt");
        assertThat(f.getActualSize(), is(equalTo(80L)));
        assertThat(f.getAbsolutePath(), is(equalTo("/sub/b.txt")));
    }

    @Test
    public void testImportStdinNoProgress() throws Exception
    {
        File archiveFile = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveBuilder.createNewEmptyArchive(archiveFile, new PlaintextDescriptor(), usp);

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(ImportFileCommand.ARG_PATH, "/b.txt");
        args.put(ImportFileCommand.ARG_SOURCE_FILE, new File("-"));
        args.put(ImportFileCommand.ARG_NO_PROGRESS, true);

        ByteArrayInputStream bais = new ByteArrayInputStream(
                "01234567890123456789012345678901234567890123456789012345678901234567890123456789".getBytes());

        System.setIn(bais);
        try (OutputCapture ignored = new OutputCapture())
        {
            new ImportFileCommand().handle(new Namespace(args));
            assertFalse(ignored.getContent().contains("==================="));
        }
        System.setIn(null);

        ArchiveInfoContext context = new ArchiveInfoContext(archiveFile, usp);

        FileInventoryItem f = context.getInventory().findFile("b.txt");
        assertThat(f.getActualSize(), is(equalTo(80L)));
        assertThat(f.getAbsolutePath(), is(equalTo("/b.txt")));
    }

    @Test
    public void testImportAsRootError() throws Exception
    {
        File archiveFile = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveBuilder.createNewEmptyArchive(archiveFile, new PlaintextDescriptor(), usp);

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(ImportFileCommand.ARG_PATH, "/");
        args.put(ImportFileCommand.ARG_SOURCE_FILE, new File("-"));

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
                archiveFile, new PlaintextDescriptor(), usp
        );

        {
            FileInventoryItem fileOne = new FileInventoryItem("a.txt");
            context.getInventory().addFile(fileOne);
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
                archiveFile, new PlaintextDescriptor(), usp
        );

        {
            FileInventoryItem fileOne = new FileInventoryItem("a.txt");
            context.getInventory().addFile(fileOne);
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
            assertThat(fileOne.getAbsolutePath(), is(equalTo("/a.txt")));
        }
    }

    @Test
    public void testImportOverFolder() throws Exception
    {
        File archiveFile = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(
                archiveFile, new PlaintextDescriptor(), usp
        );

        {
            FolderInventoryItem folderOne = new FolderInventoryItem("folder");
            context.getInventory().addFolder(folderOne);
            MetadataWriter.write(context, usp);
        }

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archiveFile);
        args.put(ImportFileCommand.ARG_PATH, "/folder");
        args.put(ImportFileCommand.ARG_SOURCE_FILE, new File("-"));

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
