package com.bunkr_beta_tests.commands;

import com.bunkr_beta.cli.commands.RmCommand;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.FolderInventoryItem;
import com.bunkr_beta.inventory.Inventory;
import com.bunkr_beta.inventory.InventoryPather;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.fail;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class TestRmdirCommand
{
    public Inventory makeSampleInventory()
    {
        Inventory i = new Inventory(new ArrayList<>(), new ArrayList<>());
        FolderInventoryItem d1 = new FolderInventoryItem("t1");
        i.getFolders().add(d1);

        FolderInventoryItem d2 = new FolderInventoryItem("t2");
        i.getFolders().add(d2);
        d2.getFiles().add(new FileInventoryItem("file"));

        FolderInventoryItem d3 = new FolderInventoryItem("t3");
        i.getFolders().add(d3);
        d3.getFolders().add(new FolderInventoryItem("file"));

        FileInventoryItem t4 = new FileInventoryItem("t4");
        i.getFiles().add(t4);

        return i;
    }

    @Test
    public void testRmdirEmpty() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        InventoryPather.traverse(inv, "/t1").isAFolder();
        new RmCommand().deleteItem(inv, "/t1", false);
        try
        {
            InventoryPather.traverse(inv, "/t1");
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}
    }

    @Test
    public void testRmdirNonEmptyWithFolder() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        InventoryPather.traverse(inv, "/t2").isAFolder();
        try
        {
            new RmCommand().deleteItem(inv, "/t2", false);
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}
        InventoryPather.traverse(inv, "/t2").isAFolder();
        new RmCommand().deleteItem(inv, "/t2", true);
        try
        {
            InventoryPather.traverse(inv, "/t2");
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}
    }

    @Test
    public void testRmdirNonEmptyWithFile() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        InventoryPather.traverse(inv, "/t3").isAFolder();
        try
        {
            new RmCommand().deleteItem(inv, "/t3", false);
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}
        InventoryPather.traverse(inv, "/t3").isAFolder();
        new RmCommand().deleteItem(inv, "/t3", true);
        try
        {
            InventoryPather.traverse(inv, "/t3");
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}
    }

    @Test
    public void testRmdirAFile() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        InventoryPather.traverse(inv, "/t4").isAFile();
        try
        {
            new RmCommand().deleteItem(inv, "/t4", false);
        }
        catch(TraversalException ignored) {}
    }

    @Test
    public void testRmdirInAFile() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        try
        {
            new RmCommand().deleteItem(inv, "/t4/t4", false);
        }
        catch(TraversalException ignored) {}
    }

    @Test
    public void testRmdirRoot()
    {
        Inventory inv = makeSampleInventory();
        try
        {
            new RmCommand().deleteItem(inv, "/", true);
        }
        catch(TraversalException ignored) {}
    }
}
