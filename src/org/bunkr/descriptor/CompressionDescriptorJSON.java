package org.bunkr.descriptor;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Creator: benmeier
 * Created At: 2015-12-13
 */
public class CompressionDescriptorJSON
{
    @SuppressWarnings("unchecked")
    public static String encode(CompressionDescriptor input)
    {
        return encodeO(input).toJSONString();
    }

    @SuppressWarnings("unchecked")
    public static JSONAware encodeO(CompressionDescriptor input)
    {
        JSONObject out = new JSONObject();
        out.put("algorithm", input.algorithm);
        return out;
    }

    public static CompressionDescriptor decode(JSONObject input)
    {
        return new CompressionDescriptor((String) input.get("algorithm"));
    }

    public static CompressionDescriptor decode(String input)
    {
        return decode((JSONObject) JSONValue.parse(input));
    }
}
