package org.bunkr_tests.inventory;

import org.bunkr.inventory.FileInventoryItem;
import org.bunkr.inventory.FileInventoryItemJSON;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Creator: benmeier
 * Created At: 2015-12-01
 */
public class TestFileInventoryItem
{
    @Test
    public void testJSON() throws IOException
    {
        FileInventoryItem fii = new FileInventoryItem("some file");

        String asJson = FileInventoryItemJSON.encode(fii);

        FileInventoryItem out = FileInventoryItemJSON.decode(asJson);
        assertThat(out.getName(), is(equalTo(fii.getName())));
        assertThat(out.getUuid(), is(equalTo(fii.getUuid())));
        assertThat(out.getBlocks().size(), is(equalTo(0)));
        assertThat(out.getSizeOnDisk(), is(equalTo(fii.getSizeOnDisk())));
        assertThat(out.getActualSize(), is(equalTo(fii.getActualSize())));
        assertThat(out.getEncryptionData(), is(equalTo(fii.getEncryptionData())));
        assertThat(out.getTags(), is(equalTo(fii.getTags())));
    }

    @Test
    public void testAllowedSizes()
    {
        FileInventoryItem fii = new FileInventoryItem("some file");
        fii.setActualSize(0);
        fii.setSizeOnDisk(0);

        fii.setActualSize(1000);
        fii.setSizeOnDisk(2222);

        try
        {
            fii.setActualSize(-1);
            fail("Cannot set size to negative");
        }
        catch (IllegalArgumentException ignored) {}

        try
        {
            fii.setSizeOnDisk(-1);
            fail("Cannot set size to negative");
        }
        catch (IllegalArgumentException ignored) {}
    }

    @Test
    public void testDateMod()
    {
        FileInventoryItem fii = new FileInventoryItem("some file");

        Calendar c = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        Date d = c.getTime();
        fii.setModifiedAt(d.getTime());

        assertThat(fii.getModifiedAt(), is(equalTo(d.getTime())));
        assertThat(fii.getModifiedAtDate(), is(equalTo(d)));

        try
        {
            fii.setModifiedAt(-1);
            fail("Cannot set modifiedAt to negative");
        }
        catch (IllegalArgumentException ignored) {}
    }


}

