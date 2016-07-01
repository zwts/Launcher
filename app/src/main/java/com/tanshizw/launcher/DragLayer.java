package com.tanshizw.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.tanshizw.launcher.drag.DragController;
import com.tanshizw.launcher.utility.Utilities;
import com.tanshizw.launcher.view.PageIndicator;

/**
 * A ViewGroup that coordinates dragging across its descendants
 */
public class DragLayer extends FrameLayout {
    private static final String TAG = "DragLayer";
    private DragController mDragController;
    private Launcher mLauncher;

    public void setup(Launcher launcher, DragController controller) {
        mLauncher = launcher;
        mDragController = controller;
    }

    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.v(TAG, "onLayout l = " + l + "; t = " + t + "; r = " + r + "; b = " + b);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof Workspace) {
                Log.v(TAG, "onLayout workspace");
                int x = l;
                int y = t + LauncherSettings.WORKSPACE_TOPPADDING;
                child.layout(x, y, x + r, y + LauncherSettings.WORKSPACE_HEIGHT);
            }
            if (child instanceof PageIndicator) {
                Log.v(TAG, "onLayout PageIndicator");
                int x = r / 2;
                int y = t + LauncherSettings.WORKSPACE_HEIGHT + LauncherSettings.WORKSPACE_TOPPADDING;
                child.layout(x, y, x + r, y + LauncherSettings.PAGEINDICATOR_HEIGHT);
            }
            if (child instanceof Hotseat) {
                Log.v(TAG, "onLayout Hotseat");
                int x = l;
                int y = t + LauncherSettings.WORKSPACE_HEIGHT + LauncherSettings.WORKSPACE_TOPPADDING
                        + LauncherSettings.PAGEINDICATOR_HEIGHT + LauncherSettings.PAGEINDICATOR_PADDING;
                child.layout(x, y, x + r, y + LauncherSettings.HOTSEAT_HEIGHT);
            }
        }
    }

    public float getLocationInDragLayer(View child, int[] loc) {
        loc[0] = 0;
        loc[1] = 0;
        return getDescendantCoordRelativeToSelf(child, loc, false);
    }

    /**
     * Given a coordinate relative to the descendant, find the coordinate in this DragLayer's
     * coordinates.
     *
     * @param descendant The descendant to which the passed coordinate is relative.
     * @param coord The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the root descendant:
     *          sometimes this is relevant as in a child's coordinates within the root descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     *         this scale factor is assumed to be equal in X and Y, and so if at any point this
     *         assumption fails, we will need to return a pair of scale factors.
     */
    public float getDescendantCoordRelativeToSelf(View descendant, int[] coord,
                                                  boolean includeRootScroll) {
        return Utilities.getDescendantCoordRelativeToParent(descendant, this,
                coord, includeRootScroll);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
/*        int action = ev.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            if (handleTouchDown(ev, true)) {
                return true;
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (mTouchCompleteListener != null) {
                mTouchCompleteListener.onTouchComplete();
            }
            mTouchCompleteListener = null;
        }
        clearAllResizeFrames();*/
        return mDragController.onInterceptTouchEvent(ev);
    }
}
