package org.bunkr.gui.components.tabs.html;

import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import org.bunkr.core.utils.Logging;
import org.bunkr.gui.dialogs.QuickDialogs;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Creator: benmeier
 * Created At: 2016-01-16
 */
public class HtmlWebViewAdapter
{
    private static final Set<String> ALLOWED_OUTBOUND_SCHEMA = new HashSet<>(Arrays.asList(
            "http", "https", "ftp", ""
    ));

    public void adapt(WebView subject) throws IOException
    {
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

        subject.getEngine().setOnError(event -> QuickDialogs.exception(event.getException()));
    }

    private void adaptLoadedPage(WebView subject)
    {
        // get the document
        Document doc = subject.getEngine().getDocument();

        // loop through all link tags
        NodeList linkNodes = doc.getElementsByTagName("a");
        for (int i = 0; i < linkNodes.getLength(); i++)
        {
            Element element = (Element) linkNodes.item(i);
            // remove on click
            element.setAttribute("onclick", "");
            ((EventTarget) element).addEventListener("click", evt -> this.handleElementLinkAttributeClick(evt, "href"), false);
        }

        // loop through all link tags
        NodeList imgNodes = doc.getElementsByTagName("img");
        for (int i = 0; i < imgNodes.getLength(); i++)
        {
            Element element = (Element) imgNodes.item(i);

            String existing_css = element.hasAttribute("style") ? element.getAttribute("style") : "";
            String new_css = existing_css + "; cursor: pointer;";
            element.setAttribute("style", new_css);
            element.setAttribute("alt", String.format("Open Image: %s", element.getAttribute("src")));
            element.setAttribute("title", String.format("Open Image: %s", element.getAttribute("src")));

            ((EventTarget) element).addEventListener("click", evt -> this.handleElementLinkAttributeClick(evt, "src"), false);
        }
    }

    private void handleElementLinkAttributeClick(Event event, String attribute)
    {
        // get the element that was clicked
        Element element = (Element) event.getTarget();

        // if the location target has an href
        if (element.hasAttribute(attribute) && ! element.getAttribute(attribute).startsWith("#"))
        {
            event.preventDefault();
            try
            {
                URI uri = new URI(element.getAttribute(attribute));
                if (uri.getScheme() != null && ALLOWED_OUTBOUND_SCHEMA.contains(uri.getScheme().toLowerCase()))
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
                    Logging.info("Outgoing link '%s' is not routable.", uri);
                }
            }
            catch (URISyntaxException e)
            {
                QuickDialogs.error("Error", null, "Outgoing link %s was not a valid uri", element.getAttribute(attribute));
            }
        }
    }
}
