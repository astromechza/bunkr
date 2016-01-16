package org.bunkr.gui.components.tabs.html;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import org.bunkr.core.utils.Logging;
import org.bunkr.gui.Icons;
import org.bunkr.gui.components.tabs.IOpenedFileTab;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Creator: benmeier
 * Created At: 2016-01-16
 */
public class HtmlTab extends Tab implements IOpenedFileTab
{
    // inventory things
    private final ArchiveInfoContext archive;
    private final FileInventoryItem subject;

    // controls
    private VBox layout;
    private Button saveButton;
    private Button switchModeButton;
    private Button resetButton;
    private TextArea editorArea;
    private WebView formattedView;

    // mode
    private enum Mode {EDITTING, VIEWING}
    private Mode currentMode = null;

    // contents
    private String plainTextContent = null;
    private boolean hasChanges = true;

    private Consumer<String> onSaveInventoryRequest;

    public HtmlTab(FileInventoryItem file, ArchiveInfoContext archive)
    {
        super();
        this.subject = file;
        this.archive = archive;

        this.initControls();
        this.reloadContent();

        try
        {
            HtmlWebViewAdapter hwva = new HtmlWebViewAdapter();
            hwva.adapt(this.formattedView);
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
        actionBar.getItems().addAll(this.saveButton, this.resetButton, this.switchModeButton);
        VBox.setVgrow(actionBar, Priority.NEVER);

        this.editorArea = new TextArea();
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
                    byte[] buffer = new byte[1024 * 1024];
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
            byte[] buffer = new byte[1024 * 1024];
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
        this.editorArea.setText((this.plainTextContent == null) ? "" : this.plainTextContent);
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
            renderedContent = this.hasChanges ? this.editorArea.getText() : this.plainTextContent;
        }
        this.formattedView.getEngine().loadContent(renderedContent);

        if (this.layout.getChildren().contains(this.editorArea))this.layout.getChildren().remove(this.editorArea);
        if (!this.layout.getChildren().contains(this.formattedView)) this.layout.getChildren().add(this.formattedView);
    }

    public void setOnSaveInventoryRequest(Consumer<String> onSaveInventoryRequest)
    {
        this.onSaveInventoryRequest = onSaveInventoryRequest;
    }
}
