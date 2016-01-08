package org.bunkr.gui.windows;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.bunkr.core.Resources;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-24
 */
public abstract class BaseWindow
{
    protected final String cssCommon;

    private final Stage stage;
    private Scene scene;
    private Parent rootLayout;

    public BaseWindow(Stage container) throws IOException
    {
        this.stage = container;
        this.cssCommon = Resources.getExternalPath("/resources/css/common.css");
    }

    public BaseWindow() throws IOException
    {
        this(new Stage());
    }

    public void initialise()
    {
        this.initControls();
        this.rootLayout = this.initLayout();
        this.bindEvents();
        this.applyStyling();
        this.scene = this.initScene();
    }

    public abstract void initControls();
    public abstract Parent initLayout();
    public abstract void bindEvents();
    public abstract void applyStyling();
    public abstract Scene initScene();

    public Stage getStage()
    {
        return stage;
    }

    public Scene getScene()
    {
        return scene;
    }

    public Parent getRootLayout()
    {
        return rootLayout;
    }
}