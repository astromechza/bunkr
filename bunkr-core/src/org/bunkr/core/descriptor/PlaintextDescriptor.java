package org.bunkr.core.descriptor;

import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.inventory.Inventory;
import org.bunkr.core.inventory.InventoryJSON;
import org.json.simple.JSONObject;

/**
 * Creator: benmeier
 * Created At: 2015-12-20
 */
public class PlaintextDescriptor implements IDescriptor
{
    public static final String IDENTIFIER = "plaintext";

    public PlaintextDescriptor()
    {

    }

    @Override
    public String getIdentifier()
    {
        return IDENTIFIER;
    }

    @Override
    public JSONObject getParams()
    {
        return new JSONObject();
    }

    @Override
    public Inventory readInventoryFromBytes(byte[] source, UserSecurityProvider usp)
    {
        return InventoryJSON.decode(new String(source));
    }

    @Override
    public byte[] writeInventoryToBytes(Inventory source, UserSecurityProvider usp)
    {
        return InventoryJSON.encode(source).getBytes();
    }
}
