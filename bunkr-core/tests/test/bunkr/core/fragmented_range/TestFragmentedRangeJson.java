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

package test.bunkr.core.fragmented_range;

import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.fragmented_range.FragmentedRangeJSON;
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
