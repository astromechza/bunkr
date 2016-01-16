package org.bunkr.gui.windows;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2016-01-16
 */
public class SearchWindow extends BaseWindow
{
    public SearchWindow() throws IOException
    {
        super();
        this.initialise();
    }

    @Override
    public void initControls()
    {

    }

    @Override
    public Parent initLayout()
    {
        return new BorderPane();
    }

    @Override
    public void bindEvents()
    {

    }

    @Override
    public void applyStyling()
    {

    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssCommon);
        this.getStage().setTitle("Bunkr - Search");
        this.getStage().setScene(scene);
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        this.getStage().setWidth(500);
        this.getStage().setHeight(400);
        this.getStage().setResizable(false);
        return scene;
    }
}
