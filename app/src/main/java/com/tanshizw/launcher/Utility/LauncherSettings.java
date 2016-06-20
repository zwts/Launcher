package com.tanshizw.launcher.Utility;

import java.lang.reflect.Array;

/**
 * Created by user on 6/15/16.
 */
final public class LauncherSettings {
    //every ITEMS_CHUNK to handle in a thread
    public static final int ITEMS_CHUNK = 6;
    //display in workspace
    public static final int CONTAINER_DESKTOP = -100;
    //display in hotseat
    public static final int CONTAINER_HOTSEAT = -101;
    //workspace height
    public static final int WORKSPACE_HEIGHT = 960;
    //workspace padding top
    public static final int WORKSPACE_TOPPADDING = 50;
    //pageindicator height
    public static final int PAGEINDICATOR_HEIGHT = 50;
    //pageindicator padding workspace
    public static final int PAGEINDICATOR_PADDING = 20;
    //hotseat height
    public static final int HOTSEAT_HEIGHT = 100;
    //icon width
    public static final int ICON_WIDTH = 130;
    //icon height
    public static final int ICON_HEIGHT = 160;
    //icon padding
    public static final int ICON_PADDING = 0;
    //mCountX cells in horizental
    public static final int mCountX = 6;
    //mCountY cells in vertical
    public static final int mCountY = 6;
    //slide the page deltaX gap
    public static final int SLIDE_PAGE_DELTAX = 50;
    //screen width
    public static int SCREEN_WIDTH;
    //screen height
    public static int SCREEN_HEIGHT;
    //snap screen gap
    public static int SNAP_SCREEN_GAP = 200;
    //hotset applications
    public static final String[] HOTSET_APPS = {
            "com.android.dialer",
            "com.android.mms",
            "com.android.contacts",
            "com.android.browser",
            "com.android.email",
            "com.android.settings"
    };
}
