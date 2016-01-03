package org.bunkr.gui.components.tabpanes.plaintext;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
    private TextArea editorArea, lineNumberArea;

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

        this.lineNumberArea = new TextArea();
        this.lineNumberArea.getStyleClass().add("editor-area-monospaced");
        this.lineNumberArea.getStyleClass().add("line-number-area");
        this.lineNumberArea.setEditable(false);
        this.lineNumberArea.setMaxWidth(60);
        HBox contentAreas = new HBox(this.lineNumberArea, this.editorArea);
        HBox.setHgrow(this.lineNumberArea, Priority.NEVER);
        HBox.setHgrow(this.editorArea, Priority.ALWAYS);
        VBox.setVgrow(contentAreas, Priority.ALWAYS);
        this.setContent(new VBox(actionBar, contentAreas));

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
            this.ensureLineNumbers();
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

        this.lineNumberArea.scrollTopProperty().bindBidirectional(this.editorArea.scrollTopProperty());
    }

    private void ensureLineNumbers()
    {
        int linen = this.lineNumberArea.getParagraphs().size();
        int diff = linen - this.editorArea.getParagraphs().size() - 1;
        if (diff > 0)
        {
            this.lineNumberArea.replaceText(this.editorArea.getParagraphs().size() * 5, this.lineNumberArea.getLength(), "");
        }
        else if (diff < 0)
        {
            StringBuilder addedLines = new StringBuilder();
            for (int i = 0; i < -diff; i++)
            {
                addedLines.append(String.format("%-4d\n", linen + i));
            }
            this.lineNumberArea.appendText(addedLines.toString());
        }
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
            this.ensureLineNumbers();
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
