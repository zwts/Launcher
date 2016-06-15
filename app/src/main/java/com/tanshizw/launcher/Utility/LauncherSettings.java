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
    public static final int WORKSPACE_HEIGHT = 1000;
    //workspace padding top
    public static final int WORKSPACE_TOPPADDING = 10;
    //pageindicator height
    public static final int PAGEINDICATOR_HEIGHT = 50;
    //pageindicator padding workspace
    public static final int PAGEINDICATOR_PADDING = 20;
    //hotseat height
    public static final int HOTSEAT_HEIGHT = 100;
    //mCountX cells in horizental
    public static final int mCountX = 6;
    //mCountY cells in vertical
    public static final int mCountY = 6;
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
