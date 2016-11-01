package org.bunkr.gui.windows;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.Resources;
import org.bunkr.core.fragmented_range.FragmentedRange;
import org.bunkr.core.inventory.FileInventoryItem;
import org.bunkr.core.utils.Formatters;
import org.bunkr.gui.BlockImageGenerator;

import java.io.IOException;
import java.util.Iterator;

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
    private Label blocksLabel;
    private Label blocksValue;
    private Label bytesLabel;
    private Label bytesValue;
    private Label percentLabel;
    private Label percentValue;

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

    }

    @Override
    public void initControls()
    {
        this.statsPanel = new GridPane();
        this.imagePanel = new BorderPane();
        this.imageView = new ImageView();

        this.blocksLabel = new Label("Blocks:");
        this.blocksValue = new Label();
        this.bytesLabel = new Label("Bytes:");
        this.bytesValue = new Label();
        this.percentLabel = new Label("Percentage:");
        this.percentValue = new Label();
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

        return root;
    }

    @Override
    public void bindEvents()
    {

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
        this.getStage().setMinWidth(800);
        this.getStage().setMinHeight(300);
        this.getStage().setScene(scene);
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        this.getStage().setResizable(true);
        return scene;
    }
}
