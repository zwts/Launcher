package com.tanshizw.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by user on 6/8/16.
 */
public class SmoothPagedView  extends ViewGroup {
    protected int mCurrentPage;
    private static final String TAG = "SmoothPagedView";

    public SmoothPagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public SmoothPagedView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        mCurrentPage = 0;
    }
    int getCurrentPage() {
        return mCurrentPage;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.v(TAG, "onLayout l = " + l + "; t = " + t + "; r = " + r + "; b = " + b);
        for(int i = 0; i < getChildCount(); i++){
            final View child = getChildAt(getChildCount() - i - 1);
            child.layout(l, t, r, b);
        }
    }
}
