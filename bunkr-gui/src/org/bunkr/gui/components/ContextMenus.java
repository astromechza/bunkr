package org.bunkr.gui.components;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * Creator: benmeier
 * Created At: 2015-12-27
 */
public class ContextMenus
{
    public static final String STR_NEW_FILE = "New File";
    public static final String STR_NEW_FOLDER = "New Folder";
    public static final String STR_DELETE = "Delete";
    public static final String STR_RENAME = "Rename";

    public final ContextMenu dirContextMenu, fileContextMenu, rootContextMenu;

    public final MenuItem rootNewFile, rootNewSubDir, dirNewFile, dirNewSubDir, dirDelete, dirRename, fileDelete, fileRename;

    public ContextMenus()
    {
        // root
        this.rootNewFile = new MenuItem(STR_NEW_FILE);
        this.rootNewSubDir = new MenuItem(STR_NEW_FOLDER);

        // file
        this.fileDelete = new MenuItem(STR_DELETE);
        this.fileRename = new MenuItem(STR_RENAME);

        // dir
        this.dirDelete = new MenuItem(STR_DELETE);
        this.dirRename = new MenuItem(STR_RENAME);
        this.dirNewFile = new MenuItem(STR_NEW_FILE);
        this.dirNewSubDir = new MenuItem(STR_NEW_FOLDER);

        this.dirContextMenu = new ContextMenu(this.dirNewFile, this.dirNewSubDir, this.dirRename, this.dirDelete);
        this.fileContextMenu = new ContextMenu(this.fileRename, this.fileDelete);
        this.rootContextMenu = new ContextMenu(this.rootNewFile, this.rootNewSubDir);
    }
}
