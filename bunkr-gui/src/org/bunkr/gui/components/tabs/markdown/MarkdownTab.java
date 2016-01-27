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

package org.bunkr.gui.components.tabs.markdown;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.exceptions.TraversalException;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.IFFTraversalTarget;
import org.bunkr.core.inventory.InventoryPather;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import org.bunkr.core.utils.Logging;
import org.bunkr.core.utils.Units;
import org.bunkr.gui.Icons;
import org.bunkr.gui.components.HExpander;
import org.bunkr.gui.components.tabs.IOpenedFileTab;
import org.bunkr.gui.dialogs.QuickDialogs;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.wellbehaved.event.EventHandlerHelper;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.markdown4j.Markdown4jProcessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Created At: 2015-12-31
 */
public class MarkdownTab extends Tab implements IOpenedFileTab
{
    // inventory things
    private final ArchiveInfoContext archive;
    private final FileInventoryItem subject;

    // tools
    private final Markdown4jProcessor markdownProcessor = new Markdown4jProcessor();

    // controls
    private VBox layout;
    private Button saveButton;
    private Button switchModeButton;
    private Button resetButton;
    private CodeArea editorArea;
    private WebView formattedView;
    private CheckBox wordWrapCheckBox;

    // mode
    private enum Mode {EDITTING, VIEWING}
    private Mode currentMode = null;

    // contents
    // Plain text content is a cache of the content held by the archive, it should only change its value when it has
    // been persisted to the archive.
    private String plainTextContent = null;
    private boolean hasChanges = true;

    private Consumer<FileInventoryItem> onRequestOpen;
    private Consumer<String> onSaveInventoryRequest;

    public MarkdownTab(FileInventoryItem file, ArchiveInfoContext archive)
    {
        super();
        this.subject = file;
        this.archive = archive;

        this.initControls();
        this.reloadContent();

        try
        {
            MarkdownWebViewAdapter mwva = new MarkdownWebViewAdapter();
            mwva.adapt(this.formattedView);
            mwva.setOnTryOpenFileItem(s -> {
                if (InventoryPather.isValidPath(s))
                {
                    this.requestOpenTab(s);
                }
                else if (InventoryPather.isValidRelativePath(s))
                {
                    String parentPath = InventoryPather.applyRelativePath(subject.getAbsolutePath(), "..");
                    String targetPath = InventoryPather.applyRelativePath(parentPath, s);
                    this.requestOpenTab(targetPath);
                }
            });
        }
        catch (IOException e)
        {
            Logging.exception(e);
        }

        this.bindEvents();
        this.goToViewMode();
    }

    private void initControls()
    {
        this.setText(this.subject.getAbsolutePath());
        ToolBar actionBar = new ToolBar();
        this.saveButton = Icons.buildIconButton("Save", Icons.ICON_SAVE);
        this.saveButton.getStyleClass().add("small-button");
        this.switchModeButton = new Button("View");
        this.switchModeButton.getStyleClass().add("small-button");
        this.resetButton = Icons.buildIconButton("Reload", Icons.ICON_RELOAD);
        this.resetButton.getStyleClass().add("small-button");
        this.wordWrapCheckBox = new CheckBox("Word Wrap");
        this.wordWrapCheckBox.getStyleClass().add("small-button");
        actionBar.getItems().addAll(this.saveButton, this.resetButton, this.switchModeButton, new HExpander(), this.wordWrapCheckBox);
        VBox.setVgrow(actionBar, Priority.NEVER);

        this.editorArea = new CodeArea();
        this.editorArea.setParagraphGraphicFactory(LineNumberFactory.get(this.editorArea));
        this.editorArea.getStyleClass().add("editor-area-monospaced");
        VBox.setVgrow(this.editorArea, Priority.ALWAYS);

        this.formattedView = new WebView();
        VBox.setVgrow(this.formattedView, Priority.ALWAYS);

        this.layout = new VBox();
        this.layout.getChildren().addAll(actionBar);
        this.setContent(this.layout);

        this.getStyleClass().add("open-file-tab");
    }

    private void bindEvents()
    {
        this.switchModeButton.setOnAction(e -> {
            if (this.currentMode == Mode.VIEWING)
                this.goToEditMode();
            else
                this.goToViewMode();
        });

        this.saveButton.setOnAction(e -> {
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
        });

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

    public void checkChanges()
    {
        this.hasChanges = ! this.editorArea.getText().equals(this.plainTextContent);
        this.saveButton.setDisable(!this.hasChanges);
        this.getStyleClass().remove("has-changes");
        if (this.hasChanges)  this.getStyleClass().add("has-changes");
    }

    public void goToEditMode()
    {
        this.currentMode = Mode.EDITTING;
        this.switchModeButton.setText("View");
        this.switchModeButton.setGraphic(Icons.buildIconLabel(Icons.ICON_VIEW));
        if (! this.hasChanges && this.plainTextContent != null) this.editorArea.replaceText(0, 0, this
                .plainTextContent);
        if (this.layout.getChildren().contains(this.formattedView)) this.layout.getChildren().remove(this.formattedView);
        if (!this.layout.getChildren().contains(this.editorArea)) this.layout.getChildren().add(this.editorArea);
    }

    public void goToViewMode()
    {
        this.currentMode = Mode.VIEWING;
        this.switchModeButton.setText("Edit");
        this.switchModeButton.setGraphic(Icons.buildIconLabel(Icons.ICON_EDIT));

        String renderedContent = "";
        if (this.plainTextContent != null)
        {
            try
            {
                renderedContent = this.markdownProcessor.process(this.hasChanges ? this.editorArea.getText() : this.plainTextContent);
            }
            catch (IOException e)
            {
                renderedContent = e.getLocalizedMessage();
            }
        }
        this.formattedView.getEngine().loadContent(renderedContent);

        if (this.layout.getChildren().contains(this.editorArea))this.layout.getChildren().remove(this.editorArea);
        if (!this.layout.getChildren().contains(this.formattedView)) this.layout.getChildren().add(this.formattedView);
    }

    public void requestOpenTab(String absolutePath)
    {
        try
        {
            IFFTraversalTarget t = InventoryPather.traverse(this.archive.getInventory(), absolutePath);
            if (t instanceof FileInventoryItem) this.onRequestOpen.accept((FileInventoryItem) t);
            else QuickDialogs.error("Item %s is not a file.", absolutePath);
        }
        catch (TraversalException e)
        {
            QuickDialogs.exception(e);
        }
    }

    public void setOnSaveInventoryRequest(Consumer<String> onSaveInventoryRequest)
    {
        this.onSaveInventoryRequest = onSaveInventoryRequest;
    }

    public void setOnRequestOpen(Consumer<FileInventoryItem> onRequestOpen)
    {
        this.onRequestOpen = onRequestOpen;
    }
}
