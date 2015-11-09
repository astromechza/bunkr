package com.bunkr_beta;

import com.bunkr_beta.fragmented_range.FragmentedRange;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.Inventory;

import java.io.IOException;
import java.util.UUID;

public class BlockAllocationManager
{
    public ArchiveInfoContext context;
    public FileInventoryItem target;
    public FragmentedRange oldAllocatedBlocks;
    public FragmentedRange newAllocatedBlocks;
    public FragmentedRange nonAllocatedBlocks;
    public int nextAllocatableBlock;

    public BlockAllocationManager(ArchiveInfoContext context, FileInventoryItem target) throws IOException
    {
        this.context = context;
        this.target = target;

        context.assertFresh();

        FileInventoryItem foundItem = null;
        this.newAllocatedBlocks = new FragmentedRange();
        this.nextAllocatableBlock = (int)context.getNumBlocks();
        this.nonAllocatedBlocks = new FragmentedRange(0, this.nextAllocatableBlock);
        Inventory.InventoryIterator fileIterator = context.getArchiveInventory().getIterator();
        while (fileIterator.hasNext())
        {
            FileInventoryItem item = fileIterator.next();
            this.nonAllocatedBlocks.subtract(item.blocks);
        }
        this.oldAllocatedBlocks = target.blocks.copy();
    }

    public int consumeBlock()
    {
        int blockId;
        if(this.oldAllocatedBlocks.isEmpty())
        {
            if(this.nonAllocatedBlocks.isEmpty())
            {
                blockId = nextAllocatableBlock++;
            }
            else
            {
                blockId = nonAllocatedBlocks.popMin();
            }
        }
        else
        {
            blockId = oldAllocatedBlocks.popMin();
        }
        this.newAllocatedBlocks.add(blockId);
        return blockId;
    }

    public FragmentedRange getNewAllocatedBlocks()
    {
        return newAllocatedBlocks;
    }
}
