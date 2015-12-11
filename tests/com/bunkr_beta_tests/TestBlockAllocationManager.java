package com.bunkr_beta_tests;

import com.bunkr_beta.BlockAllocationManager;
import com.bunkr_beta.fragmented_range.FragmentedRange;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.Inventory;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;

/**
 * Creator: benmeier
 * Created At: 2015-12-01
 */
public class TestBlockAllocationManager
{
    private Inventory fakeInventory()
    {

        ArrayList<FileInventoryItem> files = new ArrayList<>();
        FileInventoryItem file = new FileInventoryItem("something");
        file.setBlocks(new FragmentedRange(10, 8));
        files.add(file);
        return new Inventory(files, new ArrayList<>());
    }

    @Test
    public void testDefault()
    {
        BlockAllocationManager bam = new BlockAllocationManager(fakeInventory(), new FragmentedRange());

        assertThat(bam.getNextAllocatableBlockId(), is(equalTo(0)));
        assertThat(bam.allocateNextBlock(), is(equalTo(0)));
        assertTrue(bam.getCurrentAllocation().equals(new FragmentedRange(0, 1)));
        assertThat(bam.getTotalBlocks(), is(equalTo(18)));
        assertThat(bam.getNextAllocatableBlockId(), is(equalTo(1)));
        assertThat(bam.allocateBlock(1), is(equalTo(1)));
        assertTrue(bam.getCurrentAllocation().equals(new FragmentedRange(0, 2)));
    }

    @Test
    public void testOutOfRangeAllocations()
    {
        BlockAllocationManager bam = new BlockAllocationManager(fakeInventory(), new FragmentedRange());

        try
        {
            bam.allocateBlock(-1);
            fail("Can allocate negative block");
        }
        catch(Exception ignored) {}

        bam = new BlockAllocationManager(new Inventory(new ArrayList<>(), new ArrayList<>()), new FragmentedRange());

        try
        {
            bam.allocateBlock(10000);
            fail("Can allocate out of range block");
        }
        catch(Exception ignored) {}
    }

    @Test
    public void testUnallocatedBlocks()
    {
        BlockAllocationManager bam = new BlockAllocationManager(fakeInventory(), new FragmentedRange());

        for (int i = 0; i <= 9; i++)
        {
            assertThat(bam.allocateNextBlock(), is(equalTo(i)));
        }


        for (int i = 18; i <= 21; i++)
        {
            assertThat(bam.allocateNextBlock(), is(equalTo(i)));
        }

        assertThat(bam.getTotalBlocks(), is(equalTo(22)));
        FragmentedRange r = new FragmentedRange();
        r.add(0, 10);
        r.add(18, 4);
        assertTrue(bam.getCurrentAllocation().equals(r));

    }
}
