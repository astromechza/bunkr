package org.bunkr.gui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.bunkr.core.Resources;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-24
 */
public class OpenArchiveWindow extends BaseWindow
{
    public final String cssPath;

    public OpenArchiveWindow(Stage container) throws IOException
    {
        super(container);
        this.cssPath = Resources.getExternalPath("/resources/css/open_archive_window.css");
        this.initialise();
        this.getStage().show();
    }

    @Override
    void initControls()
    {

    }

    @Override
    Parent initLayout()
    {
        return new BorderPane();
    }

    @Override
    void bindEvents()
    {

    }

    @Override
    void applyStyling()
    {

    }

    @Override
    Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout(), 800, 600);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr Archiving");
        this.getStage().setScene(scene);
        this.getStage().setResizable(true);
        return scene;
    }
}
