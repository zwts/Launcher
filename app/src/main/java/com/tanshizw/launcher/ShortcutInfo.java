package com.tanshizw.launcher;

import android.content.Intent;
import android.graphics.Bitmap;

/**
 * Created by user on 6/8/16.
 */
public class ShortcutInfo extends ItemInfo{
    /**
     * The intent used to start the application.
     */
    Intent intent;
    /**
     * The application icon.
     */
    private Bitmap mIcon;
    public Bitmap getIcon() {
        return mIcon;
    }
}
