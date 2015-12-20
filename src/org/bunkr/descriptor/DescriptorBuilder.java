package org.bunkr.descriptor;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Creator: benmeier
 * Created At: 2015-12-20
 */
public class DescriptorBuilder
{
    public static final String KEY_NAME = "security";
    public static final String KEY_PARAMS = "params";

    public static IDescriptor fromJSON(String s)
    {
        JSONObject jo = (JSONObject) JSONValue.parse(s);
        if (!jo.containsKey(KEY_NAME)) throw new IllegalArgumentException("Missing required key: " + KEY_NAME);
        if (!jo.containsKey(KEY_PARAMS)) throw new IllegalArgumentException("Missing required key: " + KEY_NAME);
        String name = (String) jo.get(KEY_NAME);
        JSONObject params = (JSONObject) jo.get(KEY_PARAMS);

        // put security descriptors here
        if (name.equals(PlaintextDescriptor.IDENTIFIER)) return new PlaintextDescriptor();
        if (name.equals(PBKDF2Descriptor.IDENTIFIER)) return new PBKDF2Descriptor(params);

        throw new IllegalArgumentException("Could not identify SecurityDescriptor type " + name);
    }

    @SuppressWarnings("unchecked")
    public static String toJSON(IDescriptor source)
    {
        JSONObject jo = new JSONObject();
        jo.put(KEY_NAME, source.getIdentifier());
        jo.put(KEY_PARAMS, source.getParams());
        return jo.toJSONString();
    }
}
