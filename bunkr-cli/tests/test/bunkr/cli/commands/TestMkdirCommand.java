package test.bunkr.cli.commands;

import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.cli.CLI;
import org.bunkr.cli.commands.MkdirCommand;
import org.bunkr.core.inventory.*;
import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.exceptions.TraversalException;
import test.bunkr.core.XTemporaryFolder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class TestMkdirCommand
{
    @Rule
    public final XTemporaryFolder folder = new XTemporaryFolder();

    public void tPathCheck(Inventory i, String path) throws TraversalException
    {
        assertThat(((InventoryItem) InventoryPather.traverse(i, path)).getAbsolutePath(), is(equalTo(path)));
    }

    public Inventory makeSampleInventory()
    {
        Inventory i = new Inventory(new ArrayList<>(), new ArrayList<>(), false, false);
        FolderInventoryItem d1 = new FolderInventoryItem("d1");
        i.addFolder(d1);
        FolderInventoryItem d2 = new FolderInventoryItem("d2");
        i.addFolder(d2);
        FolderInventoryItem d3 = new FolderInventoryItem("d2.d3");
        d2.addFolder(d3);

        FileInventoryItem f1 = new FileInventoryItem("f1");
        i.addFile(f1);
        FileInventoryItem f2 = new FileInventoryItem("f2");
        i.addFile(f2);
        FileInventoryItem f3 = new FileInventoryItem("d2.f3");
        d2.addFile(f3);

        return i;
    }

    public ArchiveInfoContext buildSampleArchive() throws Exception
    {
        File archivePath = folder.newFile();
        UserSecurityProvider usp = new UserSecurityProvider(new PasswordProvider());
        ArchiveInfoContext context = ArchiveBuilder.createNewEmptyArchive(archivePath, new PlaintextDescriptor(), usp, false);

        FolderInventoryItem d1 = new FolderInventoryItem("d1");
        context.getInventory().addFolder(d1);
        FolderInventoryItem d2 = new FolderInventoryItem("d2");
        context.getInventory().addFolder(d2);
        FolderInventoryItem d3 = new FolderInventoryItem("d2.d3");
        d2.addFolder(d3);

        FileInventoryItem f1 = new FileInventoryItem("f1");
        context.getInventory().addFile(f1);
        FileInventoryItem f2 = new FileInventoryItem("f2");
        context.getInventory().addFile(f2);
        FileInventoryItem f3 = new FileInventoryItem("d2.f3");
        d2.addFile(f3);

        MetadataWriter.write(context, usp);

        return context;
    }

    @Test
    public void testBuildParser()
    {
        new MkdirCommand().buildParser(ArgumentParsers.newArgumentParser("abc").addSubparsers().addParser("xyz"));
    }

    @Test
    public void testMkdirInRoot() throws Exception
    {
        ArchiveInfoContext context = buildSampleArchive();

        Map<String, Object> args = new HashMap<>();
        args.put(CLI.ARG_ARCHIVE_PATH, context.filePath);
        args.put(MkdirCommand.ARG_PATH, "/d4");
        args.put(MkdirCommand.ARG_RECURSIVE, false);
        new MkdirCommand().handle(new Namespace(args));

        context.refresh(new UserSecurityProvider(new PasswordProvider()));
        InventoryPather.traverse(context.getInventory(), "/d4").isAFolder();
        tPathCheck(context.getInventory(), "/d4");
    }

    @Test
    public void testMkdirInExistingDir() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        new MkdirCommand().mkdirs(inv, "/d1/d4", false);
        InventoryPather.traverse(inv, "/d1/d4").isAFolder();
        tPathCheck(inv, "/d1/d4");
    }

    @Test
    public void testMkdirInExistingDirs() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        new MkdirCommand().mkdirs(inv, "/d2/d2.d3/d4", false);
        InventoryPather.traverse(inv, "/d2/d2.d3/d4").isAFolder();
        tPathCheck(inv, "/d2/d2.d3/d4");
    }

    @Test
    public void testMkdirRecursiveInRoot() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        new MkdirCommand().mkdirs(inv, "/d4/d5/d6", true);
        InventoryPather.traverse(inv, "/d4/d5/d6").isAFolder();
        tPathCheck(inv, "/d4/d5/d6");
    }

    @Test
    public void testMkdirRecursiveInADir() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        new MkdirCommand().mkdirs(inv, "/d2/d4/d5", true);
        InventoryPather.traverse(inv, "/d2/d4/d5").isAFolder();
        tPathCheck(inv, "/d2/d4/d5");
    }

    @Test
    public void testMkdirFailInFile() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        try
        {
            new MkdirCommand().mkdirs(inv, "/d2/d2.f3/d5", false);
            fail("did not through traversal exception");
        }
        catch (TraversalException ignored) {}
        try
        {
            new MkdirCommand().mkdirs(inv, "/d2/d2.f3/d5", true);
            fail("did not through traversal exception");
        }
        catch (TraversalException ignored) {}
    }

    @Test
    public void testMkdirFailInMissingFolder() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        try
        {
            new MkdirCommand().mkdirs(inv, "/d2/d4/d5", false);
            fail("did not through traversal exception");
        }
        catch (TraversalException ignored) {}
    }

}
