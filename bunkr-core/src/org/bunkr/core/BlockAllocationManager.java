/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.core;

import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.Inventory;

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
