package com.tanshizw.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by user on 6/6/16.
 */
public class CellLayout extends ViewGroup {
    private int mCountX = 6;//mCountX cells in horizental
    private int mCountY = 6;//mCountY cells in vertical
    private int mCellWidth = 100;
    boolean[][] mOccupied;
    private ShortcutAndWidgetContainer mShortcutsAndWidgets;
    private final String TAG = "CellLayout";

    public CellLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.v(TAG, "CellLayout");
        mOccupied = new boolean[mCountX][mCountY];
        mShortcutsAndWidgets = new ShortcutAndWidgetContainer(context, attrs);
        addView(mShortcutsAndWidgets);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.v(TAG, "onLayout l = " + l + "; t = " + t + "; r = " + r + "; b = " + b);
        for(int i = 0; i < getChildCount(); i++){
            final View child = getChildAt(getChildCount() - i - 1);
            child.layout(l, t, r, b);
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

    public float getChildrenScale() {
        //return mIsHotseat ? mHotseatScale : 1.0f;
        return 1.0f;
    }

    public boolean addViewToCellLayout(View child, int index, int childId, LayoutParams params,
                                       boolean markCells) {
        final LayoutParams lp = params;

        // Hotseat icons - remove text
        if (child instanceof BubbleTextView) {
            Log.v(TAG, "addViewToCellLayout BubbleTextView");
            BubbleTextView bubbleChild = (BubbleTextView) child;
            bubbleChild.setTextVisibility(true);
        }
        Log.v(TAG, "addViewToCellLayout getChildrenScale = " + getChildrenScale());
        child.setScaleX(getChildrenScale());
        child.setScaleY(getChildrenScale());

        if (lp.cellX >= 0 && lp.cellX <= mCountX - 1 && lp.cellY >= 0 && lp.cellY <= mCountY - 1) {
            if (lp.cellHSpan < 0) lp.cellHSpan = mCountX;
            if (lp.cellVSpan < 0) lp.cellVSpan = mCountY;

            child.setId(childId);
            mShortcutsAndWidgets.addView(child, index, lp);
            Log.v(TAG, "addView child.getText = " + ((TextView)child).getText());

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

    public static class LayoutParams extends ViewGroup.MarginLayoutParams{
        /**
         * Horizontal location of the item in the grid.
         */
        public int cellX;

        /**
         * Vertical location of the item in the grid.
         */
        public int cellY;
        //cellHSpan Width in cells
        public int cellHSpan;
        //cellVSpan Height in cells
        public int cellVSpan;

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
    }
}
