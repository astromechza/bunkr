package com.bunkr_beta;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Creator: benmeier
 * Created At: 2015-12-01
 */
public class JSONHelper
{
    public static String stringify(Object o) throws IOException
    {
        return stringify(o, new ObjectMapper());
    }

    public static String stringify(Object o, ObjectMapper m) throws IOException
    {
        StringWriter sw = new StringWriter();
        m.writeValue(sw, o);
        return sw.toString();
    }

    public static <T> T unstringify(String json, Class<T> klass) throws IOException
    {
        return unstringify(json, klass, new ObjectMapper());
    }

    public static <T> T unstringify(String json, Class<T> klass, ObjectMapper m) throws IOException
    {
        return m.readValue(json, klass);
    }
}
