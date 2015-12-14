package org.bunkr.inventory;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.List;

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
        return out;
    }

    public static String encode(Inventory input)
    {
        return encodeO(input).toJSONString();
    }

    @SuppressWarnings("unchecked")
    public static Inventory decodeO(JSONObject input)
    {
        List<FileInventoryItem> files = new ArrayList<>();
        List<FolderInventoryItem> folders = new ArrayList<>();

        for (Object item : (JSONArray) input.get("files"))
        {
            files.add(FileInventoryItemJSON.decodeO((JSONObject) item));
        }

        for (Object item : (JSONArray) input.get("folders"))
        {
            folders.add(FolderInventoryItemJSON.decodeO((JSONObject) item));
        }

        return new Inventory(files, folders);
    }

    public static Inventory decode(String input)
    {
        return decodeO((JSONObject) JSONValue.parse(input));
    }

}
