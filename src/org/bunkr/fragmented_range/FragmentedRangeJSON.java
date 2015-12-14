package org.bunkr.fragmented_range;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONValue;

/**
 * Creator: benmeier
 * Created At: 2015-12-13
 */
public class FragmentedRangeJSON
{
    public static String encode(FragmentedRange fr)
    {
        return encodeO(fr).toJSONString();
    }

    @SuppressWarnings("unchecked")
    public static JSONAware encodeO(FragmentedRange fr)
    {
        JSONArray ja = new JSONArray();
        for (int i : fr.toRepr())
        {
            ja.add(i);
        }
        return ja;
    }

    public static FragmentedRange decode(String s)
    {
        return decode((JSONArray) JSONValue.parse(s));
    }

    public static FragmentedRange decode(JSONArray input)
    {
        FragmentedRange f = new FragmentedRange();
        for (int i = 0; i < input.size(); i+=2)
        {
            int vStart = ((Long) input.get(i)).intValue();
            int vLength = ((Long) input.get(i + 1)).intValue();
            f.add(vStart, vLength);
        }
        return f;
    }
}
