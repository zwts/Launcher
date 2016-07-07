package com.tanshizw.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tanshizw.launcher.drag.DragController;
import com.tanshizw.launcher.drag.DragSource;
import com.tanshizw.launcher.drag.DragView;
import com.tanshizw.launcher.drag.DropTarget;
import com.tanshizw.launcher.items.BubbleTextView;
import com.tanshizw.launcher.items.ItemInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by user on 6/7/16.
 */
public class Workspace extends SmoothPagedView implements Insettable, DragSource,DropTarget, DragController.DragListener{
    private static final String TAG = "Launcher.Workspace";
    private HashMap<Long, CellLayout> mWorkspaceScreens = new HashMap<Long, CellLayout>();
    private Launcher mLauncher;
    private CellLayout.CellInfo mDragInfo;
    private int DRAG_BITMAP_PADDING = 2;
    private final Canvas mCanvas = new Canvas();
    private static final Rect sTempRect = new Rect();
    private final int[] mTempXY = new int[2];
    private ShortcutAndWidgetContainer mDragSourceInternal;
    private DragController mDragController;
    protected OnLongClickListener mLongClickListener;
    private float[] mDragViewVisualCenter = new float[2];
    boolean mAnimatingViewIntoPlace = false;
    /**
     * The CellLayout that is currently being dragged over
     */
    private CellLayout mDragTargetLayout = null;
    /**
     * The CellLayout which will be dropped to
     */
    private CellLayout mDropToLayout = null;
    /**
     * Target drop area calculated during last acceptDrop call.
     */
    private int[] mTargetCell = new int[2];

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

    void setup(DragController dragController) {
        /*mSpringLoadedDragController = new SpringLoadedDragController(mLauncher);*/
        mDragController = dragController;

        // hardware layers on children are enabled on startup, but should be disabled until
        // needed
        /*updateChildrenLayersEnabled(false);*/
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

        if (child instanceof DropTarget) {
            mDragController.addDropTarget((DropTarget) child);
        }

        child.setHapticFeedbackEnabled(false);
        child.setOnLongClickListener(mLongClickListener);
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

    public void startDrag(CellLayout.CellInfo cellInfo) {
        View child = cellInfo.cell;

        // Make sure the drag was started by a long press as opposed to a long click.
        if (!child.isInTouchMode()) {
            return;
        }

        mDragInfo = cellInfo;
        child.setVisibility(INVISIBLE);
        CellLayout layout = (CellLayout) child.getParent().getParent();
        layout.prepareChildForDrag(child);

        beginDragShared(child, new Point(), this, false);
    }

    public void beginDragShared(View child, Point relativeTouchPos,
                                DragSource source, boolean accessible) {
        child.clearFocus();
        child.setPressed(false);

        // The outline is used to visualize where the item will land if dropped
        //TODO: mDragOutline = createDragOutline(child, DRAG_BITMAP_PADDING);

        // The drag bitmap follows the touch point around on the screen
        AtomicInteger padding = new AtomicInteger(DRAG_BITMAP_PADDING);
        final Bitmap b = createDragBitmap(child, padding);

        final int bmpWidth = b.getWidth();
        final int bmpHeight = b.getHeight();

        float scale = mLauncher.getDragLayer().getLocationInDragLayer(child, mTempXY);
        int dragLayerX = Math.round(mTempXY[0] - (bmpWidth - scale * child.getWidth()) / 2);
        int dragLayerY = Math.round(mTempXY[1] - (bmpHeight - scale * bmpHeight) / 2 - padding.get() /2);

        Point dragVisualizeOffset = null;
        Rect dragRect = null;

        if (child instanceof BubbleTextView) {
            BubbleTextView icon = (BubbleTextView) child;
            int iconSize = 60;
            int left = (bmpWidth - iconSize) / 2; //why /2 ?
            int top = child.getPaddingTop();
            int right = left + iconSize;
            int bottom = top + iconSize;

            dragLayerY += top;
            // Note: The drag region is used to calculate drag layer offsets, but the
            // dragVisualizeOffset in addition to the dragRect (the size) to position the outline.
            dragVisualizeOffset = new Point(-padding.get() / 2, padding.get()/2);
            dragRect = new Rect(left, top, right, bottom);
        }

        if (child.getParent() instanceof ShortcutAndWidgetContainer) {
            mDragSourceInternal = (ShortcutAndWidgetContainer) child.getParent();
        }

        DragView dv = mDragController.startDrag(b, dragLayerX, dragLayerY, source, child.getTag(),
                DragController.DRAG_ACTION_MOVE, dragVisualizeOffset, dragRect, scale, accessible);
        dv.setIntrinsicIconScaleFactor(source.getIntrinsicIconScaleFactor());

        b.recycle();
    }

    public Bitmap createDragBitmap(View v, AtomicInteger expectedPadding) {
        Bitmap b;
        int padding = expectedPadding.get();

        if (v instanceof TextView) {
            Drawable d = getTextViewIcon((TextView) v);
            Rect bounds = getDrawableBounds(d);
            b = Bitmap.createBitmap(bounds.width() + padding, bounds.height() + padding,
                    Bitmap.Config.ARGB_8888);
            expectedPadding.set(padding - bounds.left - bounds.top);//what's this?
        } else {
            b = Bitmap.createBitmap(v.getWidth() + padding, v.getHeight() + padding,
                    Bitmap.Config.ARGB_8888);
        }

        mCanvas.setBitmap(b);
        drawDragView(v, mCanvas, padding);
        mCanvas.setBitmap(null);

        return b;
    }

    /**
     * Draw the View v into the given Canvas.
     *
     * @param v the view to draw
     * @param destCanvas the canvas to draw on
     * @param padding the horizontal and vertical padding to use when drawing
     */
    private static void drawDragView(View v, Canvas destCanvas, int padding) {
        final Rect clipRect = sTempRect;
        v.getDrawingRect(clipRect);

        destCanvas.save();//what's this？
        if (v instanceof TextView) {
            Drawable d = getTextViewIcon((TextView) v);
            Rect bounds = getDrawableBounds(d);
            clipRect.set(0, 0, bounds.width() + padding, bounds.height() + padding);
            destCanvas.translate(padding / 2 - bounds.left, padding / 2 - bounds.top);
            d.draw(destCanvas);
        } else {
            destCanvas.translate(-v.getScrollX() + padding / 2, -v.getScrollY() + padding / 2);
            destCanvas.clipRect(clipRect, Region.Op.REPLACE);
            v.draw(destCanvas);
        }
        destCanvas.restore();
    }

    /**
     * Returns the drawable for the given text view.
     */
    public static Drawable getTextViewIcon(TextView tv) {
        final Drawable[] drawables = tv.getCompoundDrawables();
        for (int i = 0; i < drawables.length; i++) {
            if (drawables[i] != null) {
                return drawables[i];
            }
        }
        return null;
    }

    /**
     *
     * We call these methods (onDragStartedWithItemSpans/onDragStartedWithSize) whenever we
     * start a drag in Launcher, regardless of whether the drag has ever entered the Workspace
     *
     * These methods mark the appropriate pages as accepting drops (which alters their visual
     * appearance).
     *
     */
    private static Rect getDrawableBounds(Drawable d) {
        Rect bounds = new Rect();
        d.copyBounds(bounds);
        if (bounds.width() == 0 || bounds.height() == 0) {
            bounds.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        } else {
            bounds.offsetTo(0, 0);
        }

        return bounds;
    }

    /*
     *
     * Convert the 2D coordinate xy from the parent View's coordinate space to this CellLayout's
     * coordinate space. The argument xy is modified with the return result.
     *
     * if cachedInverseMatrix is not null, this method will just use that matrix instead of
     * computing it itself; we use this to avoid redundant matrix inversions in
     * findMatchingPageForDragOver
     *
     */
    void mapPointFromSelfToChild(View v, float[] xy, Matrix cachedInverseMatrix) {
        xy[0] = xy[0] - v.getLeft();
        xy[1] = xy[1] - v.getTop();
    }

    /**
     * Calculate the nearest cell where the given object would be dropped.
     *
     * pixelX and pixelY should be in the coordinate system of layout
     */
    private int[] findNearestArea(int pixelX, int pixelY,
                                 int spanX, int spanY, CellLayout layout, int[] recycle) {
        return layout.findNearestArea(
                pixelX, pixelY, spanX, spanY, recycle);
    }

    /**
     * Returns a specific CellLayout
     */
    CellLayout getParentCellLayoutForView(View v) {
        ArrayList<CellLayout> layouts = getWorkspaceAndHotseatCellLayouts();
        for (CellLayout layout : layouts) {
            if (layout.getShortcutsAndWidgets().indexOfChild(v) > -1) {
                return layout;
            }
        }
        return null;
    }

    /**
     * Returns a list of all the CellLayouts in the workspace.
     */
    ArrayList<CellLayout> getWorkspaceAndHotseatCellLayouts() {
        ArrayList<CellLayout> layouts = new ArrayList<CellLayout>();
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            layouts.add(((CellLayout) getChildAt(screen)));
        }
        if (mLauncher.getHotseat() != null) {
            layouts.add(mLauncher.getHotseat().getLayout());
        }
        return layouts;
    }

    /**
     * Registers the specified listener on each page contained in this workspace.
     *
     * @param l The listener used to respond to long clicks.
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClickListener = l;
/*        final int count = getPageCount();
        for (int i = 0; i < count; i++) {
            getPageAt(i).setOnLongClickListener(l);
        }*/
        super.setOnLongClickListener(l);
    }

    @Override
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 0;
    }

    @Override
    public void onFlingToDeleteCompleted() {

    }

    @Override
    public void onDropCompleted(View target, DragObject d, boolean isFlingToDelete, boolean success) {

    }

    // DragController.DragListener
    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {

    }

    @Override
    public void onDragEnd() {

    }

    //DropTarget
    @Override
    public boolean isDropEnabled() {
        return true;
    }

    @Override
    public void onDrop(DragObject d) {
        mDragViewVisualCenter = d.getVisualCenter(mDragViewVisualCenter);
        CellLayout dropTargetLayout = mDropToLayout;

        // We want the point to be mapped to the dragTarget.
        if (dropTargetLayout != null) {
            //TODO：HOTSEAT mapPointFromSelfToHotseatLayout(mLauncher.getHotseat(), mDragViewVisualCenter);
            mapPointFromSelfToChild(dropTargetLayout, mDragViewVisualCenter, null);
        }

        if (d.dragSource != this) {
            //TODO: onDropExternal
        } else if (mDragInfo != null) {
            final View cell =  mDragInfo.cell;
            if (dropTargetLayout != null && !d.cancelled) {
                boolean hasMovedLayouts = (getParentCellLayoutForView(cell) != dropTargetLayout);
                long container = LauncherSettings.CONTAINER_DESKTOP;
                long screenId = mDragInfo.screenId;
                int spanX = mDragInfo != null ? mDragInfo.spanX : 1;
                int spanY = mDragInfo != null ? mDragInfo.spanY : 1;

                // First we find the cell nearest to point at which the item is
                // dropped, without any consideration to whether there is an item there.
                mTargetCell = findNearestArea((int) mDragViewVisualCenter[0], (int)
                        mDragViewVisualCenter[1], spanX, spanY, dropTargetLayout, mTargetCell);
                float distance = dropTargetLayout.getDistanceFromCell(mDragViewVisualCenter[0],
                        mDragViewVisualCenter[1], mTargetCell);

                // If the item being dropped is a shortcut and the nearest drop
                // cell also contains a shortcut, then create a folder with the two shortcuts.
                //TODO create folder if a shortcut to a shortcut

                // Aside from the special case where we're dropping a shortcut onto a shortcut,
                // we need to find the nearest cell location that is vacant
                ItemInfo item = (ItemInfo) d.dragInfo;
                int minSpanX = item.spanX;
                int minSpanY = item.spanY;
                int[] resultSpan = new int[2];
                boolean foundCell = mTargetCell[0] >= 0 && mTargetCell[1] >= 0;

                if (foundCell) {
                    final ItemInfo info = (ItemInfo) cell.getTag();
                    if (hasMovedLayouts) {
                        // Reparent the view
                        CellLayout parentCell = getParentCellLayoutForView(cell);
                        if (parentCell != null) {
                            parentCell.removeView(cell);
                        }
                        addInScreenFromBind(cell, container, screenId, mTargetCell[0], mTargetCell[1],
                                info.spanX, info.spanY);
                    }

                    // update the item's position after drop
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                    lp.cellX = mTargetCell[0];
                    lp.cellY = mTargetCell[1];
                    lp.cellHSpan = item.spanX;
                    lp.cellVSpan = item.spanY;
                } else {
                    // If we can't find a drop location, we return the item to its original position
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                    mTargetCell[0] = lp.cellX;
                    mTargetCell[1] = lp.cellY;
                    CellLayout layout = (CellLayout) cell.getParent().getParent();
                    layout.markCellsAsOccupiedForView(cell);
                }

                final CellLayout parent = (CellLayout) cell.getParent().getParent();
                final Runnable onCompleteRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mAnimatingViewIntoPlace = false;
                    }
                };
                mAnimatingViewIntoPlace = true;

                if (d.dragView.hasDrawn()) {
                    final ItemInfo info = (ItemInfo) cell.getTag();
                    int duration = -1; // current screen
                    mLauncher.getDragLayer().animateViewIntoPosition(d.dragView, cell, duration,
                            onCompleteRunnable, this);
                } else {
                    d.deferDragViewCleanupPostAnimation = false;
                    cell.setVisibility(VISIBLE);
                }

                parent.onDropChild(cell);
            }
        }
    }

    @Override
    public void onDragEnter(DragObject dragObject) {
        mDropToLayout = null;
        CellLayout layout = getCurrentDropLayout();
        setCurrentDropLayout(layout);
    }

    void setCurrentDropLayout(CellLayout layout) {
/*        if (mDragTargetLayout != null) {
            mDragTargetLayout.revertTempState();
            mDragTargetLayout.onDragExit();
        }*/
        mDragTargetLayout = layout;
/*        if (mDragTargetLayout != null) {
            mDragTargetLayout.onDragEnter();
        }
        cleanupReorder(true);
        cleanupFolderCreation();
        setCurrentDropOverCell(-1, -1);*/
    }

    /**
     * Return the current {@link CellLayout}, correctly picking the destination
     * screen while a scroll is in progress.
     */
    public CellLayout getCurrentDropLayout() {
        return (CellLayout) getChildAt(getNextPage());
    }

    /**
     * Returns the index of page to be shown immediately afterwards.
     */
    int getNextPage() {
        return (mNextPage != INVALID_PAGE) ? mNextPage : mCurrentPage;
    }

    @Override
    public void onDragOver(DragObject dragObject) {

    }

    @Override
    public void onDragExit(DragObject d) {
/*        // Here we store the final page that will be dropped to, if the workspace in fact
        // receives the drop
        if (mInScrollArea) {
            if (isPageMoving()) {
                // If the user drops while the page is scrolling, we should use that page as the
                // destination instead of the page that is being hovered over.
                mDropToLayout = (CellLayout) getPageAt(getNextPage());
            } else {
                mDropToLayout = mDragOverlappingLayout;
            }
        } else {
            mDropToLayout = mDragTargetLayout;
        }

        if (mDragMode == DRAG_MODE_CREATE_FOLDER) {
            mCreateUserFolderOnDrop = true;
        } else if (mDragMode == DRAG_MODE_ADD_TO_FOLDER) {
            mAddToExistingFolderOnDrop = true;
        }

        // Reset the scroll area and previous drag target
        onResetScrollArea();
        setCurrentDropLayout(null);
        setCurrentDragOverlappingLayout(null);

        mSpringLoadedDragController.cancel();

        mLauncher.getDragLayer().hidePageHints();*/
        mDropToLayout = mDragTargetLayout;
    }

    @Override
    public void onFlingToDelete(DragObject dragObject, PointF vec) {

    }

    @Override
    public boolean acceptDrop(DragObject d) {
        CellLayout dropTargetLayout = mDropToLayout;
        // TODO:If it's an external drop (e.g. from All Apps), check if it should be accepted
        if (d.dragSource != this) {

        }
/*        long screenId = getIdForScreen(dropTargetLayout);
        if (screenId == EXTRA_EMPTY_SCREEN_ID) {
            commitExtraEmptyScreen();
        }*/

        return true;
    }

    @Override
    public void prepareAccessibilityDrop() {

    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        // We want the workspace to have the whole area of the display (it will find the correct
        // cell layout to drop to in the existing drag/drop logic.
        mLauncher.getDragLayer().getDescendantRectRelativeToSelf(this, outRect);
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {

    }
}
