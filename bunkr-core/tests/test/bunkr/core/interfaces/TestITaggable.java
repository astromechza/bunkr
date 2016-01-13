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

package test.bunkr.core.interfaces;

import org.bunkr.core.inventory.ITaggable;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public class TestITaggable
{
    class FakeTaggable implements ITaggable
    {
        Set<String> tags = new HashSet<>();

        @Override
        public void setTags(Set<String> tags)
        {
            this.tags = tags;
        }

        @Override
        public Set<String> getTags()
        {
            return this.tags;
        }
    }

    @Test
    public void testFlow()
    {
        ITaggable t = new FakeTaggable();

        assertThat(t.getTags().size(), is(equalTo(0)));
        assertFalse(t.hasTag("x"));
        assertFalse(t.hasTag(""));

        assertTrue(t.addTag("thing"));
        assertFalse(t.addTag("thing"));
        assertTrue(t.hasTag("thing"));
        assertThat(t.getTags().size(), is(equalTo(1)));
        assertTrue(t.removeTag("thing"));
        assertFalse(t.removeTag("thing"));
        assertFalse(t.hasTag("thing"));

        t.addTag("bob");
        t.addTag("charles");

        assertThat(t.getTags().size(), is(equalTo(2)));

        HashSet<String> tags2 = new HashSet<>();
        tags2.add("fish");
        tags2.add("dog");
        t.setTags(tags2);

        assertThat(t.getTags().size(), is(equalTo(2)));
    }

    @Test
    public void testBadTags()
    {
        ITaggable t = new FakeTaggable();

        try
        {
            t.addTag("");
            fail("Could add empty tag");
        }
        catch (IllegalArgumentException ignored) {}

        try
        {
            t.addTag("a");
            fail("Could add short tag");
        }
        catch (IllegalArgumentException ignored) {}

        try
        {
            t.addTag("bad alphabet");
            fail("Could add tag with spaces");
        }
        catch (IllegalArgumentException ignored) {}

        try
        {
            t.addTag("0aaaaa");
            fail("Could add starting with digit");
        }
        catch (IllegalArgumentException ignored) {}

        try
        {
            t.addTag("aaaaaa.");
            fail("Could add ending with dot");
        }
        catch (IllegalArgumentException ignored) {}
    }


}
