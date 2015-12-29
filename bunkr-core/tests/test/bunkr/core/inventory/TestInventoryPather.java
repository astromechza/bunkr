package test.bunkr.core.inventory;

import org.bunkr.core.exceptions.TraversalException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.FolderInventoryItem;
import org.bunkr.core.inventory.Inventory;
import org.bunkr.core.inventory.InventoryPather;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class TestInventoryPather
{
    @Test
    public void testIsValidTrues()
    {
        assertTrue(InventoryPather.isValidPath("/"));
        assertTrue(InventoryPather.isValidPath("/a"));
        assertTrue(InventoryPather.isValidPath("/a/b"));
        assertTrue(InventoryPather.isValidPath("/aawd spaces"));
        assertTrue(InventoryPather.isValidPath("/something/what/what/tuff"));
        assertTrue(InventoryPather.isValidPath("/something/fn.jpg"));
        assertTrue(InventoryPather.isValidPath("/something/fn-one.jpg"));
        assertTrue(InventoryPather.isValidPath("/something____funny.tuff"));
    }

    @Test
    public void testIsValidFalses()
    {
        assertFalse(InventoryPather.isValidPath(""));
        assertFalse(InventoryPather.isValidPath("//"));
        assertFalse(InventoryPather.isValidPath("   "));
        assertFalse(InventoryPather.isValidPath("/something/what\r\n"));
        assertFalse(InventoryPather.isValidPath("/something/\ttab"));
        assertFalse(InventoryPather.isValidPath("/something/!@#(*!@&"));
        assertFalse(InventoryPather.isValidPath("/something[]"));
    }

    @Test
    public void testAssertValid()
    {
        InventoryPather.assertValidPath("/");
        InventoryPather.assertValidPath("/a/b");
        try
        {
            InventoryPather.assertValidPath("/some //bad path(*!@");
        }
        catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void testGetParts()
    {
        assertThat(InventoryPather.getParts("/"), is(equalTo(new String[]{ })));
        assertThat(InventoryPather.getParts("/a/b"), is(equalTo(new String[]{ "a", "b" })));
        assertThat(InventoryPather.getParts("/something/bob/file.ext"),
                   is(equalTo(new String[]{ "something", "bob", "file.ext" })));
    }

    @Test
    public void testDirname()
    {
        assertThat(InventoryPather.dirname("/"), is(equalTo("/")));
        assertThat(InventoryPather.dirname("/foo/bar/delta"), is(equalTo("/foo/bar")));
        assertThat(InventoryPather.dirname("/delta"), is(equalTo("/")));
    }

    @Test
    public void testBaseName()
    {
        assertThat(InventoryPather.baseName("/"), is(equalTo("")));
        assertThat(InventoryPather.baseName("/foo/bar/delta"), is(equalTo("delta")));
        assertThat(InventoryPather.baseName("/delta"), is(equalTo("delta")));
    }

    @Test
    public void testJoin()
    {
        assertThat(InventoryPather.simpleJoin("/", "bob"), is(equalTo("/bob")));
        assertThat(InventoryPather.simpleJoin("/a", "bob"), is(equalTo("/a/bob")));
        assertThat(InventoryPather.simpleJoin("/a/b", "bob"), is(equalTo("/a/b/bob")));
    }

    private static Inventory buildFakeInventory()
    {
        Inventory i = new Inventory(new ArrayList<>(), new ArrayList<>(), false, false);
        FolderInventoryItem d1 = new FolderInventoryItem("d1");
        i.getFolders().add(d1);
        FolderInventoryItem d2 = new FolderInventoryItem("d2");
        i.getFolders().add(d2);
        FolderInventoryItem d3 = new FolderInventoryItem("d2.d3");
        d2.getFolders().add(d3);

        FileInventoryItem f1 = new FileInventoryItem("f1");
        i.getFiles().add(f1);
        FileInventoryItem f2 = new FileInventoryItem("f2");
        i.getFiles().add(f2);
        FileInventoryItem f3 = new FileInventoryItem("d2.f3");
        d2.getFiles().add(f3);

        return i;
    }

    @Test
    public void testTraverseToRoot() throws Exception
    {
        Inventory i = buildFakeInventory();
        assertThat(InventoryPather.traverse(i, "/"), is(equalTo(i)));
    }

    @Test
    public void testTraverseToOneLevelFile() throws Exception
    {
        Inventory i = buildFakeInventory();
        FileInventoryItem f = (FileInventoryItem) InventoryPather.traverse(i, "/f1");
        assertThat(f.getName(), is(equalTo("f1")));
    }

    @Test
    public void testTraverseToOneLevelFolder() throws Exception
    {
        Inventory i = buildFakeInventory();
        FolderInventoryItem f = (FolderInventoryItem) InventoryPather.traverse(i, "/d2");
        assertThat(f.getName(), is(equalTo("d2")));
    }

    @Test
    public void testTraverseToSecondLevelFile() throws Exception
    {
        Inventory i = buildFakeInventory();
        FileInventoryItem f = (FileInventoryItem) InventoryPather.traverse(i, "/d2/d2.f3");
        assertThat(f.getName(), is(equalTo("d2.f3")));
    }

    @Test
    public void testTraverseToSecondLevelFolder() throws Exception
    {
        Inventory i = buildFakeInventory();
        FolderInventoryItem f = (FolderInventoryItem) InventoryPather.traverse(i, "/d2/d2.d3");
        assertThat(f.getName(), is(equalTo("d2.d3")));
    }

    @Test
    public void testTraverseToMissingOneLevelFile()
    {
        Inventory i = buildFakeInventory();
        try
        {
            InventoryPather.traverse(i, "/f4");
        }
        catch (TraversalException ignored) {}
    }

    @Test
    public void testTraverseToMissingOneLevelFolder()
    {
        Inventory i = buildFakeInventory();
        try
        {
            InventoryPather.traverse(i, "/d4");
        }
        catch (TraversalException ignored) {}
    }

    @Test
    public void testTraverseToMissingSecondLevelFile()
    {
        Inventory i = buildFakeInventory();
        try
        {
            InventoryPather.traverse(i, "/d1/f4");
        }
        catch (TraversalException ignored) {}
    }

    @Test
    public void testTraverseToMissingSecondLevelFolder()
    {
        Inventory i = buildFakeInventory();
        try
        {
            InventoryPather.traverse(i, "/d1/d4");
        }
        catch (TraversalException ignored) {}
    }


    @Test
    public void testTraverseThroughMissingOneLevelFolder()
    {
        Inventory i = buildFakeInventory();
        try
        {
            InventoryPather.traverse(i, "/d5/d4");
        }
        catch (TraversalException ignored) {}
    }

    @Test
    public void testApplyRelativePath()
    {
        assertThat(InventoryPather.applyRelativePath("/", "one/two"), is(equalTo("/one/two")));
        assertThat(InventoryPather.applyRelativePath("/three", "one/two"), is(equalTo("/three/one/two")));

        assertThat(InventoryPather.applyRelativePath("/one", "../"), is(equalTo("/")));
        assertThat(InventoryPather.applyRelativePath("/one", "../two"), is(equalTo("/two")));
        assertThat(InventoryPather.applyRelativePath("/one/two/three", "../../lol"), is(equalTo("/one/lol")));
    }

}