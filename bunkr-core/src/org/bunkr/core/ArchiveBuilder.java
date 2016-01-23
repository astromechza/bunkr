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

package org.bunkr.core;

import org.bunkr.core.descriptor.IDescriptor;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.inventory.Algorithms.Encryption;
import org.bunkr.core.inventory.Inventory;
import org.bunkr.core.usersec.UserSecurityProvider;

import java.io.*;
import java.util.ArrayList;

/**
 * Created At: 2015-11-08
 */
public class ArchiveBuilder
{
    public static final byte[] FORMAT_SIG = "BUNKR".getBytes();
    public static final int DEFAULT_BLOCK_SIZE = 1024;

    public static ArchiveInfoContext createNewEmptyArchive(File path, IDescriptor descriptor, UserSecurityProvider uic)
            throws IOException, BaseBunkrException
    {
        Inventory blankInventory = new Inventory(
                new ArrayList<>(),
                new ArrayList<>(),
                Encryption.NONE
        );

        try(FileOutputStream fos = new FileOutputStream(path))
        {
            try(DataOutputStream dos = new DataOutputStream(fos))
            {
                dos.write(FORMAT_SIG);
                dos.write(Version.versionMajor);
                dos.write(Version.versionMinor);
                dos.write(Version.versionBugfix);
                dos.writeInt(DEFAULT_BLOCK_SIZE);
                dos.writeLong(0);
            }
        }
        MetadataWriter.write(path, blankInventory, descriptor, uic, DEFAULT_BLOCK_SIZE);
        return new ArchiveInfoContext(path, uic);
    }
}
