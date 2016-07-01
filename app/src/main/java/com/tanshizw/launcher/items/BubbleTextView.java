package com.tanshizw.launcher.items;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import com.tanshizw.launcher.Launcher;
import com.tanshizw.launcher.R;
import com.tanshizw.launcher.utility.FastBitmapDrawable;
import com.tanshizw.launcher.utility.Utilities;

/**
 * Every item we draw in workspaces is a BubbleTextView
 */
public class BubbleTextView extends TextView {
    private final String TAG = "BubbleTextView";

    private final int ICON_TEXT_PADING = 10;
    private boolean mLayoutHorizontal;

    public BubbleTextView(Context context) {
        this(context, null, 0);
    }

    public BubbleTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.BubbleTextView, defStyle, 0);
        mLayoutHorizontal = a.getBoolean(R.styleable.BubbleTextView_layoutHorizontal, false);
    }

    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache,
                                      boolean setDefaultPadding) {
        applyFromShortcutInfo(info, iconCache, setDefaultPadding, false);
    }


    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache,
                                      boolean setDefaultPadding, boolean promiseStateChanged) {
        Bitmap b = info.getIcon(iconCache);

        FastBitmapDrawable iconDrawable = Utilities.createIconDrawable(b);

        setCompoundDrawables(null, iconDrawable, null, null);
        if (setDefaultPadding) {
            setCompoundDrawablePadding(ICON_TEXT_PADING);
        }
        if (info.contentDescription != null) {
            setContentDescription(info.contentDescription);
        }
        setText(info.title);
        setTag(info);
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
    }

    public void setTextVisibility(boolean visible) {
        Resources res = getResources();
        if (visible) {
            super.setTextColor(res.getColor(android.R.color.white));
        } else {
            super.setTextColor(res.getColor(android.R.color.transparent));
        }
    }

    /** Returns whether the layout is horizontal. */
    public boolean isLayoutHorizontal() {
        return mLayoutHorizontal;
    }

}
