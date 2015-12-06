package com.bunkr_beta_tests.commands;

import com.bunkr_beta.cli.commands.MkdirCommand;
import com.bunkr_beta.cli.commands.RmdirCommand;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.FolderInventoryItem;
import com.bunkr_beta.inventory.Inventory;
import com.bunkr_beta.inventory.InventoryPather;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertFalse;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class TestRmdirCommand
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
    public void testRmdirSingleEmpty() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        InventoryPather.traverse(inv, "/d1").isAFolder();
        new RmdirCommand().rmdir(inv, "/d1", false);
        try
        {
            InventoryPather.traverse(inv, "/d1");
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}
    }

    @Test
    public void testRmdirSingleNonEmpty() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        InventoryPather.traverse(inv, "/d2").isAFolder();
        try
        {
            new RmdirCommand().rmdir(inv, "/d2", false);
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}
        InventoryPather.traverse(inv, "/d2").isAFolder();
        new RmdirCommand().rmdir(inv, "/d2", true);
        try
        {
            InventoryPather.traverse(inv, "/d2");
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}

    }

    @Test
    public void testRmdirLevelTwoEmpty() throws TraversalException
    {
        Inventory inv = makeSampleInventory();
        InventoryPather.traverse(inv, "/d2/d2.d3").isAFolder();
        new RmdirCommand().rmdir(inv, "/d2/d2.d3", false);
        try
        {
            InventoryPather.traverse(inv, "/d2/d2.d3");
            fail("did not through traversal exception");
        }
        catch(TraversalException ignored) {}

    }





}
