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
import org.bunkr.core.inventory.Algorithms;

/**
 * Creator: benmeier
 * Created At: 2016-01-19
 */
public class FileSecWizardPanel extends VBox
{
    protected final ComboBox<Algorithms.Encryption> fileSecurityChoices = new ComboBox<>();

    private static final String DESCRIPTION_TEXT = "The file security algorithm is the algorithm used to encrypt the " +
            "actual contents of the files stored in this archive.";

    public FileSecWizardPanel()
    {
        this.setSpacing(10);
        Label descriptionLabel = new Label(DESCRIPTION_TEXT);
        descriptionLabel.setWrapText(true);
        this.getChildren().add(descriptionLabel);
        fileSecurityChoices.getItems().add(Algorithms.Encryption.AES256_CTR);
        fileSecurityChoices.getItems().add(Algorithms.Encryption.TWOFISH256_CTR);
        fileSecurityChoices.getSelectionModel().select(0);
        Label label = new Label("File Security:");
        label.setMaxHeight(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_LEFT);
        this.getChildren().add(new HBox(10, label, fileSecurityChoices));
    }

    public Algorithms.Encryption getSelectedValue()
    {
        return fileSecurityChoices.getValue();
    }
}
