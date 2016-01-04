package org.bunkr.gui.components.tabs.images;

import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
    private Button zoomInButton;
    private Button zoomOutButton;
    private Label resolutionLabel;
    private ImageView imageView;
    private ScrollPane scrollPane;

    private final double zoomRatio = 1.2;

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
        this.zoomInButton.setOnAction(event -> {
            double Fx1 = this.imageView.getFitWidth();
            if (Fx1 == 0.0) Fx1 = this.imageView.getImage().getWidth();
            double Fy1 = this.imageView.getFitHeight();
            if (Fy1 == 0.0) Fy1 = this.imageView.getImage().getHeight();
            double Sx = this.scrollPane.getWidth();
            double Sy = this.scrollPane.getHeight();
            double Hx1 = this.scrollPane.getHvalue();
            double Hy1 = this.scrollPane.getVvalue();

            double Cx = ((Fx1 - Sx) * Hx1 + Sx / 2) / Fx1;
            double Cy = ((Fy1 - Sy) * Hy1 + Sy / 2) / Fy1;

            double Fx2 = Fx1 * zoomRatio;
            double Fy2 = Fy1 * zoomRatio;
            this.imageView.setFitWidth(Fx2);
            this.imageView.setFitHeight(Fy2);

            double Hx2 = (Cx * Fx2 - 0.5 * Sx) / (Fx2 - Sx);
            double Hy2 = (Cy * Fy2 - 0.5 * Sy) / (Fy2 - Sy);

            this.scrollPane.setHvalue(Hx2);
            this.scrollPane.setVvalue(Hy2);
        });

        this.zoomOutButton.setOnAction(event -> {
            double Fx1 = this.imageView.getFitWidth();
            if (Fx1 == 0.0) Fx1 = this.imageView.getImage().getWidth();
            double Fy1 = this.imageView.getFitHeight();
            if (Fy1 == 0.0) Fy1 = this.imageView.getImage().getHeight();
            double Sx = this.scrollPane.getWidth();
            double Sy = this.scrollPane.getHeight();
            double Hx1 = this.scrollPane.getHvalue();
            double Hy1 = this.scrollPane.getVvalue();

            double Cx = ((Fx1 - Sx) * Hx1 + Sx / 2) / Fx1;
            double Cy = ((Fy1 - Sy) * Hy1 + Sy / 2) / Fy1;

            double Fx2 = Fx1 / zoomRatio;
            double Fy2 = Fy1 / zoomRatio;
            this.imageView.setFitWidth(Fx2);
            this.imageView.setFitHeight(Fy2);

            double Hx2 = (Cx * Fx2 - 0.5 * Sx) / (Fx2 - Sx);
            double Hy2 = (Cy * Fy2 - 0.5 * Sy) / (Fy2 - Sy);

            this.scrollPane.setHvalue(Hx2);
            this.scrollPane.setVvalue(Hy2);
        });

        this.imageView.imageProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
            {
                this.resolutionLabel.setText(String.format(
                        "%dpx x %dpx",
                        (int) this.imageView.getImage().getWidth(),
                        (int) this.imageView.getImage().getHeight()
                ));
                this.scrollPane.setHvalue(0.5);
                this.scrollPane.setVvalue(0.5);
            }
        });

        this.scrollPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue.intValue() == 0 && newValue.intValue() > 10)
            {
                this.imageView.setFitWidth(this.scrollPane.getWidth() - 2);
                this.imageView.setFitHeight(this.scrollPane.getHeight() - 2);
            }
        });
    }

    private void initControls()
    {
        this.setText(this.file.getAbsolutePath());

        this.zoomInButton = new Button("+");
        this.zoomOutButton = new Button("-");

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

        this.setContent(new VBox(new ToolBar(this.zoomInButton, this.zoomOutButton, this.resolutionLabel), this.scrollPane));

        this.getStyleClass().add("open-file-tab");
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
