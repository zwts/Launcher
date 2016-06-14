package com.tanshizw.launcher;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tanshizw.launcher.view.PageIndicator;

/**
 * Created by user on 6/7/16.
 */
public class DragLayer extends FrameLayout implements ViewGroup.OnHierarchyChangeListener{

    private static final String TAG = "DragLayer";
    private final Rect mInsets = new Rect();

    private final int WORKSPACE_HEIGHT = 1000;
    private final int WORKSPACE_TOPPADDING = 10;
    private final int PAGEINDICATOR_HEIGHT = 50;

    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.v(TAG, "onLayout l = " + l + "; t = " + t + "; r = " + r + "; b = " + b);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof Workspace) {
                Log.v(TAG, "onLayout workspace");
                child.layout(l, t + WORKSPACE_TOPPADDING, r, WORKSPACE_HEIGHT);
            }
            if (child instanceof PageIndicator) {
                Log.v(TAG, "onLayout PageIndicator");
                child.layout(r / 2, t + WORKSPACE_HEIGHT, r, PAGEINDICATOR_HEIGHT + WORKSPACE_HEIGHT);
            }
        }
    }

    @Override
    public void onChildViewAdded(View parent, View child) {

    }

    @Override
    public void onChildViewRemoved(View parent, View child) {

    }
}
