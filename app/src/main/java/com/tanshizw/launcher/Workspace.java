package com.tanshizw.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
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
import com.tanshizw.launcher.items.BubbleTextView;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by user on 6/7/16.
 */
public class Workspace extends SmoothPagedView implements Insettable, DragSource, DragController.DragListener{
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

    // DragController.DragListener
    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {

    }

    @Override
    public void onDragEnd() {

    }
}
