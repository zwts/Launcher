package com.tanshizw.launcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by user on 6/8/16.
 */
public class BubbleTextView extends TextView {
    private int mTextColor;
    private boolean mIsTextVisible;
    private final String TAG = "BubbleTextView";
    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.v(TAG, "BubbleTextView");
    }

    public void applyFromShortcutInfo(ShortcutInfo info) {
        Resources resources = Resources.getSystem();
        Drawable drawable = resources.getDrawable(android.R.mipmap.sym_def_app_icon);
        drawable.setBounds(0, 0, 100, 100);
        setCompoundDrawables(drawable, null, null, null);

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
