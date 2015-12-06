package com.bunkr_beta_tests.commands;

import com.bunkr_beta.cli.commands.MkdirCommand;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.FolderInventoryItem;
import com.bunkr_beta.inventory.Inventory;
import com.bunkr_beta.inventory.InventoryPather;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class TestMkdirCommands
{
    public Inventory makeSampleInventory()
    {
        Inventory i = new Inventory(new ArrayList<>(), new ArrayList<>());
        FolderInventoryItem d1 = new FolderInventoryItem("d1");
        i.getFolders().add(d1);
        FolderInventoryItem d2 = new FolderInventoryItem("d2");
        i.getFolders().add(d2);
        FolderInventoryItem d3 = new FolderInventoryItem("d2.d3");
        d2.getFolders().add(d3);

        FileInventoryItem f1 = new FileInventoryItem("f1");
        i.getFiles().add(f1);
        FileInventoryItem f2 = new FileInventoryItem("f2");
        i.getFiles().add(f2);
        FileInventoryItem f3 = new FileInventoryItem("d2.f3");
        d2.getFiles().add(f3);

        return i;
    }

    @Test
    public void testMkdirInRoot() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        new MkdirCommand().mkdirs(inv, "/d4", false);
        InventoryPather.traverse(inv, "/d4").isAFolder();
    }

    @Test
    public void testMkdirInExistingDir() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        new MkdirCommand().mkdirs(inv, "/d1/d4", false);
        InventoryPather.traverse(inv, "/d1/d4").isAFolder();
    }

    @Test
    public void testMkdirInExistingDirs() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        new MkdirCommand().mkdirs(inv, "/d2/d2.d3/d4", false);
        InventoryPather.traverse(inv, "/d2/d2.d3/d4").isAFolder();
    }

    @Test
    public void testMkdirRecursiveInRoot() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        new MkdirCommand().mkdirs(inv, "/d4/d5/d6", true);
        InventoryPather.traverse(inv, "/d4/d5/d6").isAFolder();
    }

    @Test
    public void testMkdirRecursiveInADir() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        new MkdirCommand().mkdirs(inv, "/d2/d4/d5", true);
        InventoryPather.traverse(inv, "/d2/d4/d5").isAFolder();
    }

    @Test
    public void testMkdirFailInFile() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        try
        {
            new MkdirCommand().mkdirs(inv, "/d2/d2.f3/d5", false);
        }
        catch (TraversalException ignored) {}
        try
        {
            new MkdirCommand().mkdirs(inv, "/d2/d2.f3/d5", true);
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
        }
        catch (TraversalException ignored) {}
    }

}
