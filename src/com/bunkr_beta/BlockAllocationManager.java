package com.bunkr_beta;

import com.bunkr_beta.fragmented_range.FragmentedRange;
import com.bunkr_beta.interfaces.IArchiveInfoContext;
import com.bunkr_beta.interfaces.IBlockAllocationManager;
import com.bunkr_beta.inventory.FileInventoryItem;

import java.util.Iterator;

public class BlockAllocationManager implements IBlockAllocationManager
{
    private final FragmentedRange currentAllocation = new FragmentedRange();
    private final FragmentedRange unallocatedBlocks = new FragmentedRange();
    private int highestKnownBlockId = -1;

    public BlockAllocationManager(IArchiveInfoContext context, FragmentedRange startingAllocation)
    {
        currentAllocation.union(startingAllocation);
        FragmentedRange usedBlocks = new FragmentedRange();

        Iterator<FileInventoryItem> fileIterator = context.getInventory().getIterator();
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
}
