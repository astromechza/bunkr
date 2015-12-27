package org.bunkr.gui.windows;

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
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.Resources;
import org.bunkr.core.Version;
import org.bunkr.core.descriptor.DescriptorBuilder;
import org.bunkr.core.descriptor.IDescriptor;
import org.bunkr.core.descriptor.PBKDF2Descriptor;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.gui.dialogs.PasswordDialog;
import org.bunkr.gui.dialogs.QuickDialogs;

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
    }

    @Override
    public void initControls()
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
    public Parent initLayout()
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
    public void bindEvents()
    {
        this.newArchiveButton.setOnAction(event -> {
            try
            {
                new NewArchiveSettingsWindow(this.getStage()).getStage().show();
                this.getStage().hide();
            }
            catch (IOException e)
            {
                QuickDialogs.exception(e);
                this.getStage().show();
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
                try
                {
                    IDescriptor descriptor = DescriptorBuilder.fromFile(selectedPath);
                    UserSecurityProvider usp;

                    if (descriptor instanceof PlaintextDescriptor)
                    {
                        usp = new UserSecurityProvider();
                    }
                    else if (descriptor instanceof PBKDF2Descriptor)
                    {
                        PasswordProvider passProv = new PasswordProvider();
                        PasswordDialog dialog = new PasswordDialog();
                        dialog.getStage().showAndWait();
                        if (dialog.hasFile())
                        {
                            passProv.setArchivePassword(dialog.getFile());
                        }
                        else if (dialog.hasPassword())
                        {
                            passProv.setArchivePassword(dialog.getPassword());
                        }
                        else
                        {
                            return;
                        }

                        usp = new UserSecurityProvider(passProv);
                    }
                    else
                    {
                        throw new BaseBunkrException("Archive Open is not implemented for Descriptor type %s", descriptor.getIdentifier());
                    }

                    ArchiveInfoContext archive = new ArchiveInfoContext(selectedPath, usp);
                    new MainWindow(archive).getStage().show();
                    this.getStage().close();
                }
                catch (Exception e)
                {
                    QuickDialogs.exception(e);
                    this.getStage().show();
                }
            }
        });
    }

    @Override
    public void applyStyling()
    {
        this.versionLabel.setId("version-label");
        this.getRootLayout().getStyleClass().add("background");
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr");
        this.getStage().setScene(scene);
        this.getStage().setResizable(true);
        return scene;
    }
}
