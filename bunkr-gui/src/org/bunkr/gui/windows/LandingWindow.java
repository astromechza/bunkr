/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.gui.windows;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bunkr.core.*;
import org.bunkr.core.descriptor.*;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.usersec.PasswordProvider;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.gui.Icons;
import org.bunkr.gui.dialogs.PasswordDialog;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.File;
import java.io.IOException;

/**
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
        this.newArchiveButton = Icons.buildIconButton("New Archive", Icons.ICON_NEW);
        this.openArchiveButton = Icons.buildIconButton("Open Archive", Icons.ICON_OPEN);
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
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Archive ...");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Bunkr Archives", "*.bunkr", "*.bnkr"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));
                File selectedPath = fileChooser.showSaveDialog(this.getStage());
                if (selectedPath == null) return;

                this.getStage().getScene().setCursor(Cursor.WAIT);

                PasswordProvider passProv = new PasswordProvider();
                UserSecurityProvider usp = new UserSecurityProvider(passProv);

                ArchiveInfoContext archive = ArchiveBuilder.createNewEmptyArchive(
                        selectedPath,
                        new PlaintextDescriptor(),
                        usp
                );

                try
                {
                    ArchiveSecurityWindow popup = new ArchiveSecurityWindow(archive, usp);
                    popup.setOnSaveMetadataRequest(r -> {
                        try
                        {
                            MetadataWriter.write(archive, usp);
                        }
                        catch (IOException | BaseBunkrException e)
                        {
                            QuickDialogs.exception(e);
                        }
                    });
                    popup.getStage().showAndWait();
                }
                catch (IOException e)
                {
                    QuickDialogs.exception(e);
                }

                new MainWindow(archive, usp).getStage().show();
                this.getStage().close();
            }
            catch (BaseBunkrException | IOException e)
            {
                QuickDialogs.exception(e);
            }
            finally
            {
                this.getStage().getScene().setCursor(Cursor.DEFAULT);
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
                tryOpen(selectedPath);
            }
        });
    }

    public void tryOpen(File path)
    {
        try
        {
            IDescriptor descriptor = DescriptorBuilder.fromFile(path);
            PasswordProvider passProv = new PasswordProvider();
            UserSecurityProvider usp = new UserSecurityProvider(passProv);

            if (descriptor instanceof PlaintextDescriptor)
            {
                passProv.clearArchivePassword();
            }
            else if (descriptor instanceof PBKDF2Descriptor || descriptor instanceof ScryptDescriptor)
            {
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
            }
            else
            {
                throw new BaseBunkrException("Archive Open is not implemented for Descriptor type %s", descriptor.getIdentifier());
            }

            ArchiveInfoContext archive = new ArchiveInfoContext(path, usp);
            new MainWindow(archive, usp).getStage().show();
            this.getStage().close();
        }
        catch (Exception e)
        {
            QuickDialogs.exception(e);
            this.getStage().show();
        }
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
        scene.getStylesheets().add(this.cssCommon);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr");
        this.getStage().setScene(scene);
        this.getStage().setResizable(false);
        return scene;
    }
}
