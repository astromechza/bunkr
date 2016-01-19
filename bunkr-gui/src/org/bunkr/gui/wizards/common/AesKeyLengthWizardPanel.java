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

package org.bunkr.gui.wizards.common;

import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Creator: benmeier
 * Created At: 2016-01-19
 */
public class AesKeyLengthWizardPanel extends VBox
{
    public final ComboBox<Integer> keyLengths = new ComboBox<>();

    private static final String DESCRIPTION_TEXT = "The AES Strength used for the symmetric encryption that will " +
            "protect the archive inventory. At this moment in time, only 256 bit AES is allowed.";

    public AesKeyLengthWizardPanel()
    {
        this.setSpacing(10);
        Label description = new Label(DESCRIPTION_TEXT);
        description.setWrapText(true);
        this.getChildren().add(description);
        keyLengths.getItems().add(256);
        keyLengths.getSelectionModel().select(0);
        Label label = new Label("AES Key Bits:");
        label.setMaxHeight(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_LEFT);
        this.getChildren().add(new HBox(10, label, keyLengths));
    }

    public Integer getSelectedKeyLength()
    {
        return keyLengths.getSelectionModel().getSelectedItem();
    }
}
