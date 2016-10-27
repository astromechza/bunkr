package org.bunkr.gui.windows;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bunkr.core.Resources;
import org.bunkr.core.Version;
import org.bunkr.core.utils.Logging;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created At: 2016-10-27
 */
public class AboutWindow extends BaseWindow
{
    private final String cssPath;

    private Label projectRepoLabel;
    private Hyperlink projectRepoValue;
    private Label versionLabel;
    private Label versionValue;
    private Label gitCommitDateLabel;
    private Label gitCommitDateValue;
    private Label gitCommitHashLabel;
    private Label gitCommitHashValue;
    private Label buildDateLabel;
    private Label buildDateValue;
    private Label compatVersionLabel;
    private Label compatVersionValue;

    public AboutWindow(Stage container) throws IOException
    {
        super(container);
        this.cssPath = Resources.getExternalPath("/resources/css/about_window.css");
        this.initialise();
    }

    public AboutWindow() throws IOException
    {
        this(new Stage());
    }

    @Override
    public void initControls()
    {
        this.projectRepoLabel = new Label("Project Repository");
        this.projectRepoValue = new Hyperlink("https://github.com/AstromechZA/bunkr");
        this.versionLabel = new Label("Version");
        this.versionValue = new Label(Version.versionString);
        this.compatVersionLabel = new Label("Compatible Version");
        this.compatVersionValue = new Label(Version.compatibleVersionString);
        this.gitCommitDateLabel = new Label("Git Commit Date");
        this.gitCommitDateValue = new Label(Version.gitDate);
        this.gitCommitHashLabel = new Label("Git Commit Hash");
        this.gitCommitHashValue = new Label(Version.gitHash);
        this.buildDateLabel = new Label("Build Date");
        this.buildDateValue = new Label(Version.builtDate);
    }

    @Override
    public Parent initLayout()
    {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10));

        int row = 0;
        grid.add(this.projectRepoLabel, 0, row); grid.add(this.projectRepoValue, 1, row); row++;
        grid.add(this.versionLabel, 0, row); grid.add(this.versionValue, 1, row); row++;
        grid.add(this.compatVersionLabel, 0, row); grid.add(this.compatVersionValue, 1, row); row++;
        grid.add(this.gitCommitDateLabel, 0, row); grid.add(this.gitCommitDateValue, 1, row); row++;
        grid.add(this.gitCommitHashLabel, 0, row); grid.add(this.gitCommitHashValue, 1, row); row++;
        grid.add(this.buildDateLabel, 0, row); grid.add(this.buildDateValue, 1, row); row++;

        for (int i = 0; i < row; i++)
        {
            RowConstraints rc = new RowConstraints();
            rc.setPrefHeight(22);
            grid.getRowConstraints().add(rc);
        }

        return grid;
    }

    @Override
    public void bindEvents()
    {
        this.projectRepoValue.setOnAction(t -> {
            try
            {
                Desktop.getDesktop().browse(new URI(this.projectRepoValue.getText()));
            }
            catch (IOException | URISyntaxException e)
            {
                QuickDialogs.exception(e);
            }
        });
    }

    @Override
    public void applyStyling()
    {
        this.projectRepoLabel.getStyleClass().add("row-label");
        this.projectRepoValue.getStyleClass().add("row-hyperlink");
        this.versionLabel.getStyleClass().add("row-label");
        this.versionValue.getStyleClass().add("row-value");
        this.compatVersionLabel.getStyleClass().add("row-label");
        this.compatVersionValue.getStyleClass().add("row-value");
        this.gitCommitDateLabel.getStyleClass().add("row-label");
        this.gitCommitDateValue.getStyleClass().add("row-value");
        this.gitCommitHashLabel.getStyleClass().add("row-label");
        this.gitCommitHashValue.getStyleClass().add("row-value");
        this.buildDateLabel.getStyleClass().add("row-label");
        this.buildDateValue.getStyleClass().add("row-value");
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssCommon);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr - About");
        this.getStage().setMinWidth(400);
        this.getStage().setScene(scene);
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        this.getStage().setResizable(false);
        return scene;
    }
}
