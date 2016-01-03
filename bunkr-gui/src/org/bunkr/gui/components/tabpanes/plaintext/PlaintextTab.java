package org.bunkr.gui.components.tabpanes.plaintext;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import org.bunkr.gui.components.tabpanes.IOpenedFileTab;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Creator: benmeier
 * Created At: 2016-01-03
 */
public class PlaintextTab extends Tab implements IOpenedFileTab
{
    // inventory things
    private final ArchiveInfoContext archive;
    private final FileInventoryItem subject;

    private Button saveButton;
    private Button resetButton;
    private TextArea editorArea;

    // contents
    private String plainTextContent;
    private boolean hasChanges = true;

    private Consumer<String> onSaveInventoryRequest;

    public PlaintextTab(FileInventoryItem file, ArchiveInfoContext archive)
    {
        super();
        this.subject = file;
        this.archive = archive;

        this.initControls();

        this.reloadContent();

        this.bindEvents();
    }

    private void initControls()
    {
        this.setText(this.subject.getAbsolutePath());
        ToolBar actionBar = new ToolBar();
        this.saveButton = new Button("Save");
        this.saveButton.getStyleClass().add("small-button");
        this.resetButton = new Button("Reload");
        this.resetButton.getStyleClass().add("small-button");
        actionBar.getItems().addAll(this.saveButton, this.resetButton);
        VBox.setVgrow(actionBar, Priority.NEVER);

        this.editorArea = new TextArea();
        this.editorArea.getStyleClass().add("editor-area-monospaced");
        VBox.setVgrow(this.editorArea, Priority.ALWAYS);

        this.setContent(new VBox(actionBar, this.editorArea));

        this.getStyleClass().add("open-file-tab");
    }

    private void bindEvents()
    {
        this.saveButton.setOnAction(e -> {
            try
            {
                try (
                        MultilayeredOutputStream bwos = new MultilayeredOutputStream(this.archive, this.subject);
                        InputStream is = new ByteArrayInputStream(this.editorArea.getText().getBytes()))
                {
                    byte[] buffer = new byte[1024 * 1024];
                    int n;
                    while ((n = is.read(buffer)) != -1)
                    {
                        bwos.write(buffer, 0, n);
                    }
                    Arrays.fill(buffer, (byte) 0);
                }
                this.plainTextContent = this.editorArea.getText();
                this.checkSaveAllowed();
                this.onSaveInventoryRequest.accept("Wrote content to file " + this.getText());
            }
            catch (IOException exc)
            {
                QuickDialogs.exception(exc);
            }
        });

        this.resetButton.setOnAction(e -> this.reloadContent());

        this.editorArea.textProperty().addListener((observable, oldValue, newValue) -> {
            this.checkSaveAllowed();
        });

        this.setOnCloseRequest(event -> {
            if (this.hasChanges)
            {
                if (! QuickDialogs.confirm("File %s has changes. Are you sure you want to close it?", this.getText()))
                {
                    event.consume();
                }
            }
        });
    }

    public void notifyRename()
    {
        this.setText(this.subject.getAbsolutePath());
    }

    public void reloadContent()
    {
        if (this.subject.getParent() == null)
        {
            throw new RuntimeException("Orphaned file tab found");
        }

        StringBuilder content = new StringBuilder();
        try (MultilayeredInputStream ms = new MultilayeredInputStream(this.archive, this.subject))
        {
            ms.setCheckHashOnFinish(true);
            byte[] buffer = new byte[1024 * 1024];
            int n;
            while ((n = ms.read(buffer)) != -1)
            {
                content.append(new String(buffer, 0, n));
            }
            Arrays.fill(buffer, (byte) 0);
            this.plainTextContent = content.toString();
            this.editorArea.setText(this.plainTextContent);
            this.hasChanges = false;
            this.saveButton.setDisable(true);
            this.getStyleClass().remove("has-changes");
        }
        catch (IOException e)
        {
            QuickDialogs.exception(e);
        }
    }

    public void checkSaveAllowed()
    {
        if (this.editorArea.textProperty().length().isEqualTo(this.plainTextContent.length()).get() && this.editorArea.getText().equals(this.plainTextContent))
        {
            this.hasChanges = false;
            this.saveButton.setDisable(true);
            this.getStyleClass().remove("has-changes");
        }
        else
        {
            this.hasChanges = true;
            this.saveButton.setDisable(false);
            if (! this.getStyleClass().contains("has-changes")) this.getStyleClass().add("has-changes");
        }
    }

    public void setOnSaveInventoryRequest(Consumer<String> onSaveInventoryRequest)
    {
        this.onSaveInventoryRequest = onSaveInventoryRequest;
    }
}
