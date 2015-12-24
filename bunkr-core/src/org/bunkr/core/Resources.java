package org.bunkr.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Creator: benmeier
 * Created At: 2015-12-24
 */
public class Resources
{
    private static Resources instance = null;
    public static Resources get()
    {
        if (instance == null) instance = new Resources();
        return instance;
    }

    private String getExternalPathInner(String resourcePath) throws IOException
    {
        URL u = getClass().getResource(resourcePath);
        if (u == null) throw new IOException("Could not load resource " + resourcePath);
        return u.toExternalForm();
    }

    private InputStream getStreamInner(String resourcePath) throws IOException
    {
        InputStream s = getClass().getResourceAsStream(resourcePath);
        if (s == null) throw new IOException("Could not load resource " + resourcePath);
        return s;
    }

    public static String getExternalPath(String resourcePath) throws IOException
    {
        return get().getExternalPathInner(resourcePath);
    }

    public static InputStream getStream(String resourcePath) throws IOException
    {
        return get().getStreamInner(resourcePath);
    }
}
