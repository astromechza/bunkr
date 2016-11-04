package org.bunkr.core.operations;

import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.Inventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * Created At: 2016-11-04
 */
public class BlockDefragmentation
{
    /**
     * Given an archive with a fragmented range, determine the files that need to be rewritten to fill up all free
     * blocks remaining in the block collection.
     */
    public static List<FileInventoryItem> calculateFilesThatRequireAMove(Inventory inventory)
    {
        // set up the output structure first
        List<FileInventoryItem> output = new ArrayList<>();

        // first gather the entire fragmented range of all files
        FragmentedRange stateRange = new FragmentedRange();
        // also gather an ordered queue of the files based on the last block they occupy
        TreeMap<Integer, FileInventoryItem> endBlockOrder = new TreeMap<>();

        Iterator<FileInventoryItem> fit = inventory.getIterator();
        while(fit.hasNext())
        {
            FileInventoryItem current = fit.next();
            if (!current.getBlocks().isEmpty())
            {
                stateRange.union(current.getBlocks());
                endBlockOrder.put(current.getBlocks().getMax(), current);
            }
        }

        if (stateRange.isEmpty()) return output;

        int firstB = stateRange.getMin();
        if (stateRange.isContinuous() && firstB == 0) return output;

        FragmentedRange unallocatedBlocks = stateRange.invert();
        unallocatedBlocks.add(0, firstB);

        // now while there are still empty blocks, we rewrite the last file
        while (stateRange.isFragmented() || (!stateRange.isEmpty() && stateRange.getMin() != 0))
        {
            // grab the last file
            FileInventoryItem lastFile = endBlockOrder.pollLastEntry().getValue();
            // remove the blocks from the current state
            stateRange.subtract(lastFile.getBlocks());

            // now allocate new blocks for the file
            int numBlocks = lastFile.getBlocks().size();
            unallocatedBlocks.union(lastFile.getBlocks());
            while (numBlocks > 0)
            {
                int nextBlock = unallocatedBlocks.getMin();
                stateRange.add(nextBlock);
                unallocatedBlocks.remove(nextBlock);
                numBlocks--;
            }
            output.add(lastFile);
        }

        return output;
    }
}
