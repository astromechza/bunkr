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
