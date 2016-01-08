package org.bunkr.gui.windows;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
 * Creator: benmeier
 * Created At: 2015-12-29
 */
public class FileInfoWindow extends BaseWindow
{
    private static final int WINDOW_WIDTH = 400;

    private final String cssPath;
    private final FileInventoryItem item;
    private final String filePath;

    // components
    private Label lblFilePath; SelectableLabel lblFilePathValue;
    private Label lblActualSize; SelectableLabel lblActualSizeValue;
    private Label lblLastMod; SelectableLabel lblLastModValue;
    private Label lblSizeOnDisk; SelectableLabel lblSizeOnDiskValue;
    private Label lblUUID; SelectableLabel lblUUIDValue;
    private Label lblTags;
    private Label lblMediaType, lblMediaTypeValue;
    private Button closeButton, applyButton, changeMediaTypeButton, addTagButton, removeTagButton;
    private ListView<String> tagsBox;

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

        this.lblTags = new Label("Tags:");
        this.tagsBox = new ListView<>();
        this.tagsBox.setPlaceholder(new Label("< No Tags >"));
        ObservableList<String> tagItems = FXCollections.observableArrayList(this.item.getTags());
        this.tagsBox.setItems(tagItems);

        this.addTagButton = Icons.buildIconButton("", Icons.ICON_PLUS);
        this.removeTagButton = Icons.buildIconButton("", Icons.ICON_MINUS);

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

        ColumnConstraints leftColum = new ColumnConstraints();
        leftColum.setHgrow(Priority.ALWAYS);

        ColumnConstraints rightColum = new ColumnConstraints();
        rightColum.setHgrow(Priority.ALWAYS);

        rootLayout.getColumnConstraints().addAll(leftColum, rightColum);
        rootLayout.setPadding(new Insets(10));
        rootLayout.setAlignment(Pos.CENTER);
        rootLayout.setHgap(10);
        rootLayout.setVgap(10);

        int rowid = 0;

        rootLayout.add(this.lblFilePath, 0, rowid); rootLayout.add(this.lblFilePathValue, 1, rowid); rowid++;

        rootLayout.add(this.lblActualSize, 0, rowid); rootLayout.add(this.lblActualSizeValue, 1, rowid); rowid++;

        rootLayout.add(this.lblLastMod, 0, rowid); rootLayout.add(this.lblLastModValue, 1, rowid); rowid++;

        rootLayout.add(this.lblTags, 0, rowid); rowid++;
        rootLayout.add(this.tagsBox, 0, rowid); rowid++;
        GridPane.setVgrow(this.tagsBox, Priority.ALWAYS);
        GridPane.setColumnSpan(this.tagsBox, 2);

        HBox tagButtonBox = new HBox(this.addTagButton, this.removeTagButton);
        tagButtonBox.setAlignment(Pos.CENTER_RIGHT);
        rootLayout.add(tagButtonBox, 0, rowid); rowid++;
        GridPane.setColumnSpan(tagButtonBox, 2);

        rootLayout.add(this.lblSizeOnDisk, 0, rowid); rootLayout.add(this.lblSizeOnDiskValue, 1, rowid); rowid++;

        rootLayout.add(this.lblUUID, 0, rowid); rootLayout.add(this.lblUUIDValue, 1, rowid); rowid++;

        rootLayout.add(this.lblMediaType, 0, rowid);
        HBox mediaTypeRow = new HBox(10, this.lblMediaTypeValue, this.changeMediaTypeButton);
        mediaTypeRow.setAlignment(Pos.CENTER_RIGHT);
        rootLayout.add(mediaTypeRow, 1, rowid); rowid++;

        HBox buttonbox = new HBox(10, this.closeButton, this.applyButton);
        buttonbox.setAlignment(Pos.CENTER_RIGHT);
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
            this.item.setTags(new HashSet<>(this.tagsBox.getItems()));
            this.onRefreshTreeItem.accept(this.item);
            this.onSaveInventoryRequest.accept("Modified media type for file " + this.item.getName());
            this.getStage().close();
        });

        this.addTagButton.setOnAction(event -> {
            String tag = QuickDialogs.input("Enter a new tag", "");
            if (tag != null)
            {
                try
                {
                    this.item.validateTag(tag);
                    this.tagsBox.getItems().add(tag);
                    this.applyButton.setDisable(false);
                }
                catch (IllegalArgumentException e)
                {
                    QuickDialogs.error("Tag %s is not a valid tag: '%s'", tag, e.getLocalizedMessage());
                }
            }
        });

        this.removeTagButton.setOnAction(event -> {
            // get selected item
            String tag = this.tagsBox.getSelectionModel().getSelectedItem();
            if (tag != null)
            {
                this.tagsBox.getItems().remove(tag);
                this.applyButton.setDisable(false);
            }
        });
    }

    @Override
    public void applyStyling()
    {
        this.changeMediaTypeButton.getStyleClass().add("small-button");
        this.addTagButton.getStyleClass().add("small-button");
        this.addTagButton.getStyleClass().add("btnbar-left");
        this.removeTagButton.getStyleClass().add("small-button");
        this.removeTagButton.getStyleClass().add("btnbar-right");
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
        this.getStage().setWidth(500);
        this.getStage().setHeight(400);
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
