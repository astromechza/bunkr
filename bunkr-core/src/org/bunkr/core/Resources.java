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
