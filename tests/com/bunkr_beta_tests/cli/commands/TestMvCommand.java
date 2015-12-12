package com.bunkr_beta_tests.cli.commands;

import com.bunkr_beta.ArchiveBuilder;
import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.cli.commands.MvCommand;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.descriptor.Descriptor;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.FolderInventoryItem;
import com.bunkr_beta.inventory.InventoryPather;
import com.bunkr_beta_tests.XTemporaryFolder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.bouncycastle.crypto.CryptoException;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Creator: benmeier
 * Created At: 2015-12-12
 */
public class TestMvCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    public ArchiveInfoContext buildSampleArchive() throws IOException, CryptoException
    {
        File archivePath = folder.newFile();
        ArchiveInfoContext context = ArchiveBuilder
                .createNewEmptyArchive(archivePath, new Descriptor(null, null), new PasswordProvider());

        FolderInventoryItem d1 = new FolderInventoryItem("t1");
        context.getInventory().getFolders().add(d1);

        FolderInventoryItem d2 = new FolderInventoryItem("t2");
        context.getInventory().getFolders().add(d2);
        d2.getFiles().add(new FileInventoryItem("file"));

        FolderInventoryItem d3 = new FolderInventoryItem("t3");
        context.getInventory().getFolders().add(d3);
        d3.getFiles().add(new FileInventoryItem("file"));

        FileInventoryItem t4 = new FileInventoryItem("t4");
        context.getInventory().getFiles().add(t4);

        MetadataWriter.write(context, new PasswordProvider());

        return context;
    }

    @Test
    public void testBuildParser()
    {
        new MvCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    @Test
    public void testMoveFile() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(MvCommand.ARG_FROMPATH, "/t4");
        args.put(MvCommand.ARG_TOPATH, "/x4");
        new MvCommand().handle(new Namespace(args));

        context.refresh(new PasswordProvider());
        assertTrue(context.getInventory().hasFile("x4"));
        assertFalse(context.getInventory().hasFile("t4"));
    }


    @Test
    public void testMoveFileToFolder() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(MvCommand.ARG_FROMPATH, "/t4");
        args.put(MvCommand.ARG_TOPATH, "/t1/x4");
        new MvCommand().handle(new Namespace(args));

        context.refresh(new PasswordProvider());

        assertTrue(InventoryPather.traverse(context.getInventory(), "/t1").isAFolder());

        try
        {
            InventoryPather.traverse(context.getInventory(), "/t4");
        }
        catch (TraversalException ignored) { }
        assertTrue(InventoryPather.traverse(context.getInventory(), "/t1/x4").isAFile());
    }

    @Test
    public void testMoveFileToRoot() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(MvCommand.ARG_FROMPATH, "/t3/file");
        args.put(MvCommand.ARG_TOPATH, "/rootfile");
        new MvCommand().handle(new Namespace(args));

        context.refresh(new PasswordProvider());

        try
        {
            InventoryPather.traverse(context.getInventory(), "/t3/file");
        }
        catch (TraversalException ignored) { }

        assertTrue(InventoryPather.traverse(context.getInventory(), "/rootfile").isAFile());
    }

    @Test
    public void testMoveFolder() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(MvCommand.ARG_FROMPATH, "/t1");
        args.put(MvCommand.ARG_TOPATH, "/x1");
        new MvCommand().handle(new Namespace(args));

        context.refresh(new PasswordProvider());
        assertTrue(context.getInventory().hasFolder("x1"));
        assertFalse(context.getInventory().hasFolder("t1"));
    }


    @Test
    public void testMoveFolderToFolder() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(MvCommand.ARG_FROMPATH, "/t1");
        args.put(MvCommand.ARG_TOPATH, "/t2/x1");
        new MvCommand().handle(new Namespace(args));

        context.refresh(new PasswordProvider());

        try
        {
            InventoryPather.traverse(context.getInventory(), "/t1");
        }
        catch (TraversalException ignored) { }
        assertTrue(InventoryPather.traverse(context.getInventory(), "/t2/x1").isAFolder());
    }

    @Test
    public void testMoveFolderToRoot() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(MvCommand.ARG_FROMPATH, "/t1");
        args.put(MvCommand.ARG_TOPATH, "/t6");
        new MvCommand().handle(new Namespace(args));

        context.refresh(new PasswordProvider());

        try
        {
            InventoryPather.traverse(context.getInventory(), "/t1");
        }
        catch (TraversalException ignored) { }

        assertTrue(InventoryPather.traverse(context.getInventory(), "/t6").isAFolder());
    }

    @Test
    public void testFileMoveToMissingFolder() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(MvCommand.ARG_FROMPATH, "/t1");
        args.put(MvCommand.ARG_TOPATH, "/t6/x1");
        try
        {
            new MvCommand().handle(new Namespace(args));
        }
        catch (TraversalException ignored) { }
    }

    @Test
    public void testFileMoveToFileChild() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(MvCommand.ARG_FROMPATH, "/t1");
        args.put(MvCommand.ARG_TOPATH, "/t2/file/x1");
        try
        {
            new MvCommand().handle(new Namespace(args));
        }
        catch (CLIException ignored) { }
    }

    @Test
    public void testFileMoveToExisting() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(MvCommand.ARG_FROMPATH, "/t1");
        args.put(MvCommand.ARG_TOPATH, "/t2/file");
        try
        {
            new MvCommand().handle(new Namespace(args));
        }
        catch (CLIException ignored) { }
    }

    @Test
    public void testFolderMoveToExisting() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(MvCommand.ARG_FROMPATH, "/t1");
        args.put(MvCommand.ARG_TOPATH, "/t2");
        try
        {
            new MvCommand().handle(new Namespace(args));
        }
        catch (CLIException ignored) { }
    }
}
