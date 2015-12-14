package org.bunkr.inventory;

import org.bunkr.fragmented_range.FragmentedRangeJSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-12-13
 */
public class FileInventoryItemJSON
{
    @SuppressWarnings("unchecked")
    public static JSONAware encodeO(FileInventoryItem input)
    {
        JSONObject out = new JSONObject();

        out.put("name", input.getName());
        out.put("uuid", input.getUuid().toString());

        out.put("blocks", FragmentedRangeJSON.encodeO(input.getBlocks()));
        out.put("sizeOnDisk", input.getSizeOnDisk());
        out.put("actualSize", input.getActualSize());
        out.put("modifiedAt", input.getModifiedAt());
        out.put("encryptionKey", DatatypeConverter.printBase64Binary(input.getEncryptionKey()));
        out.put("encryptionIV", DatatypeConverter.printBase64Binary(input.getEncryptionIV()));
        out.put("tags", new ArrayList<>(input.getTags()));

        return out;
    }

    public static String encode(FileInventoryItem input)
    {
        return encodeO(input).toJSONString();
    }

    @SuppressWarnings("unchecked")
    public static FileInventoryItem decodeO(JSONObject input)
    {
        return new FileInventoryItem(
                (String) input.get("name"),
                UUID.fromString((String) input.get("uuid")),
                FragmentedRangeJSON.decode((JSONArray) input.get("blocks")),
                (Long) input.get("sizeOnDisk"),
                (Long) input.get("actualSize"),
                (Long) input.get("modifiedAt"),
                DatatypeConverter.parseBase64Binary((String) input.get("encryptionKey")),
                DatatypeConverter.parseBase64Binary((String) input.get("encryptionIV")),
                new HashSet<>((JSONArray) input.get("tags"))
        );
    }

    public static FileInventoryItem decode(String input)
    {
        return decodeO((JSONObject) JSONValue.parse(input));
    }
}
