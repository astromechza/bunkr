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

import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.inventory.Algorithms.Encryption;

import java.util.Date;
import java.util.UUID;

/**
 * Created At: 2015-11-08
 */
public class FileInventoryItem extends InventoryItem implements IFFTraversalTarget
{
    private long sizeOnDisk;
    private long modifiedAt;
    private byte[] encryptionData;
    private Encryption encryptionAlgorithm;
    private byte[] integrityHash;
    private FragmentedRange blocks;
    private long actualSize;
    private String mediaType;

    public FileInventoryItem(
            String name,
            UUID uuid,
            FragmentedRange blocks,
            long sizeOnDisk,
            long actualSize,
            long modifiedAt,
            byte[] encryptionData,
            Encryption encryptionAlgorithm,
            byte[] integrityHash,
            String mediaType
    )
    {
        super(name, uuid);
        this.encryptionData = encryptionData;
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.integrityHash = integrityHash;
        this.sizeOnDisk = sizeOnDisk;
        this.actualSize = actualSize;
        this.modifiedAt = modifiedAt;
        this.blocks = blocks;
        this.mediaType = mediaType;
    }

    public FileInventoryItem(String name)
    {
        super(name, UUID.randomUUID());
        this.sizeOnDisk = 0;
        this.blocks = new FragmentedRange();
        this.modifiedAt = System.currentTimeMillis();
        this.encryptionData = null;
        this.encryptionAlgorithm = Encryption.NONE;
        this.integrityHash = null;
        this.mediaType = MediaType.UNKNOWN;
    }

    public FragmentedRange getBlocks()
    {
        return blocks;
    }

    public void setBlocks(FragmentedRange blocks)
    {
        this.blocks = blocks;
    }

    public byte[] getEncryptionData()
    {
        return encryptionData;
    }

    public void setEncryptionData(byte[] encryptionData)
    {
        this.encryptionData = encryptionData;
    }

    public Encryption getEncryptionAlgorithm()
    {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(Encryption encryptionAlgorithm)
    {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public byte[] getIntegrityHash()
    {
        return integrityHash;
    }

    public void setIntegrityHash(byte[] integrityHash)
    {
        this.integrityHash = integrityHash;
    }

    public long getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(long modifiedAt)
    {
        if (modifiedAt < 0) throw new IllegalArgumentException("Cannot set modifiedAt < 0");
        this.modifiedAt = modifiedAt;
    }

    public Date getModifiedAtDate()
    {
        return new Date(this.getModifiedAt());
    }

    public long getSizeOnDisk()
    {
        return sizeOnDisk;
    }

    public void setSizeOnDisk(long sizeOnDisk)
    {
        if (sizeOnDisk < 0) throw new IllegalArgumentException("Cannot set sizeOnDisk < 0");
        this.sizeOnDisk = sizeOnDisk;
    }

    public void setActualSize(long actualSize)
    {
        if (actualSize < 0) throw new IllegalArgumentException("Cannot set actualSize < 0");
        this.actualSize = actualSize;
    }

    public long getActualSize()
    {
        return this.actualSize;
    }

    public String getMediaType()
    {
        return mediaType;
    }

    public void setMediaType(String mediaType)
    {
        this.mediaType = mediaType;
    }

    @Override
    public boolean isAFile()
    {
        return true;
    }
}
