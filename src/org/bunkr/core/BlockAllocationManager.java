package org.bunkr.core;

import org.bunkr.fragmented_range.FragmentedRange;
import org.bunkr.inventory.FileInventoryItem;
import org.bunkr.inventory.Inventory;

import java.util.Iterator;

public class BlockAllocationManager implements IBlockAllocationManager
{
    private final FragmentedRange currentAllocation = new FragmentedRange();
    private final FragmentedRange unallocatedBlocks = new FragmentedRange();
    private int highestKnownBlockId = -1;

    public BlockAllocationManager(Inventory inv, FragmentedRange startingAllocation)
    {
        currentAllocation.union(startingAllocation);
        FragmentedRange usedBlocks = new FragmentedRange();

        Iterator<FileInventoryItem> fileIterator = inv.getIterator();
        while (fileIterator.hasNext())
        {
            FileInventoryItem item = fileIterator.next();
            usedBlocks.union(item.getBlocks());
            if (!item.getBlocks().isEmpty())
            {
                highestKnownBlockId = Math.max(highestKnownBlockId, item.getBlocks().getMax());
            }
        }

        unallocatedBlocks.add(0, highestKnownBlockId + 1);
        unallocatedBlocks.subtract(usedBlocks);
    }

    @Override
    public FragmentedRange getCurrentAllocation()
    {
        return this.currentAllocation;
    }

    @Override
    public int getTotalBlocks()
    {
        return highestKnownBlockId + 1;
    }

    @Override
    public int getNextAllocatableBlockId()
    {
        if (this.unallocatedBlocks.isEmpty())
        {
            // this will give you the id of the logical 'next' block that will extend the data section
            return this.getTotalBlocks();
        }
        else
        {
            return this.unallocatedBlocks.getMin();
        }
    }

    @Override
    public int allocateBlock(int blockId)
    {
        if (!this.unallocatedBlocks.isEmpty() && !this.unallocatedBlocks.contains(blockId))
            throw new IllegalArgumentException("There are unallocated blocks that have not been allocated yet");
        if (this.unallocatedBlocks.isEmpty() && blockId != this.getNextAllocatableBlockId())
            throw new IllegalArgumentException("The next block you're allowed to allocate is " + this.getNextAllocatableBlockId());

        this.unallocatedBlocks.remove(blockId);
        this.currentAllocation.add(blockId);
        this.highestKnownBlockId = Math.max(this.highestKnownBlockId, blockId);
        return blockId;
    }

    @Override
    public void clearAllocation()
    {
        this.unallocatedBlocks.union(this.currentAllocation);
        this.currentAllocation.clear();
    }

    /**
     * Utility method. Given an Inventory object, calculate the number of used blocks. Used blocks are not the number
     * of blocks used by the files but include unallocated blocks that lie between occupied blocks. It also counts from
     * 0.
     *
     * Eg: allocated: 2, 5, 6, 10 -> 11 blocks used.
     *
     * @param inv Inventory object to iterate over
     * @return integer number of used blocks
     */
    public static long calculateUsedBlocks(Inventory inv)
    {
        long numBlocks = 0;
        Iterator<FileInventoryItem> fileIterator = inv.getIterator();
        while (fileIterator.hasNext())
        {
            FileInventoryItem item = fileIterator.next();
            if (!item.getBlocks().isEmpty()) numBlocks = Math.max(numBlocks, item.getBlocks().getMax() + 1);
        }
        return numBlocks;
    }
}
