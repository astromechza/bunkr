/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.core.inventory;

import org.bunkr.core.fragmented_range.FragmentedRangeJSON;
import org.bunkr.core.utils.Logging;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.xml.bind.DatatypeConverter;
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
    public static final String KEY_ENCRYPTION_ALGORITHM = "encryptionAlgorithm";
    public static final String KEY_INTEGRITY_HASH = "integrityHash";
    public static final String KEY_MEDIA_TYPE = "mediaType";

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
        out.put(KEY_ENCRYPTION_ALGORITHM, input.getEncryptionAlgorithm().toString());

        if (input.getEncryptionData() != null)
            out.put(KEY_ENCRYPTION_DATA, DatatypeConverter.printBase64Binary(input.getEncryptionData()));
        else
            out.put(KEY_ENCRYPTION_DATA, null);

        if (input.getIntegrityHash() != null)
            out.put(KEY_INTEGRITY_HASH, DatatypeConverter.printBase64Binary(input.getIntegrityHash()));
        else
            out.put(KEY_INTEGRITY_HASH, null);

        out.put(KEY_MEDIA_TYPE, input.getMediaType());

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

        Algorithms.Encryption encA = Algorithms.Encryption.NONE;
        if (input.getOrDefault(KEY_ENCRYPTION_ALGORITHM, null) != null) encA = Algorithms.Encryption.valueOf((String) input.get(KEY_ENCRYPTION_ALGORITHM));

        String mt = MediaType.UNKNOWN;
        if (input.getOrDefault(KEY_MEDIA_TYPE, null) != null)
        {
            mt = (String) input.get(KEY_MEDIA_TYPE);
            if (! MediaType.ALL_TYPES.contains(mt))
            {
                Logging.warn("File %s has unsupported media type %s. Converting to %s", input.get(KEY_NAME), mt, MediaType.UNKNOWN);
                mt = MediaType.UNKNOWN;
            }
        }

        return new FileInventoryItem(
                (String) input.get(KEY_NAME),
                UUID.fromString((String) input.get(KEY_UUID)),
                FragmentedRangeJSON.decode((JSONArray) input.get(KEY_BLOCKS)),
                (Long) input.get(KEY_SIZE_ON_DISK),
                (Long) input.get(KEY_ACTUAL_SIZE),
                (Long) input.get(KEY_MODIFIED_AT),
                encD,
                encA,
                intH,
                mt
        );
    }

    public static FileInventoryItem decode(String input)
    {
        return decodeO((JSONObject) JSONValue.parse(input));
    }
}
