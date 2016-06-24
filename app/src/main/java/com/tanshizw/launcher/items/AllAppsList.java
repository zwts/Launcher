package com.tanshizw.launcher.items;

import android.content.ComponentName;

import java.util.ArrayList;

/**
 * Stores the list of all applications for the all apps view.
 */
public class AllAppsList {
    public static final int DEFAULT_APPLICATIONS_NUMBER = 42;

    /* List of all Apps*/
    public ArrayList<AppInfo> data =
            new ArrayList<>(DEFAULT_APPLICATIONS_NUMBER);

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

    /**
     * Returns whether apps contains component.
     */
    private static boolean findActivity(ArrayList<AppInfo> apps, ComponentName component) {
        for (AppInfo info : apps) {
            if (info.componentName.equals(component)) {
                return true;
            }
        }
        return false;
    }
}
