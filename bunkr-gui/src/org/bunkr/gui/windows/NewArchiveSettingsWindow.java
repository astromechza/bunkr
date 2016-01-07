package org.bunkr.gui.windows;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bouncycastle.crypto.CryptoException;
import org.bunkr.core.ArchiveBuilder;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.Resources;
import org.bunkr.core.descriptor.IDescriptor;
import org.bunkr.core.descriptor.PBKDF2Descriptor;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.exceptions.IllegalPasswordException;
import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.usersec.PasswordRequirements;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Creator: benmeier
 * Created At: 2015-12-25
 */
public class NewArchiveSettingsWindow extends BaseWindow
{
    private static final int WINDOW_WIDTH = 400;
    private static final String PW_NOTE_DEFAULT = "Please enter a password";
    private static final String PW_NOTE_CONFIRM = "Please confirm password";
    private static final String PW_NOTE_MATCH = "Confirmation matches password";
    private static final String PW_NOTE_NO_MATCH = "Confirmation does not match password";
    private static final String PW_NOTE_CLASS_OK = "pw-note-success";
    private static final String PW_NOTE_CLASS_NOT_OK = "pw-note-failure";

    private final String cssPath;
    private final Stage previousWindow;

    private CheckBox encryptionCheckBox, compressionCheckBox;
    private PasswordField passwordField, confirmPasswordField;
    private Label passwordNote, topLabel;
    private Button cancelButton, createButton;

    public NewArchiveSettingsWindow(Stage previousWindow) throws IOException
    {
        super();
        this.previousWindow = previousWindow;
        this.cssPath = Resources.getExternalPath("/resources/css/archive_settings_window.css");
        this.initialise();
    }

    @Override
    public void initControls()
    {
        this.topLabel = new Label("Settings:");
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
        this.cancelButton = new Button("Back");
        this.createButton = new Button("Create");
        this.createButton.setDisable(true);
    }

    @Override
    public Parent initLayout()
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

        rootLayout.add(this.topLabel, 0, 0);
        GridPane.setColumnSpan(this.encryptionCheckBox, 2);
        GridPane.setHalignment(this.encryptionCheckBox, HPos.LEFT);

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
    public void bindEvents()
    {
        this.encryptionCheckBox.setOnAction(event -> {
            Arrays.fill(this.passwordField.getText().getBytes(), (byte) 0);
            this.passwordField.setText("");
            Arrays.fill(this.confirmPasswordField.getText().getBytes(), (byte) 0);
            this.confirmPasswordField.setText("");
            this.confirmPasswordField.setDisable(true);
            this.passwordNote.getStyleClass().clear();

            if (this.encryptionCheckBox.isSelected())
            {
                this.createButton.setDisable(true);
                this.passwordField.setDisable(false);
                this.passwordNote.setText(PW_NOTE_DEFAULT);
            }
            else
            {
                this.createButton.setDisable(false);
                this.passwordField.setDisable(true);
                this.passwordNote.setText("");
            }
        });

        this.passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.createButton.setDisable(true);
            Arrays.fill(this.confirmPasswordField.getText().getBytes(), (byte) 0);
            this.confirmPasswordField.setText("");
            this.confirmPasswordField.setDisable(true);
            this.passwordNote.getStyleClass().clear();

            if (this.passwordField.getText().equals(""))
            {
                this.passwordNote.setText(PW_NOTE_DEFAULT);
            }
            else
            {
                try
                {
                    PasswordRequirements.checkPasses(this.passwordField.getText().getBytes());
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
            this.createButton.setDisable(true);
            if (this.confirmPasswordField.getText().equals(this.passwordField.getText()))
            {
                this.passwordNote.setText(PW_NOTE_MATCH);
                this.passwordNote.getStyleClass().add(PW_NOTE_CLASS_OK);
                this.createButton.setDisable(false);
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

        this.getStage().setOnCloseRequest(event -> this.previousWindow.show());

        this.cancelButton.setOnAction(event -> {
            this.previousWindow.show();
            this.getStage().close();
        });

        this.createButton.setOnAction(event -> {
            try
            {

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Archive ...");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Bunkr Archives", "*.bunkr", "*.bnkr"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedPath = fileChooser.showSaveDialog(this.getStage());
                if (selectedPath == null) return;

                this.getStage().getScene().setCursor(Cursor.WAIT);

                PasswordProvider passProv = new PasswordProvider();
                UserSecurityProvider usp = new UserSecurityProvider(passProv);

                IDescriptor archiveDescriptor;
                if (this.encryptionCheckBox.isSelected())
                {
                    passProv.setArchivePassword(this.passwordField.getText().getBytes());
                    archiveDescriptor = PBKDF2Descriptor.makeDefaults();
                }
                else
                {
                    archiveDescriptor = new PlaintextDescriptor();
                }

                ArchiveInfoContext archive = ArchiveBuilder.createNewEmptyArchive(
                        selectedPath,
                        archiveDescriptor,
                        usp,
                        this.compressionCheckBox.isSelected()
                );

                new MainWindow(archive, usp).getStage().show();
                this.getStage().close();
            }
            catch (CryptoException | BaseBunkrException | IOException e)
            {
                // TODO probably some exceptions we can display with a better message instead of defaulting to ExceptionDialog
                QuickDialogs.exception(e);
            }
            finally
            {
                this.getStage().getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }

    @Override
    public void applyStyling()
    {
        this.passwordNote.setId("pw-note-field");
        this.getRootLayout().getStyleClass().add("background");
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr - New Archive");
        this.getStage().setScene(scene);
        this.getStage().setResizable(false);
        this.getStage().sizeToScene();
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        return scene;
    }
}
