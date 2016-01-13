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

package org.bunkr.gui.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import org.bunkr.core.Resources;
import org.bunkr.gui.Icons;
import org.bunkr.gui.windows.BaseWindow;

import java.io.File;
import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-26
 */
public class PasswordDialog extends BaseWindow
{
    private final String cssPath;

    private PasswordField passwordField;
    private Label topLabel, passwordLabel, passwordFileLabel;
    private TextField passwordFilePathBox;
    private Button cancelButton, continueButton, pickPasswordFileButton;

    private boolean disabledStateListeners = false;

    public PasswordDialog() throws IOException
    {
        super();
        this.cssPath = Resources.getExternalPath("/resources/css/password_dialog.css");
        this.initialise();
    }

    @Override
    public void initControls()
    {
        this.topLabel = new Label("This archive requires a password. Please enter a password or provide a path to a file containing the password.");
        this.topLabel.setWrapText(true);
        this.passwordLabel = new Label("Password:");
        this.passwordFileLabel = new Label("Password File:");
        this.passwordField = new PasswordField();
        this.passwordFilePathBox = new TextField();
        this.pickPasswordFileButton = Icons.buildIconButton("Select", Icons.ICON_ELLIPSIS);
        this.cancelButton = Icons.buildIconButton("Cancel", Icons.ICON_CROSS);
        this.continueButton = Icons.buildIconButton("Continue", Icons.ICON_TICK);
        this.continueButton.setDisable(true);
    }

    @Override
    public Parent initLayout()
    {
        VBox rootBox = new VBox();
        rootBox.setMaxWidth(400);
        rootBox.setFillWidth(true);
        rootBox.setSpacing(10);
        rootBox.setPadding(new Insets(10));

        rootBox.getChildren().add(this.topLabel);

        rootBox.getChildren().add(this.passwordLabel);
        rootBox.getChildren().add(this.passwordField);

        rootBox.getChildren().add(this.passwordFileLabel);
        this.passwordFilePathBox.setMaxWidth(Double.MAX_VALUE);
        HBox pfhb = new HBox(10, this.passwordFilePathBox, this.pickPasswordFileButton);
        HBox.setHgrow(this.passwordFilePathBox, Priority.ALWAYS);
        pfhb.setAlignment(Pos.CENTER_RIGHT);
        rootBox.getChildren().add(pfhb);

        HBox btnhb = new HBox(10, this.cancelButton, this.continueButton);
        btnhb.setAlignment(Pos.CENTER_RIGHT);
        rootBox.getChildren().add(btnhb);

        return rootBox;
    }

    @Override
    public void bindEvents()
    {
        this.passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (! disabledStateListeners)
            {
                disabledStateListeners = true;
                this.passwordFilePathBox.clear();
                this.continueButton.setDisable((newValue.length() == 0));
                disabledStateListeners = false;
            }
        });

        this.pickPasswordFileButton.setOnAction(event -> {
            if (!disabledStateListeners)
            {
                disabledStateListeners = true;
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Pick password file");
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedPath = fileChooser.showOpenDialog(this.getStage());
                if (selectedPath != null)
                {
                    this.passwordField.clear();
                    this.passwordFilePathBox.setText(selectedPath.getAbsolutePath());
                    this.continueButton.setDisable(false);
                }
                disabledStateListeners = false;
            }
        });

        this.passwordFilePathBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!disabledStateListeners)
            {
                disabledStateListeners = true;
                this.passwordField.clear();
                this.continueButton.setDisable(false);
                if (newValue.length() == 0)
                {
                    this.continueButton.setDisable(true);
                }
                disabledStateListeners = false;
            }
        });

        this.cancelButton.setOnAction(event -> {
            this.passwordField.clear();
            this.passwordFilePathBox.clear();
            this.getStage().close();
        });

        this.continueButton.setOnAction(event -> this.getStage().close());

        this.getStage().setOnCloseRequest(event -> {
            this.passwordField.clear();
            this.passwordFilePathBox.clear();
        });
    }

    @Override
    public void applyStyling()
    {
        this.passwordLabel.getStyleClass().add("small-label");
        this.passwordFileLabel.getStyleClass().add("small-label");
        this.getRootLayout().getStyleClass().add("background");
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssCommon);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr - Provide a Password");
        this.getStage().setScene(scene);
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        this.getStage().sizeToScene();
        this.getStage().setResizable(false);
        return scene;
    }

    public boolean hasFile()
    {
        return this.passwordFilePathBox.getText().length() > 0;
    }

    public File getFile()
    {
        return new File(this.passwordFilePathBox.getText());
    }

    public boolean hasPassword()
    {
        return this.passwordField.getText().length() > 0;
    }

    public byte[] getPassword()
    {
        return this.passwordField.getText().getBytes();
    }
}
