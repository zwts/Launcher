package com.tanshizw.launcher;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.tanshizw.launcher.utility.LauncherSettings;

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
        mLauncher = (Launcher) context;

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
        //super.onLayout(changed, l, t, r, b);
        Log.v(TAG, "onLayout l = " + l + "; t = " + t + "; r = " + r + "; b = " + b);
        int count = getChildCount();
        int x = l;
        int y = t;
        for(int i = 0; i < count; i++){
            View child = (View)getChildAt(i);
            child.layout(x, y, x + r, y + b);
            x = x + r;
        }
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screenId The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     */
    void addInScreenFromBind(View child, long container, long screenId, int x, int y, int spanX, int spanY) {
        Log.v(TAG, "addInScreenFromBind");
        if (container == LauncherSettings.CONTAINER_DESKTOP) {
            if (getScreenWithId(screenId) == null) {
                Log.e(TAG, "Skipping child, screenId " + screenId + " not found");
                // DEBUGGING - Print out the stack trace to see where we are adding from
                new Throwable().printStackTrace();
                return;
            }
        }

        CellLayout layout;
        if (container == LauncherSettings.CONTAINER_HOTSEAT) {
            layout = mLauncher.getHotseat().getLayout();
        } else {
            layout = getScreenWithId(screenId);
        }

        ViewGroup.LayoutParams genericLp = child.getLayoutParams();
        CellLayout.LayoutParams lp;
        if (genericLp == null || !(genericLp instanceof CellLayout.LayoutParams)) {
            Log.v(TAG, "Layout is not CellLayout.LayoutParams");
            lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
            lp.setup(LauncherSettings.ICON_WIDTH, LauncherSettings.ICON_HEIGHT,
                    LauncherSettings.ICON_PADDING, LauncherSettings.ICON_PADDING);
        } else {
            Log.v(TAG, "Layout is CellLayout.LayoutParams");
            lp = (CellLayout.LayoutParams) genericLp;
            lp.cellX = x;
            lp.cellY = y;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
        }
        
        if (!layout.addViewToCellLayout(child, 0, lp, true)) {
            Log.v(TAG, "add view to CellLayout failed");
        }
    }

    /**
     * Insert page in workspace
     *
     * @param screenId The screen id.
     */
    public long insertNewWorkspaceScreen(long screenId) {
        Log.v(TAG, "insertNewWorkspaceScreen screenId = " + screenId);
        if (mWorkspaceScreens.containsKey(screenId)) {
            throw new RuntimeException("Screen id " + screenId + " already exists!");
        }
        CellLayout newScreen = (CellLayout)
                mLauncher.getLayoutInflater().inflate(R.layout.workspace_screen, null);
        mWorkspaceScreens.put(screenId, newScreen);
        addView(newScreen, getChildCount());
        Log.v(TAG, "insertNewWorkspaceScreen getChildCount() = " + getChildCount());
        return screenId;
    }
}
