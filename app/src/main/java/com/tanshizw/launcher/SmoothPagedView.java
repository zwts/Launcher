package com.tanshizw.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by user on 6/8/16.
 */
public class SmoothPagedView  extends ViewGroup {
    protected int mCurrentPage;

    public SmoothPagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public SmoothPagedView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);

    }

    int getCurrentPage() {
        return mCurrentPage;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
