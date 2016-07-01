package com.tanshizw.launcher.drag;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.tanshizw.launcher.Launcher;
import com.tanshizw.launcher.R;

import java.util.ArrayList;

/**
 * Created by archermind on 6/29/16.
 */
public class DragController {
    private static final String TAG = "Launcher.drag.DragController";
    private Launcher mLauncher;
    /** Indicates the drag is a move.  */
    public static final int DRAG_ACTION_MOVE = 0;
    /** Indicates the drag is a copy.  */
    public static final  int DRAG_ACTION_COPY = 1;
    /** X coordinate of the down event. */
    private int mMotionDownX;
    /** Y coordinate of the down event. */
    private int mMotionDownY;
    /** Whether or not we're dragging. */
    private boolean mDragging = false;
    private ArrayList<DragListener> mListeners = new ArrayList<>();
    private DropTarget.DragObject mDragObject;
    private Rect mDragLayerRect = new Rect();
    private int mTmpPoint[] = new int[2];
    private DropTarget mLastDropTarget;

    /**
     * Interface to receive notifications when a drag starts or stops
     */
    public interface DragListener {
        /**
         * A drag has begun
         *
         * @param source An object representing where the drag originated
         * @param info The data associated with the object that is being dragged
         * @param dragAction The drag action: either {@link DragController#DRAG_ACTION_MOVE}
         *        or {@link DragController#DRAG_ACTION_COPY}
         */
        void onDragStart(DragSource source, Object info, int dragAction);

        /**
         * The drag has ended
         */
        void onDragEnd();
    }

    public DragController(Launcher launcher) {
        mLauncher = launcher;
    }

    public boolean isDragging() {
        return mDragging;
    }

    public DragView startDrag(Bitmap b, int dragLayerX, int dragLayerY, DragSource source,
                              Object dragInfo, int dragAction, Point dragOffset,
                              Rect dragRegion, float initialDragViewScale, boolean accessible) {
        for (DragListener listener : mListeners) {
            listener.onDragStart(source, dragInfo, dragAction);
        }

        final int registrationX = mMotionDownX - dragLayerX;//
        final int registrationY = mMotionDownY - dragLayerY;

        final int dragRegionLeft = dragRegion == null ? 0 : dragRegion.left;
        final int dragRegionTop = dragRegion == null ? 0 : dragRegion.top;

        mDragging = true;
        mDragObject = new DropTarget.DragObject();
        mDragObject.dragComplete = false;
        mDragObject.xOffset = mMotionDownX - (dragLayerX + dragRegionLeft);
        mDragObject.yOffset = mMotionDownY - (dragLayerY + dragRegionTop);
        mDragObject.dragSource = source;
        mDragObject.dragInfo = dragInfo;

        final DragView dragView = mDragObject.dragView = new DragView(mLauncher, b, registrationX,
                registrationY, 0, 0, b.getWidth(), b.getHeight(), initialDragViewScale);
        if (dragOffset != null) {
            dragView.setDragVisualizeOffset(new Point(dragOffset));
        }
        if (dragRegion != null) {
            dragView.setDragRegion(new Rect(dragRegion));
        }

        dragView.show(mMotionDownX, mMotionDownY);
        /*handleMoveEvent(mMotionDownX, mMotionDownY);*/
        return dragView;
    }

    /**
     * Sets the drag listener which will be notified when a drag starts or ends.
     */
    public void addDragListener(DragListener l) {
        mListeners.add(l);
    }

    /**
     * Call this from a drag source view.
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Update the velocity tracker
        /*acquireVelocityTrackerAndAddMovement(ev);*/

        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;
                mLastDropTarget = null;
                break;
            case MotionEvent.ACTION_UP:
/*                mLastTouchUpTime = System.currentTimeMillis();
                if (mDragging) {
                    PointF vec = isFlingingToDelete(mDragObject.dragSource);
                    if (!DeleteDropTarget.supportsDrop(mDragObject.dragInfo)) {
                        vec = null;
                    }
                    if (vec != null) {
                        dropOnFlingToDeleteTarget(dragLayerX, dragLayerY, vec);
                    } else {
                        drop(dragLayerX, dragLayerY);
                    }
                }
                endDrag();*/
                break;
            case MotionEvent.ACTION_CANCEL:
/*                cancelDrag();*/
                break;
        }

        return mDragging;
    }

    /**
     * Clamps the position to the drag layer bounds.
     */
    private int[] getClampedDragLayerPos(float x, float y) {
        mLauncher.getDragLayer().getLocalVisibleRect(mDragLayerRect);
        mTmpPoint[0] = (int) Math.max(mDragLayerRect.left, Math.min(x, mDragLayerRect.right - 1));
        mTmpPoint[1] = (int) Math.max(mDragLayerRect.top, Math.min(y, mDragLayerRect.bottom - 1));
        return mTmpPoint;
    }
}
