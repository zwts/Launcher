package com.tanshizw.launcher;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tanshizw.launcher.view.PageIndicator;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by user on 6/8/16.
 */
public class SmoothPagedView  extends ViewGroup {
    protected int mCurrentPage;
    private static final String TAG = "SmoothPagedView";

    //page indicator
    private int mPageIndicatorViewId;
    private PageIndicator mPageIndicator;

    public SmoothPagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.PagedView, 0, 0);
        mPageIndicatorViewId = a.getResourceId(R.styleable.PagedView_pageIndicator, -1);
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
        Log.v(TAG, "onLayout getChildCount() = " + getChildCount());
        for(int i = 0; i < getChildCount(); i++){
            final View child = getChildAt(getChildCount() - i - 1);
            child.layout(l, t, r, b);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewGroup grandParent = (ViewGroup) getParent();
        if(grandParent instanceof DragLayer){
            Log.v(TAG, "onAttachedToWindow parent is DragLayer");
        }
        if(mPageIndicator == null && mPageIndicatorViewId > -1){
            mPageIndicator = (PageIndicator) grandParent.findViewById(mPageIndicatorViewId);
            mPageIndicator.removeAllMarkers();

            for (int i = 0; i < getChildCount(); ++i) {
                mPageIndicator.addMarker();
            }


        }
        mPageIndicator.offsetWindowCenterTo();
    }
}
