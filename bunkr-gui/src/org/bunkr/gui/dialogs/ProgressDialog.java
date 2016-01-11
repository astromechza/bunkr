package org.bunkr.gui.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.bunkr.gui.windows.BaseWindow;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Creator: benmeier
 * Created At: 2016-01-11
 */
public class ProgressDialog extends BaseWindow
{
    private Label statusLabel;
    private ProgressBar progressBar;

    private Consumer<String> onTryCancelRequest = null;

    public ProgressDialog() throws IOException
    {
        super();
        this.initialise();
    }

    @Override
    public void initControls()
    {
        this.statusLabel = new Label("In Progress...");
        this.progressBar = new ProgressBar(0);
    }

    @Override
    public Parent initLayout()
    {
        BorderPane root = new BorderPane();
        root.setTop(this.statusLabel);
        root.setPadding(new Insets(10));
        root.setCenter(this.progressBar);
        return root;
    }

    @Override
    public void bindEvents()
    {
        this.getStage().setOnCloseRequest(e -> {
            if (onTryCancelRequest != null) onTryCancelRequest.accept("Progress Close Requested.");
        });
    }

    @Override
    public void applyStyling()
    {
        this.statusLabel.setMaxWidth(Double.MAX_VALUE);
        this.statusLabel.setAlignment(Pos.CENTER);
        this.progressBar.setMaxWidth(Double.MAX_VALUE);
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout(), 400, 100);
        scene.getStylesheets().add(this.cssCommon);
        this.getStage().setResizable(false);
        this.getStage().setTitle("Bunkr - Progress");
        this.getStage().setScene(scene);
        this.getStage().initStyle(StageStyle.UTILITY);
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        return scene;
    }

    public void setProgress(float p)
    {
        this.progressBar.setProgress(p);
    }

    public void setStatus(String s)
    {
        this.statusLabel.setText(s);
    }

    public void setOnTryCancelRequest(Consumer<String> onTryCancelRequest)
    {
        this.onTryCancelRequest = onTryCancelRequest;
    }
}
