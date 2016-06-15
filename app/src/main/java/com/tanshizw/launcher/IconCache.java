package com.tanshizw.launcher;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by archermind on 6/13/16.
 */
public class IconCache {
    private Context mContext;
    private PackageManager mPackageManager;
    private LauncherAppsCompat mLauncherApps;
    private int mIconDpi;

    private static class CacheEntry {
        public Bitmap icon;
        public CharSequence title;
        public CharSequence contentDescription;
    }
    private static class CacheKey {
        public ComponentName componentName;

        CacheKey(ComponentName componentName) {
            this.componentName = componentName;
        }

        @Override
        public int hashCode() {
            return componentName.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            CacheKey other = (CacheKey) o;
            return other.componentName.equals(componentName);
        }
    }
    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;
    private final HashMap<CacheKey, CacheEntry> mCache =
            new HashMap<CacheKey, CacheEntry>(INITIAL_ICON_CACHE_CAPACITY);


    public IconCache(Context context) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        mContext = context;
        mPackageManager = context.getPackageManager();
        mLauncherApps = LauncherAppsCompat.getInstance(mContext);
        mIconDpi = activityManager.getLauncherLargeIconDensity();
    }

    public int getFullResIconDpi() {
        return mIconDpi;
    }

    /**
     * Fill in "application" with the icon and label for "info."
     */
    public synchronized void getTitleAndIcon(AppInfo application, LauncherActivityInfoCompat info,
                                             HashMap<Object, CharSequence> labelCache) {
        CacheEntry entry = cacheLocked(application.componentName, info, labelCache, false);

        application.title = entry.title;
        application.iconBitmap = entry.icon;
        application.contentDescription = entry.contentDescription;
    }

    /**
     * Retrieves the entry from the cache. If the entry is not present, it creates a new entry.
     * This method is not thread safe, it must be called from a synchronized method.
     */
    private CacheEntry cacheLocked(ComponentName componentName, LauncherActivityInfoCompat info,
                                   HashMap<Object, CharSequence> labelCache, boolean usePackageIcon) {
        CacheKey cacheKey = new CacheKey(componentName);
        CacheEntry entry = mCache.get(cacheKey);
        if (entry == null) {
            entry = new CacheEntry();

            mCache.put(cacheKey, entry);

            if (info != null) {
                ComponentName labelKey = info.getComponentName();
                if (labelCache != null && labelCache.containsKey(labelKey)) {
                    entry.title = labelCache.get(labelKey).toString();
                } else {
                    entry.title = info.getLabel().toString();
                    if (labelCache != null) {
                        labelCache.put(labelKey, entry.title);
                    }
                }

                entry.icon = Utilities.createIconBitmap(
                        info.getBadgedIcon(mIconDpi), mContext);
            }
        }
        return entry;
    }

    public synchronized Bitmap getIcon(ComponentName component, LauncherActivityInfoCompat info,
                                       HashMap<Object, CharSequence> labelCache) {
        if (info == null || component == null) {
            return null;
        }

        CacheEntry entry = cacheLocked(component, info, labelCache, false);
        return entry.icon;
    }

    public synchronized Bitmap getIcon(Intent intent) {
        ComponentName component = intent.getComponent();

        LauncherActivityInfoCompat launcherActInfo = mLauncherApps.resolveActivity(intent);
        CacheEntry entry = cacheLocked(component, launcherActInfo, null, true);
        return entry.icon;
    }


}
