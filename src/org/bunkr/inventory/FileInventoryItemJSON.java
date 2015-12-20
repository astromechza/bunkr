package org.bunkr.inventory;

import org.bunkr.fragmented_range.FragmentedRangeJSON;
import org.bunkr.streams.AlgorithmIdentifier;
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
    public static final String KEY_ENCRYPTION_DATA = "encryptionData";
    public static final String KEY_ALGORITHMS = "algorithms";
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

        if (input.getEncryptionData() != null)
            out.put(KEY_ENCRYPTION_DATA, DatatypeConverter.printBase64Binary(input.getEncryptionData()));
        else
            out.put(KEY_ENCRYPTION_DATA, null);

        if (input.getIntegrityHash() != null)
            out.put(KEY_INTEGRITY_HASH, DatatypeConverter.printBase64Binary(input.getIntegrityHash()));
        else
            out.put(KEY_INTEGRITY_HASH, null);

        if (input.getAlgorithms() != null)
            out.put(KEY_ALGORITHMS, input.getAlgorithms().toString());
        else
            out.put(KEY_ALGORITHMS, null);

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
        byte[] encD = null;
        if (input.getOrDefault(KEY_ENCRYPTION_DATA, null) != null) encD = DatatypeConverter.parseBase64Binary((String) input.get(KEY_ENCRYPTION_DATA));

        byte[] intH = null;
        if (input.getOrDefault(KEY_INTEGRITY_HASH, null) != null) intH = DatatypeConverter.parseBase64Binary((String) input.get(KEY_INTEGRITY_HASH));

        AlgorithmIdentifier aid = null;
        if (input.getOrDefault(KEY_ALGORITHMS, null) != null) aid = new AlgorithmIdentifier((String) input.get(
                KEY_ALGORITHMS));

        return new FileInventoryItem(
                (String) input.get(KEY_NAME),
                UUID.fromString((String) input.get(KEY_UUID)),
                FragmentedRangeJSON.decode((JSONArray) input.get(KEY_BLOCKS)),
                (Long) input.get(KEY_SIZE_ON_DISK),
                (Long) input.get(KEY_ACTUAL_SIZE),
                (Long) input.get(KEY_MODIFIED_AT),
                encD,
                aid,
                intH,
                new HashSet<>((JSONArray) input.get(KEY_TAGS))
        );
    }

    public static FileInventoryItem decode(String input)
    {
        return decodeO((JSONObject) JSONValue.parse(input));
    }
}
