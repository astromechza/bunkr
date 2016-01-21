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

import java.util.UUID;

/**
 * Created At: 2015-11-08
 */
public class InventoryItem implements Comparable<InventoryItem>
{
    private final UUID uuid;
    private String name;
    private IFFContainer parent;

    public InventoryItem(String name, UUID uuid
    )
    {
        this.name = InventoryPather.assertValidName(name);
        this.uuid = uuid;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public IFFContainer getParent()
    {
        return parent;
    }

    public void setParent(IFFContainer parent)
    {
        this.parent = parent;
    }

    public String getAbsolutePath()
    {
        if (this.parent == null) throw new RuntimeException("Orphaned Inventory Item");
        IFFContainer p = this.getParent();
        if (p instanceof Inventory)
        {
            return "/" + this.getName();
        }
        else if (p instanceof FolderInventoryItem)
        {
            return ((FolderInventoryItem) p).getAbsolutePath() + "/" + this.getName();
        }
        else
        {
            throw new RuntimeException("Bad parent type " + p.getClass().getName());
        }
    }

    @Override
    public int compareTo(InventoryItem o)
    {
        return this.getName().compareTo(o.getName());
    }
}
