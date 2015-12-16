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
        if (input.hasEncryption())
            out.put("encryption", EncryptionDescriptorJSON.encodeO(input.getEncryption()));
        else
            out.put("encryption", null);
        if (input.hasCompression())
            out.put("compression", CompressionDescriptorJSON.encodeO(input.getCompression()));
        else
            out.put("compression", null);
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
