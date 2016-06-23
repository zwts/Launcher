package com.tanshizw.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tanshizw.launcher.items.BubbleTextView;
import com.tanshizw.launcher.items.ItemInfo;
import com.tanshizw.launcher.utility.LauncherSettings;

public class CellLayout extends ViewGroup {
    private int mCountX;
    private int mCountY;
    boolean[][] mOccupied;
    private ShortcutAndWidgetContainer mShortcutsAndWidgets;
    private final String TAG = "CellLayout";

    public CellLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.v(TAG, "CellLayout");
        mCountX = LauncherSettings.mCountX;
        mCountY = LauncherSettings.mCountY;
        mOccupied = new boolean[mCountX][mCountY];
        mShortcutsAndWidgets = new ShortcutAndWidgetContainer(context, attrs);
        addView(mShortcutsAndWidgets);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.v(TAG, "onLayout l = " + l + "; t = " + t + "; r = " + r + "; b = " + b);
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            child.layout(0, 0, LauncherSettings.SCREEN_WIDTH, b);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.v(TAG, "onDraw");
    }

    public boolean isOccupied(int x, int y) {
        if (x < mCountX && y < mCountY) {
            return mOccupied[x][y];
        } else {
            throw new RuntimeException("Position exceeds the bound of this CellLayout");
        }
    }

    public boolean addViewToCellLayout(View child, int index, LayoutParams lp,
                                       boolean markCells) {

        // Hotseat icons need remove text

        if (lp.cellX >= 0 && lp.cellX <= mCountX - 1 && lp.cellY >= 0 && lp.cellY <= mCountY - 1) {
            if (lp.cellHSpan < 0) lp.cellHSpan = mCountX;
            if (lp.cellVSpan < 0) lp.cellVSpan = mCountY;

            mShortcutsAndWidgets.addView(child, index, lp);
            Log.v(TAG, "addView child.getText = " + ((TextView) child).getText());

            mShortcutsAndWidgets.measureChild(child);

            //if (markCells) markCellsAsOccupiedForView(child);

            return true;
        }
        return false;
    }

    public void markCellsAsOccupiedForView(View view) {
        markCellsAsOccupiedForView(view, mOccupied);
    }

    public void markCellsAsOccupiedForView(View view, boolean[][] occupied) {
        if (view == null || view.getParent() != mShortcutsAndWidgets) return;
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        markCellsForView(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan, occupied, true);
    }

    private void markCellsForView(int cellX, int cellY, int spanX, int spanY, boolean[][] occupied,
                                  boolean value) {
        if (cellX < 0 || cellY < 0) return;
        for (int x = cellX; x < cellX + spanX && x < mCountX; x++) {
            for (int y = cellY; y < cellY + spanY && y < mCountY; y++) {
                occupied[x][y] = value;
            }
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        /**
         * Horizontal location of the item in the grid.
         */
        public int cellX;
        /**
         * Vertical location of the item in the grid.
         */
        public int cellY;
        /**
         * Number of cells spanned horizontally by the item.
         */
        public int cellHSpan;
        /**
         * Number of cells spanned vertically by the item.
         */
        public int cellVSpan;
        // X coordinate of the view in the layout.
        public int x;
        // Y coordinate of the view in the layout.
        public int y;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan) {
            super(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellHSpan = cellHSpan;
            this.cellVSpan = cellVSpan;
        }

        public void setup(int cellWidth, int cellHeight, int widthGap, int heightGap) {
            final int myCellHSpan = cellHSpan;
            final int myCellVSpan = cellVSpan;
            int myCellX = cellX;
            int myCellY = cellY;

            width = myCellHSpan * cellWidth;
            height = myCellVSpan * cellHeight;
            x = myCellX * (cellWidth + widthGap) + leftMargin;
            y = myCellY * (cellHeight + heightGap) + topMargin;
        }
    }
}
