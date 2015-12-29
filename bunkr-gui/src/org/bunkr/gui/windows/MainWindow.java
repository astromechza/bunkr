package org.bunkr.gui.windows;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.Resources;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.controllers.InventoryCMController;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-26
 */
public class MainWindow extends BaseWindow
{
    private static final int WINDOW_WIDTH = 800, WINDOW_HEIGHT = 600;

    private final ArchiveInfoContext archive;
    private final String cssPath;
    private InventoryTreeView tree;

    public MainWindow(ArchiveInfoContext archive) throws IOException
    {
        super();
        this.archive = archive;
        this.cssPath = Resources.getExternalPath("/resources/css/main_window.css");
        this.initialise();

        new InventoryCMController(this.archive.getInventory(), this.tree).bindEvents();

        this.tree.refreshAll();
    }

    @Override
    public void initControls()
    {
        this.tree = new InventoryTreeView(this.archive);
    }

    @Override
    public Parent initLayout()
    {
        BorderPane bp = new BorderPane();
        bp.setCenter(this.tree);
        return bp;
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
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle(String.format("Bunkr - %s", this.archive.filePath.getAbsolutePath()));
        this.getStage().setScene(scene);
        this.getStage().setResizable(true);
        return scene;
    }
}
