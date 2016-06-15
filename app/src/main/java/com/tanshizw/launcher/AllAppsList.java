package com.tanshizw.launcher;

import android.content.ComponentName;

import java.util.ArrayList;

/**
 * Stores the list of all applications for the all apps view.
 */
public class AllAppsList {
    public  static final int DEFAULT_APPLICATIONS_NUMBER = 42;

    /* List of all Apps*/
    public ArrayList<AppInfo> data =
            new ArrayList<AppInfo>(DEFAULT_APPLICATIONS_NUMBER);

    private IconCache mIconCache;

    public AllAppsList(IconCache iconCache) {
        mIconCache = iconCache;
    }

    public void add(AppInfo info) {
        if (findActivity(data, info.componentName)) {
            return;
        }
        data.add(info);
    }

    public AppInfo get(int index) {
        return data.get(index);
    }

    public int size() {
        return data.size();
    }

    public void clear() {
        data.clear();
    }


    /**
     * Returns whether apps contains component.
     */
    private static boolean findActivity(ArrayList<AppInfo> apps, ComponentName component) {
        final int N = apps.size();
        for (int i = 0; i < N; i++) {
            final AppInfo info = apps.get(i);
            if (info.componentName.equals(component)) {
                return true;
            }
        }
        return false;
    }
}
