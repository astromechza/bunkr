package org.bunkr.gui.components;

import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import org.bunkr.core.Resources;
import org.bunkr.core.inventory.InventoryPather;
import org.bunkr.gui.dialogs.QuickDialogs;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.*;
import org.w3c.dom.events.Event;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Creator: benmeier
 * Created At: 2015-12-31
 */
public class MarkdownWebViewAdapter
{
    private static final Set<String> ALLOWED_OUTBOUND_SCHEMA = new HashSet<>(Arrays.asList(
            "http", "https", "ftp", ""
    ));

    private static final String githubCSSPath = "/resources/css/github_css.css";

    public void adapt(WebView subject) throws IOException
    {
        // load external style sheet into webview
        subject.getEngine().setUserStyleSheetLocation(Resources.getExternalPath(githubCSSPath));

        // disable existing context menu
        subject.setContextMenuEnabled(false);

        // disable javascript
        subject.getEngine().setJavaScriptEnabled(false);

        // need to bind the page load event in order to process link things
        subject.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            // do this when the page load has finished
            if (newValue == Worker.State.SUCCEEDED)
            {
                this.adaptLoadedPage(subject);
            }
        });

        subject.getEngine().setOnError(event -> {
            QuickDialogs.exception(event.getException());
        });

        subject.getEngine().getLoadWorker().exceptionProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue.getMessage());
        });
    }

    private void adaptLoadedPage(WebView subject)
    {
        // create a new event listener for link elements
        EventListener listener = this::handleLinkOnClick;

        // get the document
        Document doc = subject.getEngine().getDocument();
        // loop through all link tags
        NodeList nodeList = doc.getElementsByTagName("a");
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Element element = (Element) nodeList.item(i);
            // remove on click
            element.setAttribute("onclick", "");
            ((EventTarget) nodeList.item(i)).addEventListener("click", listener, false);
        }
    }

    private void handleLinkOnClick(Event ev)
    {
        // get the element that was clicked
        Element element = (Element) ev.getTarget();

        // if the location target has an href
        if (element.hasAttribute("href") && (! element.getAttribute("href").startsWith("#")))
        {
            ev.preventDefault();
            try
            {
                URI uri = new URI(element.getAttribute("href"));
                if (uri.getScheme() == null)
                {
                    if (InventoryPather.isValidPath(uri.toString()))
                    {
                        // TODO fill these references
//                            if (this.sourceFileStore.fileExists(uri.toString()))
//                            {
//                                this.parentWindow.requestOpenFile(uri.toString());
//                            }
                    }
                }
                else if (ALLOWED_OUTBOUND_SCHEMA.contains(uri.getScheme().toLowerCase()))
                {
                    if(QuickDialogs.confirm("Outgoing link to '%s'. Do you want to open it in a browser?", uri))
                    {
                        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE))
                        {
                            try
                            {
                                desktop.browse(uri);
                            }
                            catch (Exception e)
                            {
                                QuickDialogs.error("Failed to open external browser.");
                            }
                        }
                    }
                }
                else
                {
                    QuickDialogs.error("Error", null, "Outgoing link '%s' is using an unknown scheme %s", uri, uri.getScheme());
                }
            }
            catch (URISyntaxException e)
            {
                QuickDialogs.error("Error", null, "Outgoing link %s was not a valid uri", element.getAttribute("href"));
            }
        }
    }


}
