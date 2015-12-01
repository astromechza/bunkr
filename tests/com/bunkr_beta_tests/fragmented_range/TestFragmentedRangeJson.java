package com.bunkr_beta_tests.fragmented_range;

import com.bunkr_beta.JSONHelper;
import com.bunkr_beta.fragmented_range.FragmentedRange;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class TestFragmentedRangeJson
{
    @Test
    public void testSerialize() throws IOException
    {
        FragmentedRange fr = new FragmentedRange();
        fr.add(1, 2);
        fr.add(6, 2);
        fr.add(-9, 2);
        assertEquals("[-9,2,1,2,6,2]", JSONHelper.stringify(fr));
    }

    @Test
    public void testDeserialize() throws IOException
    {
        FragmentedRange fr = JSONHelper.unstringify("[-9, 2,1, 2, 6, 2]", FragmentedRange.class);
        assertEquals(fr.toList(), Arrays.asList(
                -9, -8, 1, 2, 6, 7
        ));
    }
}
