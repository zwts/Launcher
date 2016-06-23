package com.tanshizw.launcher.items;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.tanshizw.launcher.utility.FastBitmapDrawable;
import com.tanshizw.launcher.utility.Utilities;

/**
 * Every item we draw in workspaces is a BubbleTextView
 */
public class BubbleTextView extends TextView {
    private static final int ICON_TEXT_PADDING = 10;

    public BubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void applyFromShortcutInfo(ShortcutInfo info, IconCache iconCache,
                                      boolean setDefaultPadding) {
        Bitmap b = info.getIcon(iconCache);
        FastBitmapDrawable iconDrawable = Utilities.createIconDrawable(b);

        setCompoundDrawables(null, iconDrawable, null, null);
        if (setDefaultPadding) {
            setCompoundDrawablePadding(ICON_TEXT_PADDING);
        }
        if (info.contentDescription != null) {
            setContentDescription(info.contentDescription);
        }
        setText(info.title);
        setTag(info);
    }
}
