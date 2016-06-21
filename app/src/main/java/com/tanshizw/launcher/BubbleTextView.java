package com.tanshizw.launcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by user on 6/8/16.
 */
public class BubbleTextView extends TextView {
    private int mTextColor;
    private boolean mIsTextVisible;
    private final int ICON_TEXT_PADING = 10;
    private final String TAG = "BubbleTextView";
    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
            Log.v(TAG, "BubbleTextView");
    }

    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache,
                                      boolean setDefaultPadding) {
        applyFromShortcutInfo(info, iconCache, setDefaultPadding, false);
    }


    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache,
                                      boolean setDefaultPadding, boolean promiseStateChanged ) {
        Bitmap b = info.getIcon(iconCache);

        FastBitmapDrawable iconDrawable = Utilities.createIconDrawable(b);

        setCompoundDrawables(null, iconDrawable, null, null);
        if (setDefaultPadding) {
            /*DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();*/
            setCompoundDrawablePadding(ICON_TEXT_PADING);
        }
        if (info.contentDescription != null) {
            setContentDescription(info.contentDescription);
        }
        setText(info.title);
        Log.v(TAG, "applyFromShortcutInfo info.title = " + info.title);
        setTag(info);
    }

    @Override
    public void setTextColor(int color) {
        mTextColor = color;
        super.setTextColor(color);
    }

    public void setTextVisibility(boolean visible) {
        Resources res = getResources();
        if (visible) {
            super.setTextColor(res.getColor(android.R.color.white));
        } else {
            super.setTextColor(res.getColor(android.R.color.transparent));
        }
        mIsTextVisible = visible;
    }


}
