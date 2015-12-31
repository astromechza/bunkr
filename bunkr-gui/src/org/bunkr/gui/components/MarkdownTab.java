package org.bunkr.gui.components;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.streams.input.MultilayeredInputStream;

import java.io.IOException;
import java.util.Arrays;

/**
 * Creator: benmeier
 * Created At: 2015-12-31
 */
public class MarkdownTab extends Tab
{
    private final ArchiveInfoContext archive;
    private final FileInventoryItem subject;

    VBox layout;
    ToolBar actionBar;
    Button saveButton;
    Button switchModeButton;
    TextArea editorArea;

    public MarkdownTab(FileInventoryItem file, ArchiveInfoContext archive)
    {
        super();
        this.subject = file;
        this.archive = archive;

        this.initControls();

        this.reloadContent();
    }

    private void initControls()
    {
        this.setText(this.subject.getAbsolutePath());
        this.actionBar = new ToolBar();
        this.saveButton = new Button("Save");
        this.switchModeButton = new Button("View");
        this.actionBar.getItems().addAll(this.saveButton, this.switchModeButton);

        this.editorArea = new TextArea();
        VBox.setVgrow(this.editorArea, Priority.ALWAYS);

        this.layout = new VBox();
        this.layout.getChildren().addAll(this.actionBar, this.editorArea);
        this.setContent(this.layout);
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
            e.printStackTrace();
        }
        this.editorArea.setText(content.toString());
    }

}
