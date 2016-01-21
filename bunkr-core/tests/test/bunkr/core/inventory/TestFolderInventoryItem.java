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

import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.FolderInventoryItem;
import org.bunkr.core.inventory.FolderInventoryItemJSON;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created At: 2015-12-13
 */
public class TestFolderInventoryItem
{
    @Test
    public void testJSON() throws IOException
    {
        FileInventoryItem file1 = new FileInventoryItem("some file");

        FolderInventoryItem folder1 = new FolderInventoryItem("abc");
        FolderInventoryItem folder2 = new FolderInventoryItem("xyz");
        folder1.getFolders().add(folder2);
        folder1.addFile(file1);

        String asJson = FolderInventoryItemJSON.encode(folder1);

        FolderInventoryItem outFolder1 = FolderInventoryItemJSON.decode(asJson);

        FolderInventoryItem outFolder2 = outFolder1.getFolders().get(0);
        assertThat(outFolder2.getName(), is(equalTo(folder2.getName())));
        assertThat(outFolder2.getUuid(), is(equalTo(folder2.getUuid())));


        FileInventoryItem outFile1 = outFolder1.getFiles().get(0);
        assertThat(outFile1.getName(), is(equalTo(file1.getName())));
        assertThat(outFile1.getUuid(), is(equalTo(file1.getUuid())));
        assertThat(outFile1.getBlocks().size(), is(equalTo(0)));
        assertThat(outFile1.getSizeOnDisk(), is(equalTo(file1.getSizeOnDisk())));
        assertThat(outFile1.getActualSize(), is(equalTo(file1.getActualSize())));
        assertThat(outFile1.getEncryptionData(), is(equalTo(file1.getEncryptionData())));
    }
}
