package com.tanshizw.launcher.items;

import android.content.Intent;
import android.graphics.Bitmap;

/**
 * Represents a launchable icon on the workspaces an in folders.
 */
public class ShortcutInfo extends ItemInfo {
    /**
     * The intent used to start the application.
     */
    public Intent intent;

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
