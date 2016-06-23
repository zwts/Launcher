package com.tanshizw.launcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanshizw.launcher.R;

import java.util.ArrayList;

public class PageIndicator extends LinearLayout {
    private static final String TAG = "PageIndicator";
    private ArrayList<PageIndicatorMarker> mMarkers = new ArrayList<PageIndicatorMarker>();
    private int mActiveMarkerIndex = 0;
    private LayoutInflater mLayoutInflater;

    public PageIndicator(Context context) {
        super(context);
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setActiveMarkerIndex(int index) {
        mActiveMarkerIndex = index;
        for (int i = 0; i < mMarkers.size(); ++i) {
            PageIndicatorMarker marker = (PageIndicatorMarker) mMarkers.get(i);
            if (i == mActiveMarkerIndex) {
                marker.activate();
            } else {
                marker.inactivate();
            }
        }
    }

    public void offsetWindowCenterTo() {
        for (int i = getChildCount(); i >= 0; i--) {
            PageIndicatorMarker marker = (PageIndicatorMarker) getChildAt(i);
            removeView(marker);
        }

        for (int i = 0; i < mMarkers.size(); ++i) {
            PageIndicatorMarker marker = (PageIndicatorMarker) mMarkers.get(i);
            Log.v(TAG, "offsetWindowCenterTo");
            addView(marker, i);
            if (i == mActiveMarkerIndex) {
                marker.activate();
            } else {
                marker.inactivate();
            }
        }
    }

    public void addMarker() {
        int index = mMarkers.size();
        Log.v(TAG, "addMarker index = " + index);
        PageIndicatorMarker m =
                (PageIndicatorMarker) mLayoutInflater.inflate(R.layout.page_indicator_marker,
                        this, false);
        mMarkers.add(index, m);
        offsetWindowCenterTo();

    }

    void removeMarker(int index) {
        if (mMarkers.size() > 0) {
            index = Math.max(0, Math.min(mMarkers.size() - 1, index));
            mMarkers.remove(index);
            offsetWindowCenterTo();
        }
    }

    public void removeAllMarkers() {
        while (mMarkers.size() > 0) {
            removeMarker(Integer.MAX_VALUE);
        }
    }
}
