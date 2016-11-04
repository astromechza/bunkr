package test.bunkr.core.operations;

import org.bunkr.core.inventory.Algorithms;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.Inventory;
import org.bunkr.core.operations.BlockDefragmentation;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created At: 2016-11-04
 */
public class TestBlockDefragmentation
{
    @Test
    public void testEmpty() throws Exception
    {
        Inventory inv = new Inventory(new ArrayList<>(), new ArrayList<>(), Algorithms.Encryption.NONE);
        List<FileInventoryItem> result = BlockDefragmentation.calculateFilesThatRequireAMove(inv);
        assertEquals(result.size(), 0);
    }

    @Test
    public void testSingleFile() throws Exception
    {
        List<FileInventoryItem> files = new ArrayList<>();
        FileInventoryItem f1 = new FileInventoryItem("1"); f1.getBlocks().addAll(2, 3, 4); files.add(f1);

        Inventory inv = new Inventory(files, new ArrayList<>(), Algorithms.Encryption.NONE);
        List<FileInventoryItem> result = BlockDefragmentation.calculateFilesThatRequireAMove(inv);
        assertEquals(result.get(0), f1);
    }

    @Test
    public void testTwoFiles() throws Exception
    {
        List<FileInventoryItem> files = new ArrayList<>();
        FileInventoryItem f1 = new FileInventoryItem("1"); f1.getBlocks().addAll(2, 3, 4); files.add(f1);
        FileInventoryItem f2 = new FileInventoryItem("2"); f2.getBlocks().addAll(6, 7); files.add(f2);

        Inventory inv = new Inventory(files, new ArrayList<>(), Algorithms.Encryption.NONE);
        List<FileInventoryItem> result = BlockDefragmentation.calculateFilesThatRequireAMove(inv);
        assertEquals(result.size(), 1);
    }

    @Test
    public void testTwoFilesFromStart() throws Exception
    {
        List<FileInventoryItem> files = new ArrayList<>();
        FileInventoryItem f1 = new FileInventoryItem("1"); f1.getBlocks().addAll(0, 2, 4); files.add(f1);
        FileInventoryItem f2 = new FileInventoryItem("2"); f2.getBlocks().addAll(5, 6); files.add(f2);

        Inventory inv = new Inventory(files, new ArrayList<>(), Algorithms.Encryption.NONE);
        List<FileInventoryItem> result = BlockDefragmentation.calculateFilesThatRequireAMove(inv);
        assertEquals(result.size(), 1);
    }
}
