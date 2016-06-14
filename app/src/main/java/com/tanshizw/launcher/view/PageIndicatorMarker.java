package com.tanshizw.launcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tanshizw.launcher.R;

/**
 * Created by user on 6/13/16.
 */
public class PageIndicatorMarker extends FrameLayout{
    private ImageView mActiveMarker;
    private ImageView mInactiveMarker;
    private boolean mIsActive = false;
    private static final String TAG = "PageIndicatorMaker";
    public PageIndicatorMarker(Context context) {
        super(context);
    }

    public PageIndicatorMarker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorMarker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        Log.v(TAG, "onFinishInflate");
        mActiveMarker = (ImageView) findViewById(R.id.active);
        mInactiveMarker = (ImageView) findViewById(R.id.inactive);
    }

    void activate(){
        mActiveMarker.animate().cancel();
        mActiveMarker.setAlpha(1f);
        mActiveMarker.setScaleX(1f);
        mActiveMarker.setScaleY(1f);
        mInactiveMarker.animate().cancel();
        mInactiveMarker.setAlpha(0f);
        mIsActive = true;
    }

    void inactivate(){
        mInactiveMarker.animate().cancel();
        mInactiveMarker.setAlpha(1f);
        mActiveMarker.animate().cancel();
        mActiveMarker.setAlpha(0f);
        mActiveMarker.setScaleX(0.5f);
        mActiveMarker.setScaleY(0.5f);
        mIsActive = false;
    }

    boolean isActive() {
        return mIsActive;
    }
}
