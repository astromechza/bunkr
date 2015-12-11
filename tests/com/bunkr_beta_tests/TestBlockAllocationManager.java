package com.bunkr_beta_tests;

import com.bunkr_beta.BlockAllocationManager;
import com.bunkr_beta.descriptor.Descriptor;
import com.bunkr_beta.cli.passwords.PasswordProvider;
import com.bunkr_beta.fragmented_range.FragmentedRange;
import com.bunkr_beta.IArchiveInfoContext;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.Inventory;
import org.bouncycastle.crypto.CryptoException;
import org.junit.Test;

import java.io.IOException;
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
    class FakeArchiveInfoContext implements IArchiveInfoContext
    {
        public Inventory inventory;

        public FakeArchiveInfoContext()
        {
            ArrayList<FileInventoryItem> files = new ArrayList<>();
            FileInventoryItem file = new FileInventoryItem("something");
            file.setBlocks(new FragmentedRange(10, 8));
            files.add(file);

            inventory = new Inventory(files, new ArrayList<>());
        }

        @Override
        public int getBlockSize()
        {
            return 1024;
        }

        @Override
        public Inventory getInventory()
        {
            return inventory;
        }

        @Override
        public long getBlockDataLength()
        {
            return this.getBlockSize() * 20;
        }

        @Override
        public void refresh(PasswordProvider uic) throws IOException, CryptoException
        {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public boolean isFresh()
        {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void invalidate()
        {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void assertFresh()
        {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public Descriptor getDescriptor()
        {
            throw new RuntimeException("Not implemented");
        }
    }


    @Test
    public void testDefault()
    {
        BlockAllocationManager bam = new BlockAllocationManager(new FakeArchiveInfoContext(), new FragmentedRange());

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
        FakeArchiveInfoContext aic = new FakeArchiveInfoContext();
        BlockAllocationManager bam = new BlockAllocationManager(aic, new FragmentedRange());

        try
        {
            bam.allocateBlock(-1);
            fail("Can allocate negative block");
        }
        catch(Exception ignored) {}

        aic.inventory = new Inventory(new ArrayList<>(), new ArrayList<>());
        bam = new BlockAllocationManager(aic, new FragmentedRange());

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
        BlockAllocationManager bam = new BlockAllocationManager(new FakeArchiveInfoContext(), new FragmentedRange());

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
