package org.bunkr.gui.windows;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import org.bunkr.core.Resources;

import java.io.IOException;

/**
 * Created At: 2016-10-30
 */
public class BlockDistributionWindow extends BaseWindow
{
    private final String cssPath;
    private ImageView imageView;
    private BorderPane imagePanel;

    public BlockDistributionWindow() throws IOException
    {
        super();
        this.cssPath = Resources.getExternalPath("/resources/css/block_distrib.css");
        this.initialise();
    }

    @Override
    public void initControls()
    {
        this.imagePanel = new BorderPane();
        this.imageView = new ImageView();
    }

    @Override
    public Parent initLayout()
    {
        BorderPane root = new BorderPane();

        root.setMaxWidth(Double.MAX_VALUE);
        root.setMaxHeight(Double.MAX_VALUE);
        root.setPadding(new Insets(10));

        imagePanel.setCenter(imageView);

        root.setCenter(imagePanel);

        return root;
    }

    @Override
    public void bindEvents()
    {

    }

    @Override
    public void applyStyling()
    {
        this.imagePanel.getStyleClass().addAll("block-image");
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssCommon);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr - Block Distribution");
        this.getStage().setMinWidth(800);
        this.getStage().setMinHeight(600);
        this.getStage().setScene(scene);
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        this.getStage().setResizable(true);
        return scene;
    }
}
