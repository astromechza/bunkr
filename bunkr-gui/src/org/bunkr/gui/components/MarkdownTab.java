package org.bunkr.gui.components;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import org.bunkr.gui.dialogs.QuickDialogs;
import org.markdown4j.Markdown4jProcessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Creator: benmeier
 * Created At: 2015-12-31
 */
public class MarkdownTab extends Tab
{
    // inventory things
    private final ArchiveInfoContext archive;
    private final FileInventoryItem subject;

    // tools
    private final Markdown4jProcessor markdownProcessor;

    // controls
    private VBox layout;
    private ToolBar actionBar;
    private Button saveButton;
    private Button switchModeButton;
    private TextArea editorArea;
    private WebView formattedView;

    // mode
    private enum Mode {EDITTING, VIEWING}
    private Mode currentMode = null;

    // contents
    private String title;
    private String plainTextContent = null;
    private String htmlContent = null;
    private boolean hasChanges = true;


    public MarkdownTab(FileInventoryItem file, ArchiveInfoContext archive)
    {
        super();
        this.subject = file;
        this.archive = archive;

        this.markdownProcessor = new Markdown4jProcessor();

        this.reloadContent();

        this.initControls();

        try
        {
            new MarkdownWebViewAdapter().adapt(this.formattedView);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        this.bindEvents();

        this.goToViewMode();
    }

    private void initControls()
    {
        this.setText(this.subject.getAbsolutePath());
        this.actionBar = new ToolBar();
        this.saveButton = new Button("Save");
        this.saveButton.getStyleClass().add("small-button");
        this.switchModeButton = new Button("View");
        this.switchModeButton.getStyleClass().add("small-button");
        this.actionBar.getItems().addAll(this.saveButton, this.switchModeButton);
        VBox.setVgrow(this.actionBar, Priority.NEVER);

        this.editorArea = new TextArea();
        this.editorArea.getStyleClass().add("editor-area-monospaced");
        VBox.setVgrow(this.editorArea, Priority.ALWAYS);

        this.formattedView = new WebView();
        VBox.setVgrow(this.formattedView, Priority.ALWAYS);

        this.layout = new VBox();
        this.layout.getChildren().addAll(this.actionBar);
        this.setContent(this.layout);

        this.getStyleClass().add("open-file-tab");
    }

    private void bindEvents()
    {
        this.switchModeButton.setOnAction(e -> {
            if (this.currentMode == Mode.VIEWING)
            {
                this.goToEditMode();
            }
            else
            {
                this.plainTextContent = this.editorArea.getText();
                this.goToViewMode();
            }
        });

        this.saveButton.setOnAction(e -> {
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
            catch (IOException exc)
            {
                QuickDialogs.exception(exc);
            }
        });
    }

    public void notifyRename()
    {
        this.setText(this.subject.getAbsolutePath());
    }

    public void reloadContent()
    {
        StringBuilder content = new StringBuilder();
        if (this.subject.getParent() == null)
        {
            throw new RuntimeException("Orphaned file tab found");
        }

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
            this.hasChanges = false;
        }
        catch (IOException e)
        {
            QuickDialogs.exception(e);
        }
    }

    private void updateMarkdownRender()
    {
        try
        {
            if (this.plainTextContent == null)
            {
                this.htmlContent = "";
            }
            else
            {
                this.htmlContent = this.markdownProcessor.process(this.plainTextContent);
            }
        }
        catch (IOException e)
        {
            this.htmlContent = e.getLocalizedMessage();
        }
    }

    public void goToEditMode()
    {
        this.currentMode = Mode.EDITTING;
        this.switchModeButton.setText("View");

        if (this.plainTextContent == null)
        {
            this.editorArea.setText("");
        }
        else
        {
            this.editorArea.setText(this.plainTextContent);
        }

        if (this.layout.getChildren().contains(this.formattedView)) this.layout.getChildren().remove(this.formattedView);
        if (!this.layout.getChildren().contains(this.editorArea)) this.layout.getChildren().add(this.editorArea);
    }

    public void goToViewMode()
    {
        this.currentMode = Mode.VIEWING;
        this.switchModeButton.setText("Edit");

        this.updateMarkdownRender();

        this.formattedView.getEngine().loadContent(this.htmlContent);

        if (this.layout.getChildren().contains(this.editorArea))this.layout.getChildren().remove(this.editorArea);
        if (!this.layout.getChildren().contains(this.formattedView)) this.layout.getChildren().add(this.formattedView);
    }


}
