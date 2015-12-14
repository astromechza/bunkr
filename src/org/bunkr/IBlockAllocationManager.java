package org.bunkr;

import org.bunkr.fragmented_range.FragmentedRange;

/**
 * Creator: benmeier
 * Created At: 2015-11-17
 *
 * IBlockAllocationManager is a structure used for managing block allocations in a archive.
 *
 *
 */
public interface IBlockAllocationManager
{
    /**
     * @return the blocks currently allocated to this file
     */
    FragmentedRange getCurrentAllocation();

    /**
     * Clear the allocations for the given file
     */
    void clearAllocation();

    /**
     * @return the number of blocks managed by this allocation manager including unallocated blocks.
     */
    int getTotalBlocks();

    /**
     * @return the id of the next block that can be allocated
     */
    int getNextAllocatableBlockId();

    /**
     * Allocate the given block to the file
     * @param blockId the id of the block to allocate
     * @return the blockId
     */
    int allocateBlock(int blockId);

    /**
     * Allocate the next available block to the file.
     * @return the blockId
     */
    default int allocateNextBlock()
    {
        return this.allocateBlock(this.getNextAllocatableBlockId());
    }

}
