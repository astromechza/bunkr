package org.bunkr_tests.fragmented_range;

import org.bunkr.fragmented_range.FragmentedRange;
import org.bunkr.fragmented_range.FragmentedRangeJSON;
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
        assertEquals("[-9,2,1,2,6,2]", FragmentedRangeJSON.encode(fr));
    }

    @Test
    public void testDeserialize() throws IOException
    {
        FragmentedRange fr = FragmentedRangeJSON.decode("[-9, 2,1, 2, 6, 2]");
        assertEquals(fr.toList(), Arrays.asList(
                -9, -8, 1, 2, 6, 7
        ));
    }

    @Test
    public void testSerialize2() throws IOException
    {
        FragmentedRange fr = new FragmentedRange();
        fr.add(1, 2);
        fr.add(6, 2);
        fr.add(-9, 2);
        assertEquals("[-9,2,1,2,6,2]", FragmentedRangeJSON.encode(fr));
    }

    @Test
    public void testDeserialize2() throws IOException
    {
        FragmentedRange fr = FragmentedRangeJSON.decode("[-9, 2,1, 2, 6, 2]");
        assertEquals(fr.toList(), Arrays.asList(
                -9, -8, 1, 2, 6, 7
        ));
    }

}
