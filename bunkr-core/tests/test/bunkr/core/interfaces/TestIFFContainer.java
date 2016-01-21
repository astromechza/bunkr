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

import org.bunkr.core.inventory.IFFContainer;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.FolderInventoryItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created At: 2015-12-02
 */
public class TestIFFContainer
{
    class FakeFFContainer implements IFFContainer
    {
        final List<FolderInventoryItem> folders = new ArrayList<>();
        final List<FileInventoryItem> files = new ArrayList<>();

        @Override
        public List<FolderInventoryItem> getFolders()
        {
            return folders;
        }

        @Override
        public List<FileInventoryItem> getFiles()
        {
            return files;
        }
    }


    @Test
    public void testInventoryIterator()
    {
        FakeFFContainer f1 = new FakeFFContainer();
        f1.files.add(new FileInventoryItem("thing1"));
        f1.files.add(new FileInventoryItem("thing2"));
        FolderInventoryItem f2 = new FolderInventoryItem("f2");
        FolderInventoryItem f3 = new FolderInventoryItem("f3");
        FolderInventoryItem f4 = new FolderInventoryItem("f4");
        f2.addFolder(f4);
        f1.folders.add(f2);
        f1.folders.add(f3);
        f2.addFile(new FileInventoryItem("f2.1"));
        f2.addFile(new FileInventoryItem("f2.2"));
        f3.addFile(new FileInventoryItem("f3.2"));
        f3.addFile(new FileInventoryItem("f3.1"));
        f4.addFile(new FileInventoryItem("f4.2"));
        f4.addFile(new FileInventoryItem("f4.1"));

        Iterator<FileInventoryItem> it = f1.getIterator();
        assertThat(it.next().getName(), is(equalTo("thing1")));
        assertThat(it.next().getName(), is(equalTo("thing2")));
        assertThat(it.next().getName(), is(equalTo("f2.1")));
        assertThat(it.next().getName(), is(equalTo("f2.2")));
        assertThat(it.next().getName(), is(equalTo("f3.2")));
        assertThat(it.next().getName(), is(equalTo("f3.1")));
        assertThat(it.next().getName(), is(equalTo("f4.2")));
        assertThat(it.next().getName(), is(equalTo("f4.1")));
    }
}
