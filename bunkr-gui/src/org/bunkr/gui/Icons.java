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

import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Creator: benmeier
 * Created At: 2016-01-08
 */
public class Icons
{
    public static final String ICON_SETTINGS = "\uf013";
    public static final String ICON_RELOAD = "\uf021";
    public static final String ICON_EDIT = "\uf044";
    public static final String ICON_VIEW = "\uf06e";
    public static final String ICON_ELLIPSIS = "\uf141";

    public static final String ICON_MAGNIFY = "\uf002";
    public static final String ICON_MAGNIFY_PLUS = "\uf00e";
    public static final String ICON_MAGNIFY_MINUS = "\uf010";

    public static final String ICON_TICK = "\uf00c";
    public static final String ICON_CROSS = "\uf00d";

    public static final String ICON_PLUS = "\uf067";
    public static final String ICON_MINUS = "\uf068";

    public static final String ICON_OPEN = "\uf07c";
    public static final String ICON_NEW = "\uf187";
    public static final String ICON_SAVE = "\uf0c7";

    public static final String ICON_FOLDER = "\uf114";
    public static final String ICON_FILE = "\uf016";
    public static final String ICON_FILE_IMAGE = "\uf1c5";
    public static final String ICON_FILE_TEXT = "\uf0f6";

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
