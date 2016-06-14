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

    public Intent getIntent() {
        return intent;
    }

    public Bitmap getIcon(IconCache iconCache) {
        if (mIcon == null) {
            updateIcon(iconCache);
        }
        return mIcon;
    }

    public void updateIcon(IconCache iconCache) {
        mIcon = iconCache.getIcon(intent);
    }


    public ShortcutInfo(AppInfo info) {
        super(info);
        title = info.title.toString();
        intent = new Intent(info.intent);
    }
}
