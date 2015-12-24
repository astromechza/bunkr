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
 * Creator: benmeier
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
        folder1.getFiles().add(file1);

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
        assertThat(outFile1.getTags(), is(equalTo(file1.getTags())));
    }
}
