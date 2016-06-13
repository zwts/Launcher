package com.tanshizw.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by user on 6/8/16.
 */
public class ShortcutAndWidgetContainer extends ViewGroup {
    private static final String TAG = "ShortcutContainer";

    public ShortcutAndWidgetContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.v(TAG, "ShortcutAndWidgetContainer");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.v(TAG, "onLayout l = " + l + "; t = " + t + "; r = " + r + "; b = " + b);
        for(int i = 0; i < getChildCount(); i++){
            final View child = getChildAt(getChildCount() - i - 1);
            child.layout(l + 100*i, t, r, b);
        }
    }
}
