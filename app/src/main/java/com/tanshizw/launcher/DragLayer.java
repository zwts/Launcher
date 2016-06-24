package com.tanshizw.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.tanshizw.launcher.view.PageIndicator;

/**
 * A ViewGroup that coordinates dragging across its descendants
 */
public class DragLayer extends FrameLayout {

    private static final String TAG = "DragLayer";

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
                int x = l;
                int y = t + LauncherSettings.WORKSPACE_TOPPADDING;
                child.layout(x, y, x + r, y + LauncherSettings.WORKSPACE_HEIGHT);
            }
            if (child instanceof PageIndicator) {
                Log.v(TAG, "onLayout PageIndicator");
                int x = r / 2;
                int y = t + LauncherSettings.WORKSPACE_HEIGHT + LauncherSettings.WORKSPACE_TOPPADDING;
                child.layout(x, y, x + r, y + LauncherSettings.PAGEINDICATOR_HEIGHT);
            }
            if (child instanceof Hotseat) {
                Log.v(TAG, "onLayout Hotseat");
                int x = l;
                int y = t + LauncherSettings.WORKSPACE_HEIGHT + LauncherSettings.WORKSPACE_TOPPADDING
                        + LauncherSettings.PAGEINDICATOR_HEIGHT + LauncherSettings.PAGEINDICATOR_PADDING;
                child.layout(x, y, x + r, y + LauncherSettings.HOTSEAT_HEIGHT);
            }
        }
    }

}
