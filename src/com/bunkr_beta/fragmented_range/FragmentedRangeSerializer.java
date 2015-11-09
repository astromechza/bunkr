package com.bunkr_beta.fragmented_range;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class FragmentedRangeSerializer extends JsonSerializer<FragmentedRange>
{
    @Override
    public void serialize(FragmentedRange fr, JsonGenerator jg, SerializerProvider sp) throws IOException
    {
        jg.writeStartArray();
        for (int i : fr.toRepr())
        {
            jg.writeNumber(i);
        }
        jg.writeEndArray();
    }
}
