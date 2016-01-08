package org.bunkr.gui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Creator: benmeier
 * Created At: 2016-01-08
 */
public class Icons
{
    public static final String ICON_SETTINGS = "\uf013";
    public static final String ICON_MAGNIFY = "\uf002";
    public static final String ICON_MAGNIFY_PLUS = "\uf00e";
    public static final String ICON_MAGNIFY_MINUS = "\uf010";
    public static final String ICON_TICK = "\uf00c";
    public static final String ICON_CROSS = "\uf00d";
    public static final String ICON_SAVE = "\uf0c7";
    public static final String ICON_RELOAD = "\uf021";
    public static final String ICON_EDIT = "\uf044";
    public static final String ICON_VIEW = "\uf06e";
    public static final String ICON_PLUS = "\uf067";
    public static final String ICON_MINUS = "\uf068";
    public static final String ICON_ELLIPSIS = "\uf141";
    public static final String ICON_OPEN = "\uf07c";
    public static final String ICON_NEW = "\uf187";

    public static Button buildIconButton(String text, String icon, int size)
    {
        Button b = new Button(text);
        b.setGraphic(buildIconLabel(icon, size));
        return b;
    }

    public static Button buildIconButton(String text, String icon)
    {
        Button b = new Button(text);
        b.setGraphic(buildIconLabel(icon));
        return b;
    }

    public static Label buildIconLabel(String icon, int size)
    {
        Label l = new Label(icon);
        l.getStyleClass().add("icons");
        l.setStyle(String.format("-fx-font-size: %dpx;", size));
        return l;
    }

    public static Label buildIconLabel(String icon)
    {
        Label l = new Label(icon);
        l.getStyleClass().add("icons");
        return l;
    }
}
