package org.bunkr.core.inventory;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;

/**
 * Creator: benmeier
 * Created At: 2015-12-13
 */
public class InventoryJSON
{
    @SuppressWarnings("unchecked")
    public static JSONAware encodeO(Inventory input)
    {
        JSONObject out = new JSONObject();
        JSONArray files = new JSONArray();
        for (FileInventoryItem item : input.getFiles())
        {
            files.add(FileInventoryItemJSON.encodeO(item));
        }
        out.put("files", files);

        JSONArray folders = new JSONArray();
        for (FolderInventoryItem item : input.getFolders())
        {
            folders.add(FolderInventoryItemJSON.encodeO(item));
        }
        out.put("folders", folders);
        out.put("defaultEncryption", input.getDefaultEncryption().toString());
        return out;
    }

    public static String encode(Inventory input)
    {
        return encodeO(input).toJSONString();
    }

    @SuppressWarnings("unchecked")
    public static Inventory decodeO(JSONObject input)
    {
        Inventory outputInv = new Inventory(
                new ArrayList<>(),
                new ArrayList<>(),
                Algorithms.Encryption.valueOf((String) input.get("defaultEncryption"))
        );

        for (Object item : (JSONArray) input.get("files"))
        {
            outputInv.addFile(FileInventoryItemJSON.decodeO((JSONObject) item));
        }

        for (Object item : (JSONArray) input.get("folders"))
        {
            outputInv.addFolder(FolderInventoryItemJSON.decodeO((JSONObject) item));
        }

        return outputInv;
    }

    public static Inventory decode(String input)
    {
        return decodeO((JSONObject) JSONValue.parse(input));
    }

}
