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

package test.bunkr.core.inventory;

import org.bunkr.core.exceptions.TraversalException;
import org.bunkr.core.inventory.*;
import org.bunkr.core.inventory.Algorithms.Encryption;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
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
        assertTrue(InventoryPather.isValidPath("/something[]"));
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
        Inventory i = new Inventory(
                new ArrayList<>(),
                new ArrayList<>(),
                Encryption.NONE
        );
        FolderInventoryItem d1 = new FolderInventoryItem("d1");
        i.addFolder(d1);
        FolderInventoryItem d2 = new FolderInventoryItem("d2");
        i.addFolder(d2);
        FolderInventoryItem d3 = new FolderInventoryItem("d2.d3");
        d2.addFolder(d3);

        FileInventoryItem f1 = new FileInventoryItem("f1");
        i.addFile(f1);
        FileInventoryItem f2 = new FileInventoryItem("f2");
        i.addFile(f2);
        FileInventoryItem f3 = new FileInventoryItem("d2.f3");
        d2.addFile(f3);

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
