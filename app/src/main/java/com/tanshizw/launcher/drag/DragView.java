package com.tanshizw.launcher.drag;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.tanshizw.launcher.DragLayer;
import com.tanshizw.launcher.Launcher;
import com.tanshizw.launcher.R;

/**
 * Created by archermind on 6/29/16.
 */
public class DragView extends View {
    private Paint mPaint;
    private float mInitialScale = 1f;
    private DragLayer mDragLayer = null;
    private Bitmap mBitmap;
    private Rect mDragRegion = null;
    private int mRegistrationX;
    private int mRegistrationY;
    private Point mDragVisualizeOffset = null;
    ValueAnimator mAnim;
    private float mOffsetX = 0.0f;
    private float mOffsetY = 0.0f;
    private boolean mHasDrawn = false;
    // The intrinsic icon scale factor is the scale factor for a drag icon over the workspace
    // size.  This is ignored for non-icons.
    private float mIntrinsicIconScale = 1f;

    public DragView(Context context) {
        super(context);
    }

    public DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DragView(Launcher launcher, Bitmap bitmap, int registrationX, int registrationY,
                    int left, int top, int width, int height, final float initialScale) {
        super(launcher);
        mDragLayer = launcher.getDragLayer();
        mInitialScale = initialScale;

        final Resources res = getResources();
        final float scaleDps = res.getDimensionPixelSize(R.dimen.dragViewScale);
        final float scale = (width + scaleDps) / width;

        // Set the initial scale to avoid any jumps
        setScaleX(initialScale);
        setScaleY(initialScale);

        // Animate the view into the correct position
        mAnim = ofFloat(this, 0f, 1f);
        mAnim.setDuration(150);
        mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (Float) animation.getAnimatedValue();

                final int deltaX = (int) (-mOffsetX);
                final int deltaY = (int) (-mOffsetY);

                mOffsetX += deltaX;
                mOffsetY += deltaY;
                setScaleX(initialScale + (value * (scale - initialScale)));
                setScaleY(initialScale + (value * (scale - initialScale)));

                if (getParent() == null) {
                    animation.cancel();
                } else {
                    setTranslationX(getTranslationX() + deltaX);
                    setTranslationY(getTranslationY() + deltaY);
                }
            }
        });

        mBitmap = Bitmap.createBitmap(bitmap, left, top, width, height);
        setDragRegion(new Rect(0, 0, width, height));

        // The point in our scaled bitmap that the touch events are located
        mRegistrationX = registrationX;
        mRegistrationY = registrationY;

        // Force a measure, because Workspace uses getMeasuredHeight() before the layout pass
        int ms = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        measure(ms, ms);

        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    }

    public static ValueAnimator ofFloat(View target, float... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setFloatValues(values);
        /*cancelOnDestroyActivity(anim);*/
        return anim;
    }

    public void setDragVisualizeOffset(Point p) {
        mDragVisualizeOffset = p;
    }

    /**
     * Create a window containing this view and show it.
     *
     * @param touchX the x coordinate the user touched in DragLayer coordinates
     * @param touchY the y coordinate the user touched in DragLayer coordinates
     */
    public void show(int touchX, int touchY) {
        mDragLayer.addView(this);

        // Start the pick-up animation
        DragLayer.LayoutParams lp = new DragLayer.LayoutParams(0, 0);
        lp.width = mBitmap.getWidth();
        lp.height = mBitmap.getHeight();
        /*lp.customPosition = true;*/
        setLayoutParams(lp);
        setTranslationX(touchX - mRegistrationX);
        setTranslationY(touchY - mRegistrationY);
        // Post the animation to skip other expensive work happening on the first frame
        post(new Runnable() {
            public void run() {
                mAnim.start();
            }
        });
    }

    /** Sets the scale of the view over the normal workspace icon size. */
    public void setIntrinsicIconScaleFactor(float scale) {
        mIntrinsicIconScale = scale;
    }

    public void cancelAnimation() {
        if (mAnim != null && mAnim.isRunning()) {
            mAnim.cancel();
        }
    }

    public void resetLayoutParams() {
        mOffsetX = mOffsetY = 0;
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mHasDrawn = true;
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
    }

    /**
     * Move the window containing this view.
     *
     * @param touchX the x coordinate the user touched in DragLayer coordinates
     * @param touchY the y coordinate the user touched in DragLayer coordinates
     */
    void move(int touchX, int touchY) {
        setTranslationX(touchX - mRegistrationX + (int) mOffsetX);
        setTranslationY(touchY - mRegistrationY + (int) mOffsetY);
    }

    void remove() {
        if (getParent() != null) {
            mDragLayer.removeView(DragView.this);
        }
    }

    public void setDragRegion(Rect r) {
        mDragRegion = r;
    }

    public Rect getDragRegion() {
        return mDragRegion;
    }

    public boolean hasDrawn() {
        return mHasDrawn;
    }

    public Point getDragVisualizeOffset() {
        return mDragVisualizeOffset;
    }

    public float getIntrinsicIconScaleFactor() {
        return mIntrinsicIconScale;
    }
}
