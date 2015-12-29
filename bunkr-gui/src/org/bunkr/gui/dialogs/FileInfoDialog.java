package org.bunkr.gui.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
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
import org.bunkr.core.utils.Formatters;
import org.bunkr.gui.components.treeview.SelectableLabel;
import org.bunkr.gui.windows.BaseWindow;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-29
 */
public class FileInfoDialog extends BaseWindow
{
    private static final int WINDOW_WIDTH = 400;

    private final String cssPath;
    private final FileInventoryItem item;
    private final String filePath;

    private Label lblFilePath, lblActualSize, lblLastMod, lblTags, lblSizeOnDisk, lblUUID;
    private SelectableLabel lblFilePathValue, lblActualSizeValue, lblLastModValue, lblSizeOnDiskValue, lblUUIDValue;
    private Button closeButton;

    private ListView<String> tagsBox;

    public FileInfoDialog(FileInventoryItem item, String filePath) throws IOException
    {
        super();
        this.filePath = filePath;
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
        this.lblActualSizeValue = new SelectableLabel(Formatters.formatPrettyInt(this.item.getActualSize()));
        Tooltip.install(this.lblActualSizeValue, new Tooltip(String.format("%d Bytes", this.item.getActualSize())));

        this.lblLastMod = new Label("Last Modified:");
        this.lblLastModValue = new SelectableLabel(Formatters.formatPrettyDate(this.item.getModifiedAt()));
        Tooltip.install(this.lblLastModValue, new Tooltip(Formatters.formatIso8601utc(this.item.getModifiedAt())));

        this.lblTags = new Label("Tags:");
        this.tagsBox = new ListView<>();
        this.tagsBox.setPlaceholder(new Label("< No Tags >"));
        ObservableList<String> tagItems = FXCollections.observableArrayList(this.item.getTags());
        this.tagsBox.setItems(tagItems);

        this.lblSizeOnDisk = new Label("Size on Disk:");
        this.lblSizeOnDiskValue = new SelectableLabel(Formatters.formatPrettyInt(this.item.getSizeOnDisk()));
        Tooltip.install(this.lblSizeOnDiskValue, new Tooltip(String.format("%d Bytes", this.item.getSizeOnDisk())));

        this.lblUUID = new Label("File UUID:");
        this.lblUUIDValue = new SelectableLabel(this.item.getUuid().toString());

        this.closeButton = new Button("Close");
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

        rootLayout.add(this.lblFilePath, 0, 0); rootLayout.add(this.lblFilePathValue, 1, 0);
        rootLayout.add(this.lblActualSize, 0, 1); rootLayout.add(this.lblActualSizeValue, 1, 1);
        rootLayout.add(this.lblLastMod, 0, 2); rootLayout.add(this.lblLastModValue, 1, 2);
        rootLayout.add(this.lblTags, 0, 3);
        rootLayout.add(this.tagsBox, 0, 4);
        GridPane.setVgrow(this.tagsBox, Priority.ALWAYS);
        GridPane.setColumnSpan(this.tagsBox, 2);
        rootLayout.add(this.lblSizeOnDisk, 0, 5); rootLayout.add(this.lblSizeOnDiskValue, 1, 5);
        rootLayout.add(this.lblUUID, 0, 6); rootLayout.add(this.lblUUIDValue, 1, 6);

        rootLayout.add(this.closeButton, 1, 7);
        GridPane.setHalignment(this.closeButton, HPos.RIGHT);

        return rootLayout;
    }

    @Override
    public void bindEvents()
    {
        this.closeButton.setOnAction(event -> this.getStage().close());
    }

    @Override
    public void applyStyling()
    {

    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr - File Info");
        this.getStage().setScene(scene);
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        this.getStage().setWidth(500);
        this.getStage().setHeight(330);
        this.getStage().setResizable(false);
        return scene;
    }
}
