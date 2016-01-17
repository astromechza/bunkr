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

package org.bunkr.gui;

import org.bunkr.core.utils.Logging;

import java.io.IOException;
import java.net.*;

/**
 * Creator: benmeier
 * Created At: 2015-12-31
 */
public class URLRequestBlocker
{
    private static URLRequestBlocker instance = null;
    public static URLRequestBlocker instance()
    {
        if (instance == null) instance = new URLRequestBlocker();
        return instance;
    }

    public void install()
    {
        Logging.info("Installing URLStreamHandler intercept.");
        URL.setURLStreamHandlerFactory(this::getStreamHandler);
    }

    public URLStreamHandler getStreamHandler(String protocol)
    {
        if (protocol.equals("jar") || protocol.equals("file")) return null;
        return new URLStreamHandler()
        {
            @Override
            protected URLConnection openConnection(URL u) throws IOException
            {
                throw new ConnectException(String.format("Outgoing request to %s blocked.", u));
            }
        };
    }
}
