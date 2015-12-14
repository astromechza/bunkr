package org.bunkr.descriptor;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Creator: benmeier
 * Created At: 2015-12-13
 */
public class DescriptorJSON
{
    @SuppressWarnings("unchecked")
    public static String encode(Descriptor input)
    {
        JSONObject out = new JSONObject();
        if (input.getEncryption() == null)
            out.put("encryption", null);
        else
            out.put("encryption", EncryptionDescriptorJSON.encodeO(input.getEncryption()));
        if (input.getCompression() == null)
            out.put("compression", null);
        else
            out.put("compression", CompressionDescriptorJSON.encodeO(input.getCompression()));
        return out.toJSONString();
    }

    public static Descriptor decode(JSONObject input)
    {
        EncryptionDescriptor ed = null;
        CompressionDescriptor cd = null;
        if (input.get("encryption") != null) ed = EncryptionDescriptorJSON.decode((JSONObject) input.get("encryption"));
        if (input.get("compression") != null) cd = CompressionDescriptorJSON.decode((JSONObject) input.get("compression"));
        return new Descriptor(ed, cd);
    }

    public static Descriptor decode(String input)
    {
        return decode((JSONObject) JSONValue.parse(input));
    }
}
