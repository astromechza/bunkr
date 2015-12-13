package com.bunkr_beta.descriptor;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.xml.bind.DatatypeConverter;

/**
 * Creator: benmeier
 * Created At: 2015-12-13
 */
public class EncryptionDescriptorJSON
{
    public static String encode(EncryptionDescriptor input)
    {
        return encodeO(input).toJSONString();
    }

    @SuppressWarnings("unchecked")
    public static JSONAware encodeO(EncryptionDescriptor input)
    {
        JSONObject out = new JSONObject();
        out.put("aesKeyLength", input.aesKeyLength);
        out.put("pbkdf2Iterations", input.pbkdf2Iterations);
        out.put("pbkdf2Salt", DatatypeConverter.printBase64Binary(input.pbkdf2Salt));
        return out;
    }

    public static EncryptionDescriptor decode(JSONObject input)
    {
        return new EncryptionDescriptor(
                ((Long) input.get("pbkdf2Iterations")).intValue(),
                ((Long) input.get("aesKeyLength")).intValue(),
                DatatypeConverter.parseBase64Binary((String) input.get("pbkdf2Salt"))
        );
    }

    public static EncryptionDescriptor decode(String input)
    {
        return decode((JSONObject) JSONValue.parse(input));
    }
}
