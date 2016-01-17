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

package org.bunkr.core.inventory;

import java.util.*;

/**
 * Creator: benmeier
 * Created At: 2015-11-22
 */
public interface IFFContainer
{
    List<FolderInventoryItem> getFolders();
    List<FileInventoryItem> getFiles();

    default Iterator<FileInventoryItem> getIterator()
    {
        return new InventoryIterator(this);
    }

    class InventoryIterator implements Iterator<FileInventoryItem>
    {
        final Queue<FileInventoryItem> queuedFiles = new ArrayDeque<>();
        final Queue<FolderInventoryItem> queuedFolders = new ArrayDeque<>();

        public InventoryIterator(IFFContainer inv)
        {
            queuedFolders.addAll(inv.getFolders());
            queuedFiles.addAll(inv.getFiles());
        }

        private void fillFileQueue()
        {
            while (queuedFiles.isEmpty() && ! queuedFolders.isEmpty())
            {
                FolderInventoryItem f = queuedFolders.poll();
                queuedFiles.addAll(f.getFiles());
                queuedFolders.addAll(f.getFolders());
            }
        }

        @Override
        public boolean hasNext()
        {
            this.fillFileQueue();
            return !queuedFiles.isEmpty();
        }

        @Override
        public FileInventoryItem next()
        {
            this.fillFileQueue();
            return queuedFiles.poll();
        }
    }

    default IFFTraversalTarget findFileOrFolder(String name)
    {
        IFFContainer c = findFolder(name);
        if (c != null) return (IFFTraversalTarget) c;
        return findFile(name);
    }

    default IFFContainer findFolder(String name)
    {
        for (FolderInventoryItem item : this.getFolders())
        {
            if (item.getName().equals(name))
            {
                return item;
            }
        }
        return null;
    }

    default FileInventoryItem findFile(String name)
    {
        for (FileInventoryItem item : this.getFiles())
        {
            if (item.getName().equals(name))
            {
                return item;
            }
        }
        return null;
    }

    default boolean hasFile(String name)
    {
        return (this.findFile(name) != null);
    }

    default boolean hasFolder(String name)
    {
        return (this.findFolder(name) != null);
    }

    default void addFile(FileInventoryItem item)
    {
        this.getFiles().add(item);
        item.setParent(this);
    }

    default void removeFile(FileInventoryItem item)
    {
        this.getFiles().remove(item);
        item.setParent(null);
    }

    default void addFolder(FolderInventoryItem item)
    {
        this.getFolders().add(item);
        item.setParent(this);
    }

    default void removeFolder(FolderInventoryItem item)
    {
        this.getFolders().remove(item);
        item.setParent(null);
    }
}
