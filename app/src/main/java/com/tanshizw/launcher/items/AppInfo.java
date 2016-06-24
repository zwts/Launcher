package com.tanshizw.launcher.items;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.tanshizw.launcher.compat.LauncherActivityInfoCompat;

import java.util.HashMap;

/**
 * Represents an app in launcher
 */
public class AppInfo extends ItemInfo {
    /**
     * The intent used to start the application.
     */
    public Intent intent;

    /**
     * A bitmap version of the application icon.
     */
    Bitmap iconBitmap;

    ComponentName componentName;

    /**
     * Used in setupWorkspaceItems, the resource of application items
     */
    public AppInfo(Context context, LauncherActivityInfoCompat info,
                   IconCache iconCache, HashMap<Object, CharSequence> labelCache) {
        this.componentName = info.getComponentName();
        this.container = ItemInfo.NO_ID;

        iconCache.getTitleAndIcon(this, info, labelCache);
        intent = makeLaunchIntent(context, info);
    }

    public static Intent makeLaunchIntent(Context context, LauncherActivityInfoCompat info) {
        return new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(info.getComponentName())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    }

    public ShortcutInfo makeShortcut() {
        return new ShortcutInfo(this);
    }
}
