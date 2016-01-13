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

package org.bunkr.core.fragmented_range;

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
