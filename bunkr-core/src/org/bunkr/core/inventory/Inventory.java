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

import org.bunkr.core.inventory.Algorithms.Encryption;

import java.util.List;

/**
 * Created At: 2015-11-08
 */
public class Inventory implements IFFContainer, IFFTraversalTarget
{
    private Encryption defaultEncryption;
    private final FFContainer ffcontainer;

    public Inventory(List<FileInventoryItem> files, List<FolderInventoryItem> folders, Encryption encrypted)
    {
        this.ffcontainer = new FFContainer(files, folders);
        this.defaultEncryption = encrypted;
    }

    public List<FolderInventoryItem> getFolders()
    {
        return this.ffcontainer.getFolders();
    }

    public List<FileInventoryItem> getFiles()
    {
        return this.ffcontainer.getFiles();
    }

    @Override
    public boolean isAFolder()
    {
        return true;
    }

    @Override
    public boolean isRoot()
    {
        return true;
    }

    public Encryption getDefaultEncryption()
    {
        return defaultEncryption;
    }

    public void setDefaultEncryption(Encryption defaultEncryption)
    {
        this.defaultEncryption = defaultEncryption;
    }
}
