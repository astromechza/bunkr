package org.bunkr.gui.windows;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.core.Resources;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.gui.components.treeview.InventoryTreeView;
import org.bunkr.gui.controllers.InventoryCMController;
import org.bunkr.gui.dialogs.QuickDialogs;

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
    private final UserSecurityProvider securityProvider;
    private InventoryTreeView tree;

    public MainWindow(ArchiveInfoContext archive, UserSecurityProvider securityProvider) throws IOException
    {
        super();
        this.archive = archive;
        this.securityProvider = securityProvider;
        this.cssPath = Resources.getExternalPath("/resources/css/main_window.css");
        this.initialise();

        InventoryCMController contextMenuController = new InventoryCMController(this.archive.getInventory(), this.tree);
        contextMenuController.bindEvents();
        contextMenuController.setOnSaveRequest(s -> {
            try
            {
                MetadataWriter.write(MainWindow.this.archive, MainWindow.this.securityProvider);
            }
            catch (IOException | BaseBunkrException e)
            {
                QuickDialogs.exception(e);
            }
        });

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
