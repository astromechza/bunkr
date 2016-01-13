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
    final Proxy http_proxy;
    final Proxy https_proxy;

    public URLRequestBlocker()
    {
        http_proxy = build_proxy(System.getenv("http_proxy"));
        https_proxy = build_proxy(System.getenv("https_proxy"));
    }

    public void install()
    {
        Logging.info("Installing URLStreamHandler intercept.");
        URL.setURLStreamHandlerFactory(this::getStreamHandler);
    }

    public URLStreamHandler getStreamHandler(String protocol)
    {
        if (protocol.equals("jar") || protocol.equals("file")) return null;
        if (protocol.equals("http")) return new HTTPHandler();
        if (protocol.equals("https")) return new HTTPSHandler();
        Logging.warn("Blocking request using '%s' protocol.", protocol);
        return new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException
            {
                throw new IOException(String.format("Outgoing request to %s blocked", u));
            }
        };
    }

    private Proxy build_proxy(String input)
    {
        if (input == null) return Proxy.NO_PROXY;
        URI u = URI.create(input);
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(u.getHost(), u.getPort()));
    }

    private class HTTPHandler extends URLStreamHandler
    {
        @Override
        protected int getDefaultPort()
        {
            return 80;
        }

        @Override
        protected URLConnection openConnection(URL url) throws IOException
        {
            return this.openConnection(url, http_proxy);
        }

        @Override
        protected URLConnection openConnection(URL url, Proxy proxy) throws IOException
        {
            Logging.warn("Outgoing request to %s blocked", url);
            throw new IOException(String.format("Outgoing request to %s blocked", url));
        }
    }

    private class HTTPSHandler extends URLStreamHandler
    {
        @Override
        protected int getDefaultPort()
        {
            return 80;
        }

        @Override
        protected URLConnection openConnection(URL url) throws IOException
        {
            return this.openConnection(url, https_proxy);
        }

        @Override
        protected URLConnection openConnection(URL url, Proxy proxy) throws IOException
        {
            return new URLConnection(url) {
                @Override
                public void connect() throws IOException
                {
                    Logging.warn("Outgoing request to %s blocked", url);
                    new IOException(String.format("Outgoing request to %s blocked", url));
                }
            };
        }
    }
}
