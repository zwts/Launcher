package com.tanshizw.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.tanshizw.launcher.utility.LauncherSettings;

/**
 * Custom control to display at the bottom of screen, set applications in common use
 */
public class Hotseat extends ViewGroup {
    private final String TAG = "Hotseat";
    private CellLayout mContent;
    private Launcher mLauncher;

    public Hotseat(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLauncher = (Launcher) context;

        //remove lines below
//        //mContent = (CellLayout) findViewById(R.id.layout);
//        Log.v(TAG, "Hotseat mContent = " + mContent);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.v(TAG, "onLayout l = " + l + "; t = " + t + "; r = " + r + "; b = " + b);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = (View) getChildAt(i);
            int x = 0;
            int y = 0;
            child.layout(x, y, x + r, y + LauncherSettings.HOTSEAT_HEIGHT);

            // child.layout(0, 0, r, LauncherSettings.HOTSEAT_HEIGHT);
            // and I don't think we should override this onLayout here.
        }
    }

    CellLayout getLayout() {
        return mContent;
    }


    // and why add this CellLayout dynamically, not statically in hot_seat.xml?
    public void insertHotseatLayout() {
        CellLayout newScreen = (CellLayout)
                mLauncher.getLayoutInflater().inflate(R.layout.workspace_screen, null); // use this instead of null
        mContent = newScreen;
        addView(newScreen, getChildCount());
        Log.v(TAG, "insertHotseatLayout getChildCount() = " + getChildCount());
    }
}
