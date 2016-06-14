package com.tanshizw.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
 * Created by archermind on 6/13/16.
 */
public class LauncherActivityInfoCompat {
    private ActivityInfo mActivityInfo;
    private ComponentName mComponentName;
    private PackageManager mPm;

    LauncherActivityInfoCompat(Context context, ResolveInfo info) {
        this.mActivityInfo = info.activityInfo;
        mComponentName = new ComponentName(mActivityInfo.packageName, mActivityInfo.name);
        mPm = context.getPackageManager();
    }

    public ComponentName getComponentName() {
        return mComponentName;
    }

    public CharSequence getLabel() {
        return mActivityInfo.loadLabel(mPm);
    }

    public Drawable getIcon(int density) {
        Drawable d = null;
        if (mActivityInfo.getIconResource() != 0) {
            Resources resources;
            try {
                resources = mPm.getResourcesForApplication(mActivityInfo.packageName);
            } catch (PackageManager.NameNotFoundException e) {
                resources = null;
            }
            if (resources != null) {
                try {
                    d = resources.getDrawableForDensity(mActivityInfo.getIconResource(), density);
                } catch (Resources.NotFoundException e) {
                    // Return default icon below.
                }
            }
        }
        if (d == null) {
            Resources resources = Resources.getSystem();
            d = resources.getDrawableForDensity(android.R.mipmap.sym_def_app_icon, density);
        }
        return d;
    }

    public Drawable getBadgedIcon(int density) {
        return getIcon(density);
    }
}
