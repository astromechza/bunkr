package org.bunkr.descriptor;

import org.bunkr.usersec.UserSecurityProvider;
import org.bunkr.exceptions.BaseBunkrException;
import org.bunkr.inventory.Inventory;
import org.json.simple.JSONObject;

/**
 * Created by benmeier on 15/10/25.
 *
 * This class contains publically readable plaintext information for how to read and decrypt the archive.
 */
public interface IDescriptor
{
    String getIdentifier();

    JSONObject getParams();

    Inventory readInventoryFromBytes(byte[] source, UserSecurityProvider usp) throws BaseBunkrException;

    byte[] writeInventoryToBytes(Inventory source, UserSecurityProvider usp) throws BaseBunkrException;

    boolean mustEncryptFiles();
}
