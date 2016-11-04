package org.bunkr.gui.windows;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.Resources;
import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.operations.BlockDefragmentation;
import org.bunkr.core.streams.input.MultilayeredInputStream;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
import org.bunkr.core.utils.Formatters;
import org.bunkr.core.utils.Units;
import org.bunkr.gui.BlockImageGenerator;
import org.bunkr.gui.ProgressTask;
import org.bunkr.gui.dialogs.ProgressDialog;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created At: 2016-10-30
 */
public class BlockDistributionWindow extends BaseWindow
{
    private final String cssPath;
    private final ArchiveInfoContext archive;
    private ImageView imageView;
    private BorderPane imagePanel;
    private GridPane statsPanel;
    private GridPane actionPanel;
    private Label blocksLabel;
    private Label blocksValue;
    private Label bytesLabel;
    private Label bytesValue;
    private Label percentLabel;
    private Label percentValue;
    private Label workValue;
    private Button defragButton;

    private Consumer<String> onSaveInventoryRequest;

    public BlockDistributionWindow(ArchiveInfoContext archive) throws IOException
    {
        super();
        this.archive = archive;
        this.cssPath = Resources.getExternalPath("/resources/css/block_distrib.css");
        this.initialise();
        this.refresh();
    }

    public void refresh()
    {
        FragmentedRange allocatedBlocks = new FragmentedRange();
        Iterator<FileInventoryItem> fit = this.archive.getInventory().getIterator();
        while(fit.hasNext())
        {
            allocatedBlocks.union(fit.next().getBlocks());
        }

        FragmentedRange unallocatedBlocks = allocatedBlocks.invert();
        long allocatedBlocksCount = allocatedBlocks.size();
        long unallocatedBlocksCount = unallocatedBlocks.size();
        int blockSize = this.archive.getBlockSize();

        this.blocksValue.setText(String.format("%d allocated, %d unallocated", allocatedBlocksCount, unallocatedBlocksCount));
        this.bytesValue.setText(String.format("%s allocated, %s unallocated",
                                               Formatters.formatBytes(allocatedBlocksCount * blockSize),
                                               Formatters.formatBytes(unallocatedBlocksCount * blockSize)));
        long total = allocatedBlocksCount + unallocatedBlocksCount;
        if (total > 0)
            this.percentValue.setText(String.format("%.2f%% unallocated",
                                                    100 * unallocatedBlocksCount / (float) total));
        else
            this.percentValue.setText("-");

        imageView.setImage(BlockImageGenerator.buildImageFromArchiveInfo(this.archive, 400));

        int moveBlocksCount = BlockDefragmentation.calculateFilesThatRequireAMove(
                archive.getInventory()).stream().mapToInt(f -> f.getBlocks().size()).sum();
        defragButton.setDisable(moveBlocksCount == 0);
        workValue.setText(String.format("%d blocks (%s) need to be written to defrag the archive and save %s.",
                                        moveBlocksCount,
                                        Formatters.formatBytes(moveBlocksCount * blockSize),
                                        Formatters.formatBytes(unallocatedBlocksCount * blockSize)
        ));
    }

    @Override
    public void initControls()
    {
        this.statsPanel = new GridPane();
        this.imagePanel = new BorderPane();
        this.actionPanel = new GridPane();

        this.imageView = new ImageView();

        this.blocksLabel = new Label("Blocks:");
        this.blocksValue = new Label();
        this.bytesLabel = new Label("Bytes:");
        this.bytesValue = new Label();
        this.percentLabel = new Label("Percentage:");
        this.percentValue = new Label();

        this.workValue = new Label();
        this.defragButton = new Button("Defrag Once");
    }

    @Override
    public Parent initLayout()
    {
        BorderPane root = new BorderPane();

        root.setMaxWidth(Double.MAX_VALUE);
        root.setMaxHeight(Double.MAX_VALUE);
        root.setPadding(new Insets(10));

        statsPanel.setHgap(10);
        statsPanel.setHgap(10);
        statsPanel.setPadding(new Insets(10));

        int row = 0;
        statsPanel.add(this.blocksLabel, 0, row); statsPanel.add(this.blocksValue, 1, row); row++;
        statsPanel.add(this.bytesLabel, 0, row); statsPanel.add(this.bytesValue, 1, row); row++;
        statsPanel.add(this.percentLabel, 0, row); statsPanel.add(this.percentValue, 1, row);

        root.setTop(statsPanel);

        imagePanel.setCenter(imageView);

        imageView.setSmooth(false);
        imageView.fitWidthProperty().bind(imagePanel.widthProperty().subtract(10));
        imageView.fitHeightProperty().bind(imagePanel.heightProperty().subtract(10));

        root.setCenter(imagePanel);

        actionPanel.setHgap(10);
        actionPanel.setHgap(10);
        actionPanel.setPadding(new Insets(10, 0, 0, 10));

        actionPanel.add(this.workValue, 0, 0); actionPanel.add(this.defragButton, 1, 0);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHalignment(HPos.LEFT);
        c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHalignment(HPos.RIGHT);

        actionPanel.getColumnConstraints().addAll(c1, c2);
        actionPanel.setMaxWidth(Double.MAX_VALUE);

        root.setBottom(actionPanel);

        BorderPane.setMargin(imagePanel, new Insets(10, 0, 0, 0));

        return root;
    }

    @Override
    public void bindEvents()
    {
        this.defragButton.setOnAction(event -> this.work());
    }

    @Override
    public void applyStyling()
    {
        this.statsPanel.getStyleClass().addAll("block-stats");
        this.imagePanel.getStyleClass().addAll("block-image");
        this.bytesLabel.getStyleClass().add("row-label");
        this.bytesValue.getStyleClass().add("row-value");
        this.blocksLabel.getStyleClass().add("row-label");
        this.blocksValue.getStyleClass().add("row-value");
        this.percentLabel.getStyleClass().add("row-label");
        this.percentValue.getStyleClass().add("row-value");
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssCommon);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr - Block Distribution");
        this.getStage().setMinWidth(750);
        this.getStage().setMinHeight(300);
        this.getStage().setScene(scene);
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        this.getStage().setResizable(true);
        return scene;
    }

    private void work()
    {
        List<FileInventoryItem> files = BlockDefragmentation.calculateFilesThatRequireAMove(archive.getInventory());
        if (files.size() > 0)
        {
            FileInventoryItem firstFile = files.get(0);

            ProgressTask<Void> progressTask = new ProgressTask<Void>()
            {
                FileInventoryItem subject = firstFile;

                @Override
                protected Void innerCall() throws Exception
                {
                    try (MultilayeredInputStream mis = new MultilayeredInputStream(archive, subject))
                    {
                        try (MultilayeredOutputStream mos = new MultilayeredOutputStream(archive, subject))
                        {
                            this.updateMessage("Re-encrypting file bytes..");
                            long bytesTotal = subject.getActualSize();
                            long bytesDone = 0;
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

                    this.updateMessage("Finished.");

                    return null;
                }

                @Override
                protected void succeeded()
                {
                    onSaveInventoryRequest.accept(String.format("Rewrote file %s", subject.getName()));
                    BlockDistributionWindow.this.refresh();
                }

                @Override
                protected void cancelled()
                {
                    onSaveInventoryRequest.accept("Cancelled file import");
                }

                @Override
                protected void failed()
                {
                    onSaveInventoryRequest.accept("Failed file import");
                    QuickDialogs.exception(this.getException());
                }
            };

            ProgressDialog pd = new ProgressDialog(progressTask);
            pd.setHeaderText(String.format("Re-encrypting file %s ...", firstFile.getName()));
            Thread task = new Thread(progressTask);
            task.setDaemon(true);
            task.start();
        }
    }

    public void setOnSaveInventoryRequest(Consumer<String> onSaveInventoryRequest)
    {
        this.onSaveInventoryRequest = onSaveInventoryRequest;
    }
}
