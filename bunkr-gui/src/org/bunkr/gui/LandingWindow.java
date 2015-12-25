package org.bunkr.gui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bunkr.core.Resources;
import org.bunkr.core.Version;

import java.io.File;
import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-24
 */
public class LandingWindow extends BaseWindow
{
    private static final int WINDOW_WIDTH = 400, WINDOW_HEIGHT = 300;

    private final String cssPath, logoPath;
    private Button newArchiveButton, openArchiveButton;
    private Label versionLabel;
    private ImageView logoImage;

    public LandingWindow(Stage container) throws IOException
    {
        super(container);
        this.cssPath = Resources.getExternalPath("/resources/css/open_archive_window.css");
        this.logoPath = Resources.getExternalPath("/resources/images/bunkr-logo-200x200.png");
        this.initialise();
        this.getStage().show();
    }

    @Override
    void initControls()
    {
        this.newArchiveButton = new Button("New Archive");
        this.openArchiveButton = new Button("Open Archive");
        this.versionLabel = new Label(String.format("Version: %s (%s %s)",
                                                    Version.versionString,
                                                    Version.gitDate,
                                                    Version.gitHash.substring(0, 8)));

        this.logoImage = new ImageView(this.logoPath);
    }

    @Override
    Parent initLayout()
    {
        GridPane rootLayout = new GridPane();

        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setFillWidth(true);
        columnConstraints.setHgrow(Priority.ALWAYS);

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setFillHeight(true);
        rowConstraints.setVgrow(Priority.ALWAYS);

        rootLayout.getColumnConstraints().add(columnConstraints);
        rootLayout.getRowConstraints().add(rowConstraints);
        rootLayout.setPadding(new Insets(10));
        rootLayout.setAlignment(Pos.CENTER);
        rootLayout.setHgap(10);
        rootLayout.setVgap(10);

        this.logoImage.setPreserveRatio(true);
        this.logoImage.setFitHeight(120);
        rootLayout.add(this.logoImage, 0, 0);
        GridPane.setHalignment(this.logoImage, HPos.CENTER);

        this.newArchiveButton.setPrefWidth(180);
        rootLayout.add(this.newArchiveButton, 0, 1);
        GridPane.setHalignment(this.newArchiveButton, HPos.CENTER);

        this.openArchiveButton.setPrefWidth(180);
        rootLayout.add(this.openArchiveButton, 0, 2);
        GridPane.setHalignment(this.openArchiveButton, HPos.CENTER);

        rootLayout.add(this.versionLabel, 0, 6);
        GridPane.setHalignment(this.versionLabel, HPos.LEFT);

        return rootLayout;
    }

    @Override
    void bindEvents()
    {
        this.newArchiveButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Archive ...");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Bunkr Archives", "*.bunkr"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));
            File selectedPath = fileChooser.showSaveDialog(LandingWindow.this.getStage());
            if (selectedPath != null)
            {
                // display new archive window
                // - show options for password
                // - show options for encryption
                // - show cancel/create buttons
                try
                {
                    new NewArchiveSettingsWindow();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

        this.openArchiveButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Archive ...");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Bunkr Archives", "*.bunkr"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));
            File selectedPath = fileChooser.showOpenDialog(LandingWindow.this.getStage());
            if (selectedPath != null)
            {
                // display open archive window
                // try open archive
                // - catch password requirement
                // if password
            }
        });
    }

    @Override
    void applyStyling()
    {
        this.versionLabel.setId("version-label");
        this.getRootLayout().getStyleClass().add("background");
    }

    @Override
    Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr");
        this.getStage().setScene(scene);
        this.getStage().setResizable(true);
        return scene;
    }
}
