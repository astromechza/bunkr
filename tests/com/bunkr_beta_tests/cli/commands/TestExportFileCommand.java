package com.bunkr_beta_tests.cli.commands;

import com.bunkr_beta.ArchiveBuilder;
import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.RandomMaker;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.cli.commands.ExportFileCommand;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.descriptor.Descriptor;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.FolderInventoryItem;
import com.bunkr_beta.streams.output.MultilayeredOutputStream;
import com.bunkr_beta_tests.XTemporaryFolder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.bouncycastle.crypto.CryptoException;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
public class TestExportFileCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    @Test
    public void testBuildParser()
    {
        new ExportFileCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    public File buildArchive() throws IOException, CryptoException
    {
        File archiveFile = folder.newFile();

        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(archiveFile, new Descriptor(null, null), new PasswordProvider());

        FileInventoryItem fileOne = new FileInventoryItem("a.txt");
        context.getInventory().getFiles().add(fileOne);
        try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileOne))
        {
            bwos.write(RandomMaker.get(3333 * 8));
        }

        FolderInventoryItem folderOne = new FolderInventoryItem("folder");

        FileInventoryItem fileTwo = new FileInventoryItem("b.txt");
        context.getInventory().getFiles().add(fileTwo);
        try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, fileTwo))
        {
            bwos.write(RandomMaker.get(50 * 8));
        }

        folderOne.getFiles().add(fileTwo);
        context.getInventory().getFolders().add(folderOne);

        MetadataWriter.write(context, new PasswordProvider());

        return archiveFile;
    }

    @Test
    public void testExportToStdout() throws Exception
    {
        File archive = buildArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archive);
        args.put(ExportFileCommand.ARG_PATH, "/a.txt");
        args.put(ExportFileCommand.ARG_DESTINATION_FILE, new File("-"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        new ExportFileCommand().handle(new Namespace(args));
        System.setOut(null);
        assertThat(baos.toByteArray().length, is(equalTo(3333)));
    }

    @Test
    public void testExportToFile() throws Exception
    {
        File archive = buildArchive();
        File outputFile = folder.newFilePath();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archive);
        args.put(ExportFileCommand.ARG_PATH, "/folder/b.txt");
        args.put(ExportFileCommand.ARG_DESTINATION_FILE, outputFile);

        new ExportFileCommand().handle(new Namespace(args));
        assertThat(outputFile.length(), is(equalTo(50L)));
    }

    @Test
    public void testExportAFolder() throws Exception
    {
        File archive = buildArchive();
        File outputFile = folder.newFilePath();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archive);
        args.put(ExportFileCommand.ARG_PATH, "/folder");
        args.put(ExportFileCommand.ARG_DESTINATION_FILE, outputFile);

        try
        {
            new ExportFileCommand().handle(new Namespace(args));
            fail("Should not be able to export /folder");
        }
        catch (CLIException ignored) {}
    }

    @Test
    public void testExportRoot() throws Exception
    {
        File archive = buildArchive();
        File outputFile = folder.newFilePath();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archive);
        args.put(ExportFileCommand.ARG_PATH, "/");
        args.put(ExportFileCommand.ARG_DESTINATION_FILE, outputFile);

        try
        {
            new ExportFileCommand().handle(new Namespace(args));
            fail("Should not be able to export /");
        }
        catch (CLIException ignored) {}
    }


    @Test
    public void testExportCannotOverwrite() throws Exception
    {
        File archive = buildArchive();
        File outputFile = folder.newFile();
        assertTrue(outputFile.exists());

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, archive);
        args.put(ExportFileCommand.ARG_PATH, "/a.txt");
        args.put(ExportFileCommand.ARG_DESTINATION_FILE, outputFile);

        try
        {
            new ExportFileCommand().handle(new Namespace(args));
            fail("Should not be able to overwrite");
        }
        catch (CLIException ignored) {}
    }
}