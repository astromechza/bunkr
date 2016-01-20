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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.Resources;
import org.bunkr.core.descriptor.PBKDF2Descriptor;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.descriptor.ScryptDescriptor;
import org.bunkr.core.inventory.Algorithms;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.gui.wizards.PBKDF2SecurityWizard;
import org.bunkr.gui.wizards.ScryptSecurityWizard;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Creator: benmeier
 * Created At: 2016-01-06
 */
public class ArchiveSecurityWindow extends BaseWindow
{
    private final ArchiveInfoContext archive;
    private final UserSecurityProvider securityProvider;
    private final String cssPath;

    private Label headerLabel;
    private Label headerLabelValue;
    private BorderPane centerPane;
    private Button changeSecurityButton, backButton;

    private Consumer<String> onSaveMetadataRequest;

    public ArchiveSecurityWindow(ArchiveInfoContext archive, UserSecurityProvider securityProvider) throws IOException
    {
        super();
        this.archive = archive;
        this.securityProvider = securityProvider;
        this.cssPath = Resources.getExternalPath("/resources/css/archive_settings_window.css");
        this.initialise();
        this.reloadArchiveSecurityDisplay();
    }

    @Override
    public void initControls()
    {
        this.headerLabel = new Label("Archive Security: ");
        this.headerLabelValue = new Label("Unknown");
        this.changeSecurityButton = new Button("Change..");
        this.changeSecurityButton.setFocusTraversable(false);
        this.backButton = new Button("Back");
        this.backButton.setFocusTraversable(false);
    }

    @Override
    public Parent initLayout()
    {
        BorderPane rootLayout = new BorderPane();
        rootLayout.setPadding(new Insets(10));

        rootLayout.setTop(new HBox(this.headerLabel, this.headerLabelValue));

        this.centerPane = new BorderPane();
        this.centerPane.setPrefWidth(600);
        this.centerPane.setMinWidth(400);
        this.centerPane.setMinHeight(200);
        this.centerPane.setBottom(changeSecurityButton);
        BorderPane.setAlignment(changeSecurityButton, Pos.BOTTOM_RIGHT);
        rootLayout.setCenter(this.centerPane);
        BorderPane.setMargin(this.centerPane, new Insets(10, 0, 10, 0));

        rootLayout.setBottom(backButton);
        BorderPane.setAlignment(backButton, Pos.BOTTOM_RIGHT);

        return rootLayout;
    }

    @Override
    public void bindEvents()
    {
        backButton.setOnAction(e -> this.getStage().close());
        changeSecurityButton.setOnAction(e -> {
            try
            {
                // ask user for new security model
                String type = QuickDialogs.pick("Pick a new Security Model", Arrays.asList(
                        PlaintextDescriptor.IDENTIFIER, PBKDF2Descriptor.IDENTIFIER, ScryptDescriptor.IDENTIFIER
                ), null);

                // if cancel selected, stop
                if (type == null) return;

                // spawn the correct wizard
                switch (type)
                {
                    case PlaintextDescriptor.IDENTIFIER:
                        if (QuickDialogs.confirm("Set security model to %s?", PlaintextDescriptor.IDENTIFIER))
                        {
                            this.archive.setDescriptor(new PlaintextDescriptor());
                            this.archive.getInventory().setDefaultEncryption(Algorithms.Encryption.NONE);
                            this.onSaveMetadataRequest.accept("Updated Security Model");
                        }
                        return;
                    case PBKDF2Descriptor.IDENTIFIER:
                        new PBKDF2SecurityWizard(archive, securityProvider).getStage().showAndWait();
                        this.reloadArchiveSecurityDisplay();
                        break;
                    case ScryptDescriptor.IDENTIFIER:
                        new ScryptSecurityWizard(archive, securityProvider).getStage().showAndWait();
                        this.reloadArchiveSecurityDisplay();
                        break;
                    default:
                        QuickDialogs.error("Descriptor type %s is unknown.", this.archive.getDescriptor().getIdentifier());
                }
            }
            catch (IOException exc)
            {
                QuickDialogs.exception(exc);
            }
        });
    }

    @Override
    public void applyStyling()
    {
        this.centerPane.getStyleClass().add("center-pane");
        this.changeSecurityButton.getStyleClass().add("small-button");
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssCommon);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr - Archive Security");
        this.getStage().setMinWidth(400);
        this.getStage().setScene(scene);
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        this.getStage().setResizable(false);
        return scene;
    }

    private Node getArchiveDescriptorNode()
    {
        switch (this.archive.getDescriptor().getIdentifier())
        {
            case PlaintextDescriptor.IDENTIFIER:
                return buildPlaintextDescriptorInfoPanel();
            case PBKDF2Descriptor.IDENTIFIER:
                return buildPBKDF2DescriptorInfoPanel();
            case ScryptDescriptor.IDENTIFIER:
                return buildScryptDescriptorInfoPanel();
            default:
                QuickDialogs.error("Descriptor type %s is unknown.", this.archive.getDescriptor().getIdentifier());
                return null;
        }
    }

    private void reloadArchiveSecurityDisplay()
    {
        this.headerLabelValue.setText(this.archive.getDescriptor().getIdentifier().toUpperCase());
        this.centerPane.setCenter(getArchiveDescriptorNode());
    }

    private Node buildPlaintextDescriptorInfoPanel()
    {
        Pane p = new Pane();
        p.getChildren().add(new Label("No options"));
        return p;
    }

    private Node buildPBKDF2DescriptorInfoPanel()
    {
        GridPane gp = new GridPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        gp.getColumnConstraints().addAll(cc, cc);
        gp.setHgap(10);

        PBKDF2Descriptor descriptor = (PBKDF2Descriptor) archive.getDescriptor();

        int rowid = 0;
        gp.add(new Label("PBKDF2 Iterations:"), 0, rowid); gp.add(new Label(String.format("%d", descriptor.pbkdf2Iterations)), 1, rowid++);
        gp.add(new Label("PBKDF2 Salt Length:"), 0, rowid); gp.add(new Label(String.format("%d bytes", descriptor.pbkdf2Salt.length)), 1, rowid++);
        gp.add(new Label("Inventory Encryption:"), 0, rowid); gp.add(new Label(String.format("%s", descriptor.encryptionAlgorithm)), 1, rowid++);
        gp.add(new Label("File Encryption:"), 0, rowid); gp.add(new Label(archive.getInventory().getDefaultEncryption().toString()), 1, rowid);

        return gp;
    }

    private Node buildScryptDescriptorInfoPanel()
    {
        GridPane gp = new GridPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        gp.getColumnConstraints().addAll(cc, cc);
        gp.setHgap(10);

        ScryptDescriptor descriptor = (ScryptDescriptor) archive.getDescriptor();

        int rowid = 0;
        gp.add(new Label("Scrypt Cost:"), 0, rowid); gp.add(new Label(String.format("%d", descriptor.scryptN)), 1, rowid++);
        gp.add(new Label("Scrypt Parallelization:"), 0, rowid); gp.add(new Label(String.format("%d", descriptor.scryptP)), 1, rowid++);
        gp.add(new Label("Scrypt Block Size:"), 0, rowid); gp.add(new Label(String.format("%d", descriptor.scryptR)), 1, rowid++);
        gp.add(new Label("Scrypt Salt Length:"), 0, rowid); gp.add(new Label(String.format("%d bytes", descriptor.scryptSalt.length)), 1, rowid++);
        gp.add(new Label("Inventory Encryption:"), 0, rowid); gp.add(new Label(String.format("%s", descriptor.encryptionAlgorithm)), 1, rowid++);
        gp.add(new Label("File Encryption:"), 0, rowid); gp.add(new Label(archive.getInventory().getDefaultEncryption().toString()), 1, rowid);

        return gp;
    }

    public void setOnSaveMetadataRequest(Consumer<String> onSaveMetadataRequest)
    {
        this.onSaveMetadataRequest = onSaveMetadataRequest;
    }
}
