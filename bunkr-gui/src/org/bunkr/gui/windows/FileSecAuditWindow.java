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

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import org.bunkr.core.utils.Units;
import org.bunkr.gui.Icons;
import org.bunkr.gui.ProgressTask;
import org.bunkr.gui.components.HExpander;
import org.bunkr.gui.dialogs.ProgressDialog;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created At: 2016-01-23
 */
public class FileSecAuditWindow extends BaseWindow
{
    private final ArchiveInfoContext archive;

    private Label topLabel;
    private Button backButton, fixButton, fixAllButton;
    private ListView<PathBoxItem> pathsBox;
    private Consumer<String> onSaveMetadataRequest;

    public FileSecAuditWindow(ArchiveInfoContext archive) throws IOException
    {
        super();
        this.archive = archive;
        this.initialise();
        this.reloadFixablePaths();
    }

    @Override
    public void initControls()
    {
        this.topLabel = new Label("These files all have stale encryption settings. Select the ones you want to fix, and press the 'Re-encrypt' button to re-encrypt them.");
        this.topLabel.setWrapText(true);
        this.pathsBox = new ListView<>();
        this.pathsBox.setCellFactory(CheckBoxListCell.forListView(param -> param.selected));
        this.pathsBox.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.backButton = new Button("Back");
        this.fixButton = Icons.buildIconButton("Re-encrypt Selected Files", Icons.ICON_RELOAD);
        this.fixAllButton = Icons.buildIconButton("Re-encrypt All Files", Icons.ICON_RELOAD);
    }

    @Override
    public Parent initLayout()
    {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        root.setTop(this.topLabel);

        root.setCenter(this.pathsBox);
        BorderPane.setMargin(this.pathsBox, new Insets(10, 0, 10, 0));

        root.setBottom(new HBox(10, this.fixButton, this.fixAllButton, new HExpander(), this.backButton));
        return root;
    }

    @Override
    public void bindEvents()
    {
        this.backButton.setOnAction(e -> this.getStage().close());

        this.fixButton.setOnAction(e -> {
            List<FileInventoryItem> selected = this.pathsBox.getItems().stream()
                    .filter(pathBoxItem -> pathBoxItem.selected.get())
                    .map(pathBoxItem -> pathBoxItem.file)
                    .collect(Collectors.toList());

            if (selected.size() == 0)  return;

            if (!QuickDialogs.confirm("Are you sure you want to re-encrypt %d files? This may take some time.", selected.size())) return;

            this.reencryptFiles(selected);
        });

        this.fixAllButton.setOnAction(e -> {
            this.pathsBox.getItems().stream().forEach(i -> i.selected.set(true));
            this.fixButton.fire();
        });
    }

    @Override
    public void applyStyling()
    {

    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssCommon);
        this.getStage().setTitle("Bunkr - File Security Audit");
        this.getStage().setMinWidth(400);
        this.getStage().setMaxWidth(800);
        this.getStage().setScene(scene);
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        this.getStage().setResizable(false);
        return scene;
    }

    public void setOnSaveMetadataRequest(Consumer<String> onSaveMetadataRequest)
    {
        this.onSaveMetadataRequest = onSaveMetadataRequest;
    }

    public void reloadFixablePaths()
    {
        this.pathsBox.getItems().clear();
        Iterator<FileInventoryItem> fileit = archive.getInventory().getIterator();
        while(fileit.hasNext())
        {
            FileInventoryItem current = fileit.next();
            if (current.getEncryptionAlgorithm() != archive.getInventory().getDefaultEncryption())
            {
                this.pathsBox.getItems().add(new PathBoxItem(current));
            }
        }
    }

    public boolean hasOutstandingItems()
    {
        return this.pathsBox.getItems().size() > 0;
    }

    private class PathBoxItem
    {
        public final FileInventoryItem file;
        public final String text;
        public final SimpleBooleanProperty selected = new SimpleBooleanProperty();

        public PathBoxItem(FileInventoryItem file)
        {
            this.file = file;
            this.text = String.format("%s [%s]", file.getAbsolutePath(), file.getEncryptionAlgorithm());
            this.selected.set(false);
        }

        @Override
        public String toString()
        {
            return text;
        }
    }

    private void reencryptFiles(List<FileInventoryItem> files)
    {
        ProgressTask<Void> progressTask = new ProgressTask<Void>()
        {
            @Override
            protected Void innerCall() throws Exception
            {
                this.updateMessage("Calculating total bytes");

                long bytesTotal = files.stream().mapToLong(FileInventoryItem::getActualSize).sum();
                long bytesDone = 0;
                int totalFiles = files.size();
                for (int i = 0; i < totalFiles; i++)
                {
                    FileInventoryItem currentFile = files.get(i);
                    try (MultilayeredInputStream mis = new MultilayeredInputStream(archive, currentFile))
                    {
                        try (MultilayeredOutputStream mos = new MultilayeredOutputStream(archive, currentFile))
                        {
                            this.updateMessage("Re-encrypting file %d of %d..", i + 1, totalFiles);
                            byte[] buffer = new byte[(int) Units.MEBIBYTE];
                            int n;
                            while ((n = mis.read(buffer)) != -1)
                            {
                                mos.write(buffer, 0, n);
                                bytesDone += n;
                                this.updateProgress(bytesDone, bytesTotal);
                            }
                        }
                    }
                }

                this.updateMessage("Finished.");

                return null;
            }

            @Override
            protected void succeeded()
            {
                onSaveMetadataRequest.accept("Reencrypted files");
                FileSecAuditWindow.this.reloadFixablePaths();
                QuickDialogs.info("Finished Re-encrypting all files.");
            }

            @Override
            protected void cancelled()
            {
                onSaveMetadataRequest.accept("Cancelled file re-encrypt");
            }

            @Override
            protected void failed()
            {
                onSaveMetadataRequest.accept("Failed file reencrypt");
                QuickDialogs.exception(this.getException());
            }
        };

        ProgressDialog pd = new ProgressDialog(progressTask);
        pd.setHeaderText("Re-encrypting files ...");
        Thread task = new Thread(progressTask);
        task.setDaemon(true);
        task.start();

    }
}
