package org.bunkr.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import org.bunkr.gui.windows.LandingWindow;

/**
 * Creator: benmeier
 * Created At: 2015-12-24
 */
public class GuiEntryPoint extends Application
{
    public static void main(String[] args)
    {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        new LandingWindow(primaryStage).getStage().show();
    }
}
