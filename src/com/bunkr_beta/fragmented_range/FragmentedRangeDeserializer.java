package com.bunkr_beta.fragmented_range;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;

public class FragmentedRangeDeserializer extends JsonDeserializer<FragmentedRange>
{
    @Override
    public FragmentedRange deserialize(JsonParser jp, DeserializationContext dc) throws IOException
    {
        FragmentedRange output = new FragmentedRange();
        ArrayNode node = jp.getCodec().readTree(jp);
        for (int i = 0; i < node.size(); i+=2)
        {
            int vStart = node.get(i).intValue();
            int vLength = node.get(i+1).intValue();
            output.add(vStart, vLength);
        }
        return output;
    }
}
