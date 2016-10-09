package org.bunkr.gui.components.tabs.text;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import org.bunkr.core.utils.Units;
import org.bunkr.gui.Icons;
import org.bunkr.gui.components.HExpander;
import org.bunkr.gui.components.tabs.IOpenedFileTab;
import org.bunkr.gui.dialogs.QuickDialogs;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.wellbehaved.event.EventHandlerHelper;
import org.fxmisc.wellbehaved.event.EventPattern;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Created At: 2016-10-09
 */
public class TextTab extends Tab implements IOpenedFileTab
{
    // inventory things
    private final ArchiveInfoContext archive;
    private final FileInventoryItem subject;

    private Button saveButton;
    private Button resetButton;
    private CodeArea editorArea;
    private CheckBox wordWrapCheckBox;

    // contents
    // Plain text content is a cache of the content held by the archive, it should only change its value when it has
    // been persisted to the archive.
    private String plainTextContent = null;
    private boolean hasChanges = true;

    private Consumer<String> onSaveInventoryRequest;

    public TextTab(FileInventoryItem file, ArchiveInfoContext archive)
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
        this.saveButton = Icons.buildIconButton("Save", Icons.ICON_SAVE);
        this.saveButton.getStyleClass().add("small-button");
        this.resetButton = Icons.buildIconButton("Reload", Icons.ICON_RELOAD);
        this.resetButton.getStyleClass().add("small-button");
        this.wordWrapCheckBox = new CheckBox("Word Wrap");
        this.wordWrapCheckBox.getStyleClass().add("small-button");
        this.wordWrapCheckBox.selectedProperty().set(true);
        actionBar.getItems().addAll(this.saveButton, this.resetButton, new HExpander(), this.wordWrapCheckBox);
        VBox.setVgrow(actionBar, Priority.NEVER);

        this.editorArea = new CodeArea();
        this.editorArea.setWrapText(true);
        this.editorArea.setParagraphGraphicFactory(LineNumberFactory.get(this.editorArea));
        this.editorArea.getStyleClass().add("editor-area-monospaced");
        VBox.setVgrow(this.editorArea, Priority.ALWAYS);

        VBox layout = new VBox();
        layout.getChildren().addAll(actionBar);
        layout.getChildren().add(this.editorArea);
        this.setContent(layout);

        this.getStyleClass().add("open-file-tab");
    }

    private void bindEvents()
    {
        this.saveButton.setOnAction(e -> this.saveContent());
        this.resetButton.setOnAction(e -> this.reloadContent());

        this.editorArea.textProperty().addListener((observable, oldValue, newValue) -> {
            this.checkChanges();
        });

        this.setOnCloseRequest(event -> {
            if (this.hasChanges)
            {
                if (!QuickDialogs.confirm("File %s has changes. Are you sure you want to close it?", this.getText()))
                {
                    event.consume();
                }
            }
        });

        this.wordWrapCheckBox.setOnAction(e -> this.editorArea.setWrapText(this.wordWrapCheckBox.isSelected()));

        EventHandlerHelper.install(this.editorArea.onKeyPressedProperty(), EventHandlerHelper
                .on(EventPattern.keyPressed(KeyCode.TAB)).act(event -> {
                    int c = this.editorArea.getCaretColumn();
                    c = 4 - (c % 4);
                    this.editorArea.replaceSelection(new String(new char[c]).replace("\0", " "));
                })
                .create());
    }

    @Override
    public void notifyRename()
    {
        this.setText(this.subject.getAbsolutePath());
    }

    public void reloadContent()
    {
        // first make sure we can get the absolute path
        if (this.subject.getParent() == null) throw new RuntimeException("Orphaned file tab found");

        StringBuilder content = new StringBuilder();
        try (MultilayeredInputStream ms = new MultilayeredInputStream(this.archive, this.subject))
        {
            ms.setCheckHashOnFinish(true);
            byte[] buffer = new byte[(int) Units.MEBIBYTE];
            int n;
            while ((n = ms.read(buffer)) != -1)
            {
                content.append(new String(buffer, 0, n));
            }
            Arrays.fill(buffer, (byte) 0);
        }
        catch (IOException e)
        {
            QuickDialogs.exception(e);
            return;
        }

        this.plainTextContent = content.toString();
        this.editorArea.clear();
        this.editorArea.replaceText(0, 0, this.plainTextContent);
        this.hasChanges = false;
        this.saveButton.setDisable(true);
        this.getStyleClass().remove("has-changes");
    }

    public void saveContent()
    {
        try
        {
            try (
                    MultilayeredOutputStream bwos = new MultilayeredOutputStream(this.archive, this.subject);
                    InputStream is = new ByteArrayInputStream(this.editorArea.getText().getBytes()))
            {
                byte[] buffer = new byte[(int) Units.MEBIBYTE];
                int n;
                while ((n = is.read(buffer)) != -1)
                {
                    bwos.write(buffer, 0, n);
                }
                Arrays.fill(buffer, (byte) 0);
            }
            this.plainTextContent = this.editorArea.getText();
            this.checkChanges();
            this.onSaveInventoryRequest.accept("Wrote content to file " + this.getText());
        }
        catch (IOException exc)
        {
            QuickDialogs.exception(exc);
        }
    }

    public void checkChanges()
    {
        this.hasChanges = ! this.editorArea.getText().equals(this.plainTextContent);
        this.saveButton.setDisable(!this.hasChanges);
        this.getStyleClass().remove("has-changes");
        if (this.hasChanges)  this.getStyleClass().add("has-changes");
    }

    public void setOnSaveInventoryRequest(Consumer<String> onSaveInventoryRequest)
    {
        this.onSaveInventoryRequest = onSaveInventoryRequest;
    }
}
