package com.tanshizw.launcher;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by user on 6/8/16.
 */
public class BubbleTextView extends TextView {
    private int mTextColor;
    private boolean mIsTextVisible;
    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void applyFromShortcutInfo(ShortcutInfo info, //IconCache iconCache,
                                      boolean setDefaultPadding) {
        applyFromShortcutInfo(info, setDefaultPadding, false);
    }

    public void applyFromShortcutInfo(ShortcutInfo info, //IconCache iconCache,
                                      boolean setDefaultPadding, boolean promiseStateChanged) {
        //// TODO: 6/8/16
        Bitmap b = info.getIcon();
    }

    @Override
    public void setTextColor(int color) {
        mTextColor = color;
        super.setTextColor(color);
    }

    @Override
    public void setTextColor(ColorStateList colors) {
        mTextColor = colors.getDefaultColor();
        super.setTextColor(colors);
    }

    public void setTextVisibility(boolean visible) {
        Resources res = getResources();
        if (visible) {
            super.setTextColor(mTextColor);
        } else {
            super.setTextColor(res.getColor(android.R.color.transparent));
        }
        mIsTextVisible = visible;
    }
}
