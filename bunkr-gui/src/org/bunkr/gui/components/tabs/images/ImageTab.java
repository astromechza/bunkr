package org.bunkr.gui.components.tabs.images;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import org.bunkr.gui.components.tabs.IOpenedFileTab;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2016-01-03
 */
public class ImageTab extends Tab implements IOpenedFileTab
{
    private final FileInventoryItem file;
    private final ArchiveInfoContext archive;

    // components
    private ImageView imageView;
    private ScrollPane scrollPane;

    private final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

    public ImageTab(FileInventoryItem file, ArchiveInfoContext archive)
    {
        super();
        this.file = file;
        this.archive = archive;

        this.initControls();
        this.bindEvents();
        this.reloadContent();
    }

    private void bindEvents()
    {
        zoomProperty.addListener(arg0 -> {
            imageView.setFitWidth(zoomProperty.get() * 4);
            imageView.setFitHeight(zoomProperty.get() * 3);
        });

        scrollPane.addEventFilter(ScrollEvent.ANY, event -> {
            if (event.getDeltaY() > 0) {
                zoomProperty.set(zoomProperty.get() * 1.1);
            } else if (event.getDeltaY() < 0) {
                zoomProperty.set(zoomProperty.get() / 1.1);
            }
        });
    }

    private void initControls()
    {
        this.setText(this.file.getAbsolutePath());
        this.imageView = new ImageView();
        this.imageView.preserveRatioProperty().set(true);
        this.scrollPane = new ScrollPane();
        this.scrollPane.setContent(this.imageView);
        this.setContent(this.scrollPane);
    }

    private void reloadContent()
    {
        try (MultilayeredInputStream ms = new MultilayeredInputStream(this.archive, this.file))
        {
            this.imageView.setImage(new Image(ms));
        }
        catch (IOException e)
        {
            QuickDialogs.exception(e);
        }
    }

    @Override
    public void notifyRename()
    {
        this.setText(this.file.getAbsolutePath());
    }
}
