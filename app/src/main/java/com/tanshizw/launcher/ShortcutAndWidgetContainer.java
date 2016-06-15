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
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for(int i = 0; i < count; i++){
            View child = getChildAt(count - i - 1);
            CellLayout.LayoutParams params = (CellLayout.LayoutParams) child.getLayoutParams();
            Log.v(TAG, "onLayout l = " + params.x + "; t = " + params.y + "; r = " + params.width + "; b = " + params.height);
            child.layout(params.x, params.y, params.x + params.width, params.y + params.height);
        }
    }
}
