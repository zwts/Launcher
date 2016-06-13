package com.tanshizw.launcher;

/**
 * Created by user on 6/8/16.
 */
public class ItemInfo {
    static final int NO_ID = -1;
    /**
     * The id of the container that holds this item. For the desktop, this will be
     * CONTAINER_DESKTOP. For the all applications folder it NO_ID (since it is not stored in the settings DB). For user folders
     * it will be the id of the folder.
     */
    public long container = NO_ID;
    /**
     * The id in the settings database for this item
     */
    public long id = NO_ID;

    /**
     * ITEM_TYPE_APPLICATION
     * ITEM_TYPE_SHORTCUT
     * ITEM_TYPE_FOLDER
     * ITEM_TYPE_APPWIDGET
     */
    public int itemType;
    /**
     * Iindicates the screen in which the shortcut appears.
     */
    public long screenId = -1;

    /**
     * Indicates the X position of the associated cell.
     */
    public int cellX = -1;

    /**
     * Indicates the Y position of the associated cell.
     */
    public int cellY = -1;

    /**
     * Indicates the X cell span.
     */
    public int spanX = 1;

    /**
     * Indicates the Y cell span.
     */
    int spanY = 1;
    /**
     * Title of the item
     */
    CharSequence title;
}
