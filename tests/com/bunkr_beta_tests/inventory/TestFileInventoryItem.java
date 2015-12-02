package com.bunkr_beta_tests.inventory;

import com.bunkr_beta.JSONHelper;
import com.bunkr_beta.inventory.FileInventoryItem;
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
    public void testDefault() throws IOException
    {
        FileInventoryItem fii = new FileInventoryItem("some file");

        String asJson = JSONHelper.stringify(fii);

        // this is a little silly, but illustrates the point that a lot of this is automatic
        // one could try mock these various random/time sources, but that would be a heacache.
        asJson = asJson.replace(fii.getUuid().toString(), "01234567-89ab-cdef-0123-456789abcdef");
        asJson = asJson.replace(Base64.getEncoder().encodeToString(fii.getEncryptionKey()), "xxxEncryptionKeyxxx");
        asJson = asJson.replace(Base64.getEncoder().encodeToString(fii.getEncryptionIV()), "xxxEncryptionIVxxx");
        asJson = asJson.replace("" + fii.getModifiedAt(), "000000");

        assertThat(asJson, is(equalTo(
                "{\"name\":\"some file\"," +
                 "\"uuid\":\"01234567-89ab-cdef-0123-456789abcdef\"," +
                 "\"blocks\":[]," +
                 "\"sizeOnDisk\":0," +
                 "\"actualSize\":0," +
                 "\"modifiedAt\":000000," +
                 "\"encryptionKey\":\"xxxEncryptionKeyxxx\"," +
                 "\"encryptionIV\":\"xxxEncryptionIVxxx\"," +
                 "\"tags\":[]" +
                "}")));
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

