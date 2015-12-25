package org.bunkr.gui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import org.bunkr.core.Resources;
import org.bunkr.core.exceptions.IllegalPasswordException;
import org.bunkr.core.usersec.PasswordRequirements;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-25
 */
public class NewArchiveSettingsWindow extends BaseWindow
{
    private static final int WINDOW_WIDTH = 400, WINDOW_HEIGHT = 300;
    private static final String PW_NOTE_DEFAULT = "Please enter a password";
    private static final String PW_NOTE_CONFIRM = "Please confirm password";
    private static final String PW_NOTE_MATCH = "Confirmation matches password";
    private static final String PW_NOTE_NO_MATCH = "Confirmation does not match password";
    private static final String PW_NOTE_CLASS_OK = "pw-note-success";
    private static final String PW_NOTE_CLASS_NOT_OK = "pw-note-failure";
    private final String cssPath;

    private CheckBox encryptionCheckBox, compressionCheckBox;
    private PasswordField passwordField, confirmPasswordField;
    private Label passwordNote;
    private Button cancelButton, createButton;

    public NewArchiveSettingsWindow() throws IOException
    {
        super();
        this.cssPath = Resources.getExternalPath("/resources/css/new_archive_settings_window.css");
        this.initialise();
        this.getStage().show();
    }

    @Override
    void initControls()
    {
        this.encryptionCheckBox = new CheckBox("Encrypt the archive");
        this.encryptionCheckBox.setSelected(true);
        this.compressionCheckBox = new CheckBox("Compress files stored in the archive");
        this.compressionCheckBox.setSelected(true);
        this.passwordField = new PasswordField();
        this.passwordField.setPromptText("Enter a password");
        this.confirmPasswordField = new PasswordField();
        this.confirmPasswordField.setPromptText("Confirm the password");
        this.confirmPasswordField.setDisable(true);
        this.passwordNote = new Label(PW_NOTE_DEFAULT);
        this.cancelButton = new Button("Cancel");
        this.createButton = new Button("Create");
    }

    @Override
    Parent initLayout()
    {
        GridPane rootLayout = new GridPane();
        rootLayout.setMinWidth(WINDOW_WIDTH);

        ColumnConstraints leftColum = new ColumnConstraints();
        leftColum.setPercentWidth(50);
        leftColum.setHgrow(Priority.ALWAYS);

        ColumnConstraints rightColum = new ColumnConstraints();
        rightColum.setPercentWidth(50);
        rightColum.setHgrow(Priority.ALWAYS);

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setFillHeight(true);
        rowConstraints.setVgrow(Priority.ALWAYS);

        rootLayout.getColumnConstraints().addAll(leftColum, rightColum);
        rootLayout.getRowConstraints().add(rowConstraints);
        rootLayout.setPadding(new Insets(10));
        rootLayout.setAlignment(Pos.CENTER);
        rootLayout.setHgap(10);
        rootLayout.setVgap(10);

        rootLayout.add(this.encryptionCheckBox, 0, 1);
        GridPane.setColumnSpan(this.encryptionCheckBox, 2);
        GridPane.setHalignment(this.encryptionCheckBox, HPos.LEFT);

        rootLayout.add(this.passwordField, 0, 2);
        GridPane.setColumnSpan(this.passwordField, 2);
        GridPane.setHalignment(this.passwordField, HPos.LEFT);

        rootLayout.add(this.confirmPasswordField, 0, 3);
        GridPane.setColumnSpan(this.confirmPasswordField, 2);
        GridPane.setHalignment(this.confirmPasswordField, HPos.LEFT);

        rootLayout.add(this.passwordNote, 0, 4);
        GridPane.setColumnSpan(this.passwordNote, 2);
        GridPane.setHalignment(this.passwordNote, HPos.RIGHT);

        rootLayout.add(this.compressionCheckBox, 0, 5);
        GridPane.setColumnSpan(this.compressionCheckBox, 2);
        GridPane.setHalignment(this.compressionCheckBox, HPos.LEFT);

        this.cancelButton.setMaxWidth(Double.MAX_VALUE);
        rootLayout.add(this.cancelButton, 0, 6);
        GridPane.setHalignment(this.cancelButton, HPos.LEFT);
        this.createButton.setMaxWidth(Double.MAX_VALUE);
        rootLayout.add(this.createButton, 1, 6);
        GridPane.setHalignment(this.createButton, HPos.RIGHT);

        return rootLayout;
    }

    @Override
    void bindEvents()
    {
        this.encryptionCheckBox.setOnAction(event -> {
            this.passwordField.setText("");
            this.confirmPasswordField.setText("");
            this.confirmPasswordField.setDisable(true);
            this.passwordNote.getStyleClass().clear();

            if (this.encryptionCheckBox.isSelected())
            {
                this.passwordField.setDisable(false);
                this.passwordNote.setText(PW_NOTE_DEFAULT);
            }
            else
            {
                this.passwordField.setDisable(true);
                this.passwordNote.setText("");
            }
        });

        this.passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.confirmPasswordField.textProperty().setValue("");
            this.confirmPasswordField.setDisable(true);
            this.passwordNote.getStyleClass().clear();

            if (this.passwordField.textProperty().get().equals(""))
            {
                this.passwordNote.setText(PW_NOTE_DEFAULT);
            }
            else
            {
                try
                {
                    PasswordRequirements.checkPasses(this.passwordField.textProperty().get().getBytes());
                    this.confirmPasswordField.setDisable(false);
                    this.passwordNote.setText(PW_NOTE_CONFIRM);
                }
                catch (IllegalPasswordException e)
                {
                    this.passwordNote.setText(e.getMessage());
                }
                this.passwordNote.getStyleClass().add(PW_NOTE_CLASS_NOT_OK);
            }
        });

        this.confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.passwordNote.getStyleClass().clear();
            if (this.confirmPasswordField.getText().equals(this.passwordField.getText()))
            {
                this.passwordNote.setText(PW_NOTE_MATCH);
                this.passwordNote.getStyleClass().add(PW_NOTE_CLASS_OK);
            }
            else if (this.confirmPasswordField.getText().equals(""))
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

    @Override
    void applyStyling()
    {
        this.passwordNote.setId("pw-note-field");
    }

    @Override
    Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr - New Archive");
        this.getStage().setScene(scene);
        this.getStage().setResizable(false);
        this.getStage().sizeToScene();
        return scene;
    }
}
