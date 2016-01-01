package org.bunkr.gui;

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
        URL.setURLStreamHandlerFactory(this::getStreamHandler);
    }

    public URLStreamHandler getStreamHandler(String protocol)
    {
        if (protocol.equals("jar") || protocol.equals("file")) return null;
        if (protocol.equals("http")) return new HTTPHandler();
        if (protocol.equals("https")) return new HTTPSHandler();
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
            throw new IOException(String.format("Outgoing request to %s blocked", url));
        }
    }
}
