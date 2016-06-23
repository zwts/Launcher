package com.tanshizw.launcher;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.tanshizw.launcher.utility.LauncherSettings;
import com.tanshizw.launcher.view.PageIndicator;

public class PagedView extends ViewGroup {
    protected int mCurrentPage;
    private static final String TAG = "SmoothPagedView";

    //page indicator
    private int mPageIndicatorViewId;
    private PageIndicator mPageIndicator;

    protected final static int TOUCH_STATE_REST = 0;
    protected final static int TOUCH_STATE_SCROLLING = 1;
    protected final static int TOUCH_STATE_PREV_PAGE = 2;
    protected final static int TOUCH_STATE_NEXT_PAGE = 3;
    protected final static int TOUCH_STATE_INVALIDE = 4;

    protected int mTouchState = TOUCH_STATE_REST;

    protected float mLastMotionX;
    protected float mLastMotionY;

    protected boolean mIsPageMoving = false;

    public PagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.PagedView, 0, 0);
        mPageIndicatorViewId = a.getResourceId(R.styleable.PagedView_pageIndicator, -1);
    }

    public PagedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        mCurrentPage = 0;
    }

    int getCurrentPage() {
        Log.v(TAG, "mCurrentPage = " + mCurrentPage);
        return mCurrentPage;
    }

    void setCurrentPage(int index) {
        Log.v(TAG, "setCurrentPage index = " + index);
        if (index < getChildCount()) {
            mCurrentPage = index;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.v(TAG, "onLayout l = " + l + "; t = " + t + "; r = " + r + "; b = " + b);
        Log.v(TAG, "onLayout getChildCount() = " + getChildCount());
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(getChildCount() - i - 1);
            child.layout(l, t, r, b);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewGroup grandParent = (ViewGroup) getParent();
        if (grandParent instanceof DragLayer) {
            Log.v(TAG, "onAttachedToWindow parent is DragLayer");
        }
        if (mPageIndicator == null && mPageIndicatorViewId > -1) {
            mPageIndicator = (PageIndicator) grandParent.findViewById(mPageIndicatorViewId);
            mPageIndicator.setActiveMarkerIndex(mCurrentPage);
            mPageIndicator.removeAllMarkers();

            for (int i = 0; i < getChildCount(); ++i) {
                mPageIndicator.addMarker();
            }
        }
        mPageIndicator.offsetWindowCenterTo();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);

        // Skip touch handling if there are no pages to swipe
        if (getChildCount() <= 0) return super.onTouchEvent(ev);

        final int action = ev.getAction();
        final float x = ev.getX();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaX = mLastMotionX - x;
                Log.v(TAG, "deltaX = " + deltaX);
                Log.v(TAG, "mTouchState = " + mTouchState);
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    if (Math.abs(deltaX) >= 1.0f) {
                        scrollTo((int) (mCurrentPage * LauncherSettings.SCREEN_WIDTH + deltaX), 0);
                    }
                    if (Math.abs(deltaX) >= LauncherSettings.SNAP_SCREEN_GAP) {
                        if (deltaX > 0 && canSnapNext()) {
                            mTouchState = TOUCH_STATE_NEXT_PAGE;
                        } else if (deltaX < 0 && canSnapPrev()) {
                            mTouchState = TOUCH_STATE_PREV_PAGE;
                        } else {
                            mTouchState = TOUCH_STATE_INVALIDE;
                        }
                    }
                } else if (mTouchState == TOUCH_STATE_NEXT_PAGE || mTouchState == TOUCH_STATE_PREV_PAGE) {
                    if (Math.abs(deltaX) >= 1.0f) {
                        scrollTo((int) (mCurrentPage * LauncherSettings.SCREEN_WIDTH + deltaX), 0);
                    }
                } else if (mTouchState == TOUCH_STATE_INVALIDE) {
                    int constDeltaX;
                    if (deltaX > 0) {
                        constDeltaX = LauncherSettings.SNAP_SCREEN_GAP;
                    } else {
                        constDeltaX = -LauncherSettings.SNAP_SCREEN_GAP;
                    }
                    scrollTo(mCurrentPage * LauncherSettings.SCREEN_WIDTH + constDeltaX, 0);
                } else {
                    determineScrollingStart(ev);
                }
                break;
            case MotionEvent.ACTION_UP:
                int currentPage = getCurrentPage();
                if (Math.abs(x - mLastMotionX) > (LauncherSettings.SCREEN_WIDTH / 2)) {
                    if (mTouchState == TOUCH_STATE_PREV_PAGE) {
                        Log.v(TAG, "need snap to prev page");
                        if (currentPage > 0) {
                            currentPage--;
                        }
                    } else if (mTouchState == TOUCH_STATE_NEXT_PAGE) {
                        Log.v(TAG, "need snap to next page");
                        if (currentPage < (getChildCount() - 1)) {
                            currentPage++;
                        }
                    }
                }
                snapPage(currentPage);
                break;
        }
        return true;
    }

    /* determine start scroll or not
    * */
    protected void determineScrollingStart(MotionEvent ev) {
        final float x = ev.getX();
        final int xDiff = (int) Math.abs(x - mLastMotionX);
        boolean xMoved = xDiff > LauncherSettings.SLIDE_PAGE_DELTAX;
        Log.v(TAG, "determineScrollingStart xMoved = " + xMoved);
        if (xMoved) {
            mTouchState = TOUCH_STATE_SCROLLING;
            pageBeginMoving();
        }
    }

    protected void pageBeginMoving() {
        if (!mIsPageMoving) {
            mIsPageMoving = true;
        }
    }

    protected void snapPage(int pageId) {
        Log.v(TAG, "snapPage pageId = " + pageId);
        setCurrentPage(pageId);
        if (mPageIndicator != null) {
            mPageIndicator.setActiveMarkerIndex(mCurrentPage);
        }
        scrollTo(LauncherSettings.SCREEN_WIDTH * mCurrentPage, 0);
        mIsPageMoving = false;
        mTouchState = TOUCH_STATE_REST;
    }

    protected boolean canSnapPrev() {
        return (getCurrentPage() > 0);
    }

    protected boolean canSnapNext() {
        return ((getCurrentPage() < getChildCount() - 1));
    }
}
