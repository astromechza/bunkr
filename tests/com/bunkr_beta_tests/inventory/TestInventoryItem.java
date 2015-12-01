package com.bunkr_beta_tests.inventory;

import com.bunkr_beta.JSONHelper;
import com.bunkr_beta.inventory.InventoryItem;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Creator: benmeier
 * Created At: 2015-12-01
 */
public class TestInventoryItem
{
    @Test
    public void testBasic() throws IOException
    {
        UUID u = UUID.fromString("01234567-89ab-cdef-0123-456789abcdef");
        InventoryItem i = new InventoryItem("some name", u);

        assertThat(i.getName(), is(equalTo("some name")));
        assertThat(i.getUuid(), is(equalTo(u)));

        String asJson = JSONHelper.stringify(i);
        assertThat(asJson, is(equalTo("{\"name\":\"some name\",\"uuid\":\"01234567-89ab-cdef-0123-456789abcdef\"}")));

        InventoryItem ia = JSONHelper.unstringify(asJson, InventoryItem.class);

        assertThat(ia.getUuid(), is(equalTo(u)));
        assertThat(ia.getName(), is(equalTo("some name")));

        ia.setName("new name");

        assertThat(ia.getName(), is(equalTo("new name")));
        assertThat(i.getName(), is(equalTo("some name")));

        assertThat(JSONHelper.stringify(ia), is(equalTo("{\"name\":\"new name\",\"uuid\":\"01234567-89ab-cdef-0123-456789abcdef\"}")));
    }
}
