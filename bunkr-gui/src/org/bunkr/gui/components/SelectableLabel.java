package org.bunkr.gui.components;

import javafx.scene.control.TextField;

/**
 * Creator: benmeier
 * Created At: 2015-12-29
 */
public class SelectableLabel extends TextField
{
    public SelectableLabel(String text)
    {
        super(text);
        this.setEditable(false);
        this.setFocusTraversable(false);
        this.setStyle("-fx-background-color: transparent; -fx-background-insets: 0px; -fx-background-radius: 0; -fx-padding: 0;");
    }
}
