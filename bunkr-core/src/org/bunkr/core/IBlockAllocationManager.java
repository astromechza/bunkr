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

/**
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
