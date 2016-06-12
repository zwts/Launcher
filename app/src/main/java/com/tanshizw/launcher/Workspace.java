package com.tanshizw.launcher;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

/**
 * Created by user on 6/7/16.
 */
public class Workspace extends SmoothPagedView implements Insettable{
    private HashMap<Long, CellLayout> mWorkspaceScreens = new HashMap<Long, CellLayout>();
    private static final String TAG = "Launcher.Workspace";
    private Launcher mLauncher;

    public Workspace(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Workspace(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        mLauncher = (Launcher) context;
    }

    public CellLayout getScreenWithId(long screenId) {
        CellLayout layout = mWorkspaceScreens.get(screenId);
        return layout;
    }

    @Override
    public void setInsets(Rect insets) {

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    void addInScreenFromBind(View child, long container, long screenId, int x, int y, int spanX, int spanY) {
        if (container == Launcher.CONTAINER_DESKTOP) {
            if (getScreenWithId(screenId) == null) {
                Log.e(TAG, "Skipping child, screenId " + screenId + " not found");
                // DEBUGGING - Print out the stack trace to see where we are adding from
                new Throwable().printStackTrace();
                return;
            }
        }

        CellLayout layout = null;
        if (container == Launcher.CONTAINER_HOTSEAT) {
            //// TODO: 6/8/16  
        } else {
            //// TODO: 6/8/16
            layout = getScreenWithId(screenId);
        }

        ViewGroup.LayoutParams genericLp = child.getLayoutParams();
        CellLayout.LayoutParams lp;
        if (genericLp == null || !(genericLp instanceof CellLayout.LayoutParams)) {
            lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
        } else {
            lp = (CellLayout.LayoutParams) genericLp;
            lp.cellX = x;
            lp.cellY = y;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
        }

        // Get the canonical child id to uniquely represent this view in this screen
        ItemInfo info = (ItemInfo) child.getTag();
        int childId = mLauncher.getViewIdForItem(info);
        if (!layout.addViewToCellLayout(child, 0, childId, lp, true)) {
            // TODO: This branch occurs when the workspace is adding views
        }
    }

}
