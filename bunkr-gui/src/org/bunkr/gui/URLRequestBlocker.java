package org.bunkr.gui;

import com.sun.net.httpserver.HttpHandler;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Creator: benmeier
 * Created At: 2015-12-31
 */
public class URLRequestBlocker
{
    final String http_proxy;
    final String https_proxy;

    public URLRequestBlocker()
    {
        http_proxy = System.getenv("http_proxy");
        https_proxy = System.getenv("https_proxy");
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
            return this.openConnection(url, (Proxy)null);
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
            return this.openConnection(url, (Proxy)null);
        }

        @Override
        protected URLConnection openConnection(URL url, Proxy proxy) throws IOException
        {
            throw new IOException(String.format("Outgoing request to %s blocked", url));
        }
    }
}
