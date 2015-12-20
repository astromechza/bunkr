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
        return out.toJSONString();
    }

    public static Descriptor decode(JSONObject input)
    {
        EncryptionDescriptor ed = null;
        if (input.get("encryption") != null) ed = EncryptionDescriptorJSON.decode((JSONObject) input.get("encryption"));
        return new Descriptor(ed);
    }

    public static Descriptor decode(String input)
    {
        return decode((JSONObject) JSONValue.parse(input));
    }
}
