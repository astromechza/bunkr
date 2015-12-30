package org.bunkr.gui.components;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.FileInventoryItem;

/**
 * Creator: benmeier
 * Created At: 2015-12-31
 */
public class MarkdownTab extends Tab
{
    FileInventoryItem subject;
    VBox layout;
    ToolBar actionBar;
    Button saveButton;
    Button switchModeButton;
    Button renameButton;
    TextArea editorArea;

    public MarkdownTab(FileInventoryItem file, ArchiveInfoContext context)
    {
        super();
        this.subject = file;

        initContent();
    }

    private void initContent()
    {
        this.setText(this.subject.getAbsolutePath());
        this.actionBar = new ToolBar();
        this.saveButton = new Button("Save");
        this.switchModeButton = new Button("View");
        this.renameButton = new Button("Rename");
        this.actionBar.getItems().addAll(this.saveButton, this.switchModeButton, this.renameButton);

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
}
