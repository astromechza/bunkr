package com.bunkr_beta_tests.interfaces;

import com.bunkr_beta.inventory.IFFContainer;
import com.bunkr_beta.inventory.FileInventoryItem;
import com.bunkr_beta.inventory.FolderInventoryItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Creator: benmeier
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
        f2.getFolders().add(f4);
        f1.folders.add(f2);
        f1.folders.add(f3);
        f2.getFiles().add(new FileInventoryItem("f2.1"));
        f2.getFiles().add(new FileInventoryItem("f2.2"));
        f3.getFiles().add(new FileInventoryItem("f3.2"));
        f3.getFiles().add(new FileInventoryItem("f3.1"));
        f4.getFiles().add(new FileInventoryItem("f4.2"));
        f4.getFiles().add(new FileInventoryItem("f4.1"));

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
