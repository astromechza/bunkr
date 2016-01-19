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

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import org.bunkr.core.exceptions.IllegalPasswordException;
import org.bunkr.core.usersec.PasswordRequirements;

/**
 * Creator: benmeier
 * Created At: 2016-01-19
 */
public class PasswordWizardPanel extends VBox
{
    private static final String PW_NOTE_DEFAULT = "Please enter a password";
    private static final String PW_NOTE_CONFIRM = "Please confirm password";
    private static final String PW_NOTE_MATCH = "Confirmation matches password";
    private static final String PW_NOTE_NO_MATCH = "Confirmation does not match password";
    private static final String PW_NOTE_CLASS_OK = "pw-note-success";
    private static final String PW_NOTE_CLASS_NOT_OK = "pw-note-failure";

    private static final String DESCRIPTION_TEXT = "Pick a new password to protect this archive.";

    protected final PasswordField passwordBox = new PasswordField();
    protected final PasswordField passwordConfirmBox = new PasswordField();
    private final Label passwordNote = new Label("");

    public PasswordWizardPanel()
    {
        this.passwordBox.setPromptText("Enter a password");
        this.passwordBox.setMaxWidth(Double.MAX_VALUE);
        this.passwordConfirmBox.setPromptText("Enter the password again");
        this.passwordConfirmBox.setMaxWidth(Double.MAX_VALUE);
        this.passwordConfirmBox.setDisable(true);
        this.passwordNote.setId("pw-note-field");
        Label descriptionLabel = new Label(DESCRIPTION_TEXT);
        descriptionLabel.setWrapText(true);
        this.getChildren().addAll(descriptionLabel, passwordBox, passwordConfirmBox, passwordNote);
        this.setSpacing(10);
        this.setMaxWidth(Double.MAX_VALUE);

        this.passwordBox.textProperty().addListener((observable, oldValue, newValue) -> {
            this.passwordConfirmBox.setText("");
            this.passwordConfirmBox.setDisable(true);
            this.passwordNote.getStyleClass().clear();

            if (this.passwordBox.getText().equals(""))
            {
                this.passwordNote.setText(PW_NOTE_DEFAULT);
            }
            else
            {
                try
                {
                    PasswordRequirements.checkPasses(this.passwordBox.getText().getBytes());
                    this.passwordConfirmBox.setDisable(false);
                    this.passwordNote.setText(PW_NOTE_CONFIRM);
                }
                catch (IllegalPasswordException e)
                {
                    this.passwordNote.setText(e.getMessage());
                }
                this.passwordNote.getStyleClass().add(PW_NOTE_CLASS_NOT_OK);
            }
        });

        this.passwordConfirmBox.textProperty().addListener((observable, oldValue, newValue) -> {
            this.passwordNote.getStyleClass().clear();
            if (this.passwordConfirmBox.getText().equals(this.passwordBox.getText()))
            {
                this.passwordNote.setText(PW_NOTE_MATCH);
                this.passwordNote.getStyleClass().add(PW_NOTE_CLASS_OK);
            }
            else if (this.passwordConfirmBox.getText().equals(""))
            {
                this.passwordNote.setText(PW_NOTE_CONFIRM);
            }
            else
            {
                this.passwordNote.setText(PW_NOTE_NO_MATCH);
                this.passwordNote.getStyleClass().add(PW_NOTE_CLASS_NOT_OK);
            }
        });
    }

    public String getPasswordValue() throws IllegalPasswordException
    {
        PasswordRequirements.checkPasses(passwordBox.getText().getBytes());
        if (! passwordBox.getText().equals(passwordConfirmBox.getText()))
            throw new IllegalPasswordException("Password confirmation does not match");
        return passwordBox.getText();
    }
}
