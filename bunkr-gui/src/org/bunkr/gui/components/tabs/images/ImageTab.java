package org.bunkr.gui.components.tabs.images;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import org.bunkr.gui.components.tabs.IOpenedFileTab;
import org.bunkr.gui.components.tabs.TabLoadError;
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
    private Button zoomInButton;
    private Button zoomResetButton;
    private Button zoomOutButton;
    private Label resolutionLabel;
    private ImageView imageView;
    private ScrollPane scrollPane;

    private static final double zoomStep = 1.1;
    private final DoubleProperty zoomRatio = new SimpleDoubleProperty(1);

    public ImageTab(FileInventoryItem file, ArchiveInfoContext archive) throws TabLoadError
    {
        super();
        this.file = file;
        this.archive = archive;

        this.initControls();
        this.bindEvents();
        this.loadContent();
    }

    private void bindEvents()
    {
        // Bind the actual zoom action in response to changing the zoom value.
        // This is complicated by the fact that the scroll bar positions must be recalculated to maintain the same
        // center of view. Some pretty fun math here.
        this.zoomRatio.addListener((observable, oldValue, newValue) -> {
            double Ix = this.imageView.getImage().getWidth();
            double Iy = this.imageView.getImage().getHeight();
            double Fx1 = this.imageView.getFitWidth();
            if (Fx1 == 0.0) Fx1 = Ix;
            double Fy1 = this.imageView.getFitHeight();
            if (Fy1 == 0.0) Fy1 = Iy;
            double Sx = this.scrollPane.getWidth();
            double Sy = this.scrollPane.getHeight();
            double Hx1 = this.scrollPane.getHvalue();
            double Hy1 = this.scrollPane.getVvalue();
            double Cx = ((Fx1 - Sx) * Hx1 + Sx / 2) / Fx1;
            double Cy = ((Fy1 - Sy) * Hy1 + Sy / 2) / Fy1;
            double Fx2 = Ix * newValue.doubleValue();
            double Fy2 = Iy * newValue.doubleValue();
            this.imageView.setFitWidth(Fx2);
            this.imageView.setFitHeight(Fy2);
            double Hx2 = (Cx * Fx2 - 0.5 * Sx) / (Fx2 - Sx);
            double Hy2 = (Cy * Fy2 - 0.5 * Sy) / (Fy2 - Sy);
            this.scrollPane.setHvalue(Hx2);
            this.scrollPane.setVvalue(Hy2);

            // update info label
            this.updateInfoLabel();
        });

        // Bind actual zoom buttons.
        this.zoomInButton.setOnAction(event -> zoomRatio.setValue(zoomRatio.get() * zoomStep));
        this.zoomResetButton.setOnAction(event -> this.fitImageToPane());
        this.zoomOutButton.setOnAction(event -> zoomRatio.setValue(zoomRatio.get() / zoomStep));

        // This listener is a bit silly, the goal is to fit the image to the scroll pane when the layout is first
        // calculated. The best way I found to do this was to watch the size of the scroll pane when it changes from 0.
        this.scrollPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue.intValue() == 0 && newValue.intValue() > 10) this.fitImageToPane();
        });
    }

    private void fitImageToPane()
    {
        if (this.imageView.getImage() != null)
        {
            double suitableZoom = 1;
            double xValue = this.scrollPane.getWidth() / this.imageView.getImage().getWidth();
            double yValue = this.scrollPane.getHeight() / this.imageView.getImage().getHeight();
            suitableZoom = Math.min(suitableZoom, Math.min(xValue, yValue));
            this.zoomRatio.set(suitableZoom);
            this.scrollPane.setHvalue(0.5);
            this.scrollPane.setVvalue(0.5);
        }
    }

    private void updateInfoLabel()
    {
        if (this.imageView.getImage() != null)
        {
            if (this.imageView.getImage().getWidth() > 0 && this.imageView.getImage().getHeight() > 0)
            {
                this.resolutionLabel.setText(String.format(
                        "%dpx x %dpx @ %d%%",
                        (int) this.imageView.getImage().getWidth(),
                        (int) this.imageView.getImage().getHeight(),
                        (int) (this.zoomRatio.get() * 100)
                ));
            }
        }
    }

    private void initControls()
    {
        this.setText(this.file.getAbsolutePath());

        this.zoomInButton = new Button("+");
        this.zoomInButton.getStyleClass().add("btnbar-left");
        this.zoomInButton.setFocusTraversable(false);
        this.zoomResetButton = new Button(".");
        this.zoomResetButton.getStyleClass().add("btnbar-middle");
        this.zoomResetButton.setFocusTraversable(false);
        this.zoomOutButton = new Button("-");
        this.zoomOutButton.getStyleClass().add("btnbar-right");
        this.zoomOutButton.setFocusTraversable(false);

        this.imageView = new ImageView();
        this.imageView.preserveRatioProperty().set(true);
        this.imageView.setCursor(Cursor.HAND);
        this.imageView.setSmooth(false);

        this.resolutionLabel = new Label();

        this.scrollPane = new ScrollPane();
        this.scrollPane.setContent(this.imageView);
        VBox.setVgrow(this.scrollPane, Priority.ALWAYS);
        this.scrollPane.setMaxWidth(Double.MAX_VALUE);
        this.scrollPane.setMaxHeight(Double.MAX_VALUE);

        this.setContent(new VBox(new ToolBar(new HBox(this.zoomInButton, this.zoomResetButton, this.zoomOutButton), this.resolutionLabel), this.scrollPane));

        this.getStyleClass().add("open-file-tab");
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void loadContent() throws TabLoadError
    {
        Image loadingImage;
        // Construct the image from the block stream.
        try (MultilayeredInputStream ms = new MultilayeredInputStream(this.archive, this.file))
        {
            loadingImage = new Image(ms);
        }
        catch (IOException e)
        {
            // These exceptions will be related to actually reading the bytes from disk.
            QuickDialogs.exception(e);
            throw new TabLoadError(e);
        }

        // Have to catch this error outside of the try catch because we need to make sure the stream has closed.
        if (loadingImage.isError())
        {
            throw new TabLoadError(
                    "Error: '%s' occured. This may be due to a corrupted image, or an incorrect media type. " +
                            "Change the media type using Context Menu > Info.",
                    loadingImage.getException().getLocalizedMessage()
            );
        }
        this.imageView.setImage(loadingImage);
    }

    @Override
    public void notifyRename()
    {
        this.setText(this.file.getAbsolutePath());
    }
}
