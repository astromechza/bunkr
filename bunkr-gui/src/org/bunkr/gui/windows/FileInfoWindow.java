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

package org.bunkr.gui.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import org.bunkr.core.Resources;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.inventory.MediaType;
import org.bunkr.core.utils.Formatters;
import org.bunkr.gui.Icons;
import org.bunkr.gui.components.SelectableLabel;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created At: 2015-12-29
 */
public class FileInfoWindow extends BaseWindow
{
    private static final int WINDOW_WIDTH = 600;

    private final String cssPath;
    private final FileInventoryItem item;
    private final String filePath;

    // components
    private Label lblFilePath; SelectableLabel lblFilePathValue;
    private Label lblActualSize; SelectableLabel lblActualSizeValue;
    private Label lblLastMod; SelectableLabel lblLastModValue;
    private Label lblSizeOnDisk; SelectableLabel lblSizeOnDiskValue;
    private Label lblUUID; SelectableLabel lblUUIDValue;
    private Label lblMediaType, lblMediaTypeValue;
    private Button closeButton, applyButton, changeMediaTypeButton;

    private Consumer<String> onSaveInventoryRequest;
    private Consumer<FileInventoryItem> onRefreshTreeItem;

    public FileInfoWindow(FileInventoryItem item) throws IOException
    {
        super();
        this.filePath = item.getAbsolutePath();
        this.item = item;
        this.cssPath = Resources.getExternalPath("/resources/css/fileinfo_dialog.css");
        this.initialise();
    }

    @Override
    public void initControls()
    {

        this.lblFilePath = new Label("File Path:");
        this.lblFilePathValue = new SelectableLabel(this.filePath);

        this.lblActualSize = new Label("Actual Size:");
        this.lblActualSizeValue = new SelectableLabel(String.format(
                "%s (%s bytes)", Formatters.formatPrettyInt(this.item.getActualSize()), this.item.getActualSize()
        ));

        this.lblLastMod = new Label("Last Modified:");
        this.lblLastModValue = new SelectableLabel(Formatters.formatPrettyDate(this.item.getModifiedAt()));
        Tooltip.install(this.lblLastModValue, new Tooltip(Formatters.formatIso8601utc(this.item.getModifiedAt())));

        this.lblSizeOnDisk = new Label("Size on Disk:");
        this.lblSizeOnDiskValue = new SelectableLabel(String.format(
                "%s (%s bytes)", Formatters.formatPrettyInt(this.item.getSizeOnDisk()), this.item.getSizeOnDisk()
        ));

        this.lblUUID = new Label("File UUID:");
        this.lblUUIDValue = new SelectableLabel(this.item.getUuid().toString());

        this.lblMediaType = new Label("Media Type:");
        this.lblMediaTypeValue = new Label(this.item.getMediaType());
        this.changeMediaTypeButton = new Button("Change..");

        this.applyButton = Icons.buildIconButton("Save Changes", Icons.ICON_SAVE);
        this.applyButton.setDisable(true);
        this.closeButton = Icons.buildIconButton("Close", Icons.ICON_CROSS);
    }

    @Override
    public Parent initLayout()
    {
        GridPane rootLayout = new GridPane();
        rootLayout.setMinWidth(WINDOW_WIDTH);
        rootLayout.setMaxHeight(Double.MAX_VALUE);

        ColumnConstraints leftColumn = new ColumnConstraints();
        leftColumn.setHgrow(Priority.SOMETIMES);

        ColumnConstraints rightColumn = new ColumnConstraints();
        rightColumn.setHgrow(Priority.ALWAYS);

        rootLayout.getColumnConstraints().addAll(leftColumn, rightColumn);
        rootLayout.setPadding(new Insets(10));
        rootLayout.setAlignment(Pos.TOP_CENTER);
        rootLayout.setHgap(10);
        rootLayout.setVgap(10);

        int rowid = 0;

        rootLayout.add(this.lblFilePath, 0, rowid); rootLayout.add(this.lblFilePathValue, 1, rowid); rowid++;

        rootLayout.add(this.lblActualSize, 0, rowid); rootLayout.add(this.lblActualSizeValue, 1, rowid); rowid++;

        rootLayout.add(this.lblLastMod, 0, rowid); rootLayout.add(this.lblLastModValue, 1, rowid); rowid++;

        rootLayout.add(this.lblSizeOnDisk, 0, rowid); rootLayout.add(this.lblSizeOnDiskValue, 1, rowid); rowid++;

        rootLayout.add(this.lblUUID, 0, rowid); rootLayout.add(this.lblUUIDValue, 1, rowid); rowid++;

        rootLayout.add(this.lblMediaType, 0, rowid);
        HBox mediaTypeRow = new HBox(10, this.lblMediaTypeValue, this.changeMediaTypeButton);
        mediaTypeRow.setMaxWidth(Double.MAX_VALUE);
        this.changeMediaTypeButton.setAlignment(Pos.CENTER_RIGHT);
        rootLayout.add(mediaTypeRow, 1, rowid); rowid++;

        HBox buttonbox = new HBox(10, this.closeButton, this.applyButton);
        buttonbox.setAlignment(Pos.BOTTOM_RIGHT);
        rootLayout.add(buttonbox, 0, rowid);
        GridPane.setColumnSpan(buttonbox, 2);

        return rootLayout;
    }

    @Override
    public void bindEvents()
    {
        this.closeButton.setOnAction(event -> this.getStage().close());

        this.changeMediaTypeButton.setOnAction(event -> {
            List<String> choices = new ArrayList<>(MediaType.ALL_TYPES);
            Collections.sort(choices);
            String newMT = QuickDialogs.pick(
                    "Choose a Media Type",
                    "Choose a new Media Type for this file. This will affect how the file is handled in this GUI and " +
                            "won't change the actual bytes of the file.",
                    null,
                    choices,
                    this.item.getMediaType()
            );

            if (newMT != null)
            {
                this.lblMediaTypeValue.setText(newMT);
                this.applyButton.setDisable(false);
            }
        });

        this.applyButton.setOnAction(event -> {
            this.item.setMediaType(this.lblMediaTypeValue.getText());
            this.onRefreshTreeItem.accept(this.item);
            this.onSaveInventoryRequest.accept("Modified media type for file " + this.item.getName());
            this.getStage().close();
        });
    }

    @Override
    public void applyStyling()
    {
        this.changeMediaTypeButton.getStyleClass().add("small-button");
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssCommon);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr - File Info");
        this.getStage().setScene(scene);
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        this.getStage().sizeToScene();
        this.getStage().setResizable(false);
        return scene;
    }

    public void setOnSaveInventoryRequest(Consumer<String> onSaveInventoryRequest)
    {
        this.onSaveInventoryRequest = onSaveInventoryRequest;
    }

    public void setOnRefreshTreeItem(Consumer<FileInventoryItem> onRefreshTreeItem)
    {
        this.onRefreshTreeItem = onRefreshTreeItem;
    }
}
