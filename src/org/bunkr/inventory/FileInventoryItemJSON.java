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

    public static final String KEY_NAME = "name";
    public static final String KEY_UUID = "uuid";
    public static final String KEY_BLOCKS = "blocks";
    public static final String KEY_SIZE_ON_DISK = "sizeOnDisk";
    public static final String KEY_ACTUAL_SIZE = "actualSize";
    public static final String KEY_MODIFIED_AT = "modifiedAt";
    public static final String KEY_ENCRYPTION_KEY = "encryptionKey";
    public static final String KEY_ENCRYPTION_IV = "encryptionIV";
    public static final String KEY_INTEGRITY_HASH = "integrityHash";
    public static final String KEY_TAGS = "tags";

    @SuppressWarnings("unchecked")
    public static JSONAware encodeO(FileInventoryItem input)
    {
        JSONObject out = new JSONObject();

        out.put(KEY_NAME, input.getName());
        out.put(KEY_UUID, input.getUuid().toString());
        out.put(KEY_BLOCKS, FragmentedRangeJSON.encodeO(input.getBlocks()));
        out.put(KEY_SIZE_ON_DISK, input.getSizeOnDisk());
        out.put(KEY_ACTUAL_SIZE, input.getActualSize());
        out.put(KEY_MODIFIED_AT, input.getModifiedAt());

        if (input.getEncryptionKey() != null)
            out.put(KEY_ENCRYPTION_KEY, DatatypeConverter.printBase64Binary(input.getEncryptionKey()));
        else
            out.put(KEY_ENCRYPTION_KEY, null);

        if (input.getEncryptionIV() != null)
            out.put(KEY_ENCRYPTION_IV, DatatypeConverter.printBase64Binary(input.getEncryptionIV()));
        else
            out.put(KEY_ENCRYPTION_IV, null);

        if (input.getIntegrityHash() != null)
            out.put(KEY_INTEGRITY_HASH, DatatypeConverter.printBase64Binary(input.getIntegrityHash()));
        else
            out.put(KEY_INTEGRITY_HASH, null);

        out.put(KEY_TAGS, new ArrayList<>(input.getTags()));

        return out;
    }

    public static String encode(FileInventoryItem input)
    {
        return encodeO(input).toJSONString();
    }

    @SuppressWarnings("unchecked")
    public static FileInventoryItem decodeO(JSONObject input)
    {
        byte[] encK = null;
        if (input.getOrDefault(KEY_ENCRYPTION_KEY, null) != null) encK = DatatypeConverter.parseBase64Binary((String) input.get(KEY_ENCRYPTION_KEY));

        byte[] encIV = null;
        if (input.getOrDefault(KEY_ENCRYPTION_IV, null) != null) encIV = DatatypeConverter.parseBase64Binary((String) input.get(KEY_ENCRYPTION_IV));

        byte[] intH = null;
        if (input.getOrDefault(KEY_INTEGRITY_HASH, null) != null) intH = DatatypeConverter.parseBase64Binary((String) input.get(KEY_INTEGRITY_HASH));

        return new FileInventoryItem(
                (String) input.get(KEY_NAME),
                UUID.fromString((String) input.get(KEY_UUID)),
                FragmentedRangeJSON.decode((JSONArray) input.get(KEY_BLOCKS)),
                (Long) input.get(KEY_SIZE_ON_DISK),
                (Long) input.get(KEY_ACTUAL_SIZE),
                (Long) input.get(KEY_MODIFIED_AT),
                encK,
                encIV,
                intH,
                new HashSet<>((JSONArray) input.get(KEY_TAGS))
        );
    }

    public static FileInventoryItem decode(String input)
    {
        return decodeO((JSONObject) JSONValue.parse(input));
    }
}
