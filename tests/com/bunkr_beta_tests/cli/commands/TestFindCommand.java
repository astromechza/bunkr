package com.bunkr_beta_tests.cli.commands;

import com.bunkr_beta.ArchiveBuilder;
import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.cli.commands.FindCommand;
import com.bunkr_beta.cli.commands.MvCommand;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.descriptor.Descriptor;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.FolderInventoryItem;
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

        context.getInventory().getFiles().add(new FileInventoryItem("abc"));
        context.getInventory().getFiles().add(new FileInventoryItem("aabbcc"));
        context.getInventory().getFiles().add(new FileInventoryItem("aaabbbccc"));

        FolderInventoryItem folderOne = new FolderInventoryItem("afolder");
        folderOne.getFiles().add(new FileInventoryItem("abc"));
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
        new FindCommand().handle(new Namespace(args));
    }

}
