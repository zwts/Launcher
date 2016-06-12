package com.tanshizw.launcher;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by user on 6/7/16.
 */
public class DragLayer extends FrameLayout implements ViewGroup.OnHierarchyChangeListener{

    private final Rect mInsets = new Rect();

    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        Log.v("test", "fitSystemWindows");
        final int n = getChildCount();
        for (int i = 0; i < n; i++) {
            final View child = getChildAt(i);
            setInsets(child, insets, mInsets);
        }
        mInsets.set(insets);
        return true; // I'll take it from here
    }

    private void setInsets(View child, Rect newInsets, Rect oldInsets) {
        final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) child.getLayoutParams();
        if (child instanceof Insettable) {
            ((Insettable) child).setInsets(newInsets);
        } else {
            flp.topMargin += (newInsets.top - oldInsets.top);
            flp.leftMargin += (newInsets.left - oldInsets.left);
            flp.rightMargin += (newInsets.right - oldInsets.right);
            flp.bottomMargin += (newInsets.bottom - oldInsets.bottom);
        }
        child.setLayoutParams(flp);
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        setInsets(child, mInsets, new Rect());
    }

    @Override
    public void onChildViewAdded(View parent, View child) {

    }

    @Override
    public void onChildViewRemoved(View parent, View child) {

    }
}
