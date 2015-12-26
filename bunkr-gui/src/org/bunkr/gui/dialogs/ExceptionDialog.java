package org.bunkr.gui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import org.bunkr.gui.windows.BaseWindow;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Creator: benmeier
 * Created At: 2015-12-26
 */
public class ExceptionDialog extends BaseWindow
{
    private static final int WINDOW_WIDTH = 400, WINDOW_HEIGHT = 300;

    private final String message;
    private final Throwable exception;
    private TextArea tracebackBox;
    private Label messageLabel;

    public ExceptionDialog(String message, Throwable e)
    {
        super();
        this.message = message;
        this.exception = e;
        this.initialise();
    }

    public ExceptionDialog(Throwable e)
    {
        this(e.getMessage(), e);
    }

    @Override
    public void initControls()
    {
        this.messageLabel = new Label(this.message);
        this.tracebackBox = new TextArea();
        this.tracebackBox.setEditable(false);
        StringWriter sw = new StringWriter();
        this.exception.printStackTrace(new PrintWriter(sw));
        this.tracebackBox.setText(sw.toString());
    }

    @Override
    public Parent initLayout()
    {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));
        layout.setTop(this.messageLabel);
        layout.setCenter(this.tracebackBox);
        return layout;
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
        Scene scene = new Scene(this.getRootLayout(), WINDOW_WIDTH, WINDOW_HEIGHT);
        this.getStage().setTitle("Bunkr - An Exception Occured");
        this.getStage().setScene(scene);
        this.getStage().setResizable(true);
        return scene;
    }
}
