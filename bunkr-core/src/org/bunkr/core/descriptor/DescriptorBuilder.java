package org.bunkr.core.descriptor;

import org.bunkr.core.Version;
import org.bunkr.core.utils.IO;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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

    public static IDescriptor fromFile(File path) throws IOException
    {
        try(FileInputStream fis = new FileInputStream(path))
        {
            try(DataInputStream dis = new DataInputStream(fis))
            {
                String fivebytes = IO.readNByteString(dis, 5);
                if (! fivebytes.equals("BUNKR")) throw new IOException("File format header does not match 'BUNKR'");
                Version.assertCompatible(dis.readByte(), dis.readByte(), dis.readByte(), false);
                dis.readInt();
                IO.reliableSkip(dis, dis.readLong());
                return DescriptorBuilder.fromJSON(IO.readString(dis));
            }
        }
    }
}
