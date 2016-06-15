package com.tanshizw.launcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Launcher extends Activity {
    //private Button chooseWallpaperBt;
    //private Button allApplicationBt;
    private IconCache mIconCache;
    private AllAppsList mBgAllAppsList;
    private LauncherAppsCompat mLauncherApps;
    private HashMap<Object, CharSequence> mLabelCache;
    private Context mContext;

    DragLayer mDragLayer;
    Workspace mWorkspace;
    ArrayList<ItemInfo> workspaceItems = new ArrayList<ItemInfo>();
    private HashMap<Integer, Integer> mItemIdToViewId = new HashMap<Integer, Integer>();
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    ArrayList<Long> orderedScreenIds = new ArrayList<Long>();;
    private LayoutInflater mInflater;
    private static final int ITEMS_CHUNK = 6; // batch size for the workspace icons
    static final int CONTAINER_DESKTOP = -100;
    static final int CONTAINER_HOTSEAT = -101;
    /**
     * The gesture is an application
     */
    static final int ITEM_TYPE_APPLICATION = 0;

    /**
     * The gesture is an application created shortcut
     */
    static final int ITEM_TYPE_SHORTCUT = 1;
    private final String TAG = "Launcher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Log.v(TAG, "onCreate");
        mInflater = getLayoutInflater();
        setupViews();

        Long screenId = Long.valueOf(0);
        orderedScreenIds.add(screenId);
        orderedScreenIds.add(screenId + 1);//two screens
        bindAddScreens(orderedScreenIds);

        setupWorkspaceItems();
    }

    private void setupViews(){
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace = (Workspace) mDragLayer.findViewById(R.id.workspace);
    }

    private void setupWorkspaceItems(){
        mIconCache = new IconCache(this);
        mBgAllAppsList = new AllAppsList(mIconCache);
        mLauncherApps = LauncherAppsCompat.getInstance(this);
        mLabelCache = new HashMap<Object, CharSequence>();
        mContext = getApplicationContext();

        List<LauncherActivityInfoCompat> apps = mLauncherApps.getActivityList(null);
        if (apps == null || apps.isEmpty()) {
            return;
        }

        Collections.sort(apps, new ShortcutNameComparator(mLabelCache));

        for (int i = 0; i < apps.size(); i++) {
            LauncherActivityInfoCompat app = apps.get(i);
            // This builds the icon bitmaps.
            mBgAllAppsList.add(new AppInfo(mContext, app, mIconCache, mLabelCache));
        }

        for (int i = 0; i < 3; i++) {
            AppInfo app = mBgAllAppsList.get(i);
            ShortcutInfo shortcut = new ShortcutInfo(app);
            shortcut.container = CONTAINER_DESKTOP;
            shortcut.itemType = ITEM_TYPE_SHORTCUT;
            shortcut.screenId = 0;
            shortcut.id = 0;
            shortcut.cellX = 0 + i;
            shortcut.cellY = 0;
            shortcut.spanX = 1;
            shortcut.spanY = 1;
            workspaceItems.add(shortcut);
        }
        bindWorkspaceItems(workspaceItems);
    }

    public static class ShortcutNameComparator implements Comparator<LauncherActivityInfoCompat> {
        private Collator mCollator;
        private HashMap<Object, CharSequence> mLabelCache;
        ShortcutNameComparator(HashMap<Object, CharSequence> labelCache) {
            mLabelCache = labelCache;
            mCollator = Collator.getInstance();
        }
        public final int compare(LauncherActivityInfoCompat a, LauncherActivityInfoCompat b) {
            String labelA, labelB;
            ComponentName keyA = a.getComponentName();
            ComponentName keyB = b.getComponentName();
            if (mLabelCache.containsKey(keyA)) {
                labelA = mLabelCache.get(keyA).toString();
            } else {
                labelA = a.getLabel().toString().trim();

                mLabelCache.put(keyA, labelA);
            }
            if (mLabelCache.containsKey(keyB)) {
                labelB = mLabelCache.get(keyB).toString();
            } else {
                labelB = b.getLabel().toString().trim();

                mLabelCache.put(keyB, labelB);
            }
            return mCollator.compare(labelA, labelB);
        }
    };

    public void bindAddScreens(ArrayList<Long> orderedScreenIds) {
        int count = orderedScreenIds.size();
        for (int i = 0; i < count; i++) {
            mWorkspace.insertNewWorkspaceScreen(orderedScreenIds.get(i));
        }
    }

    private void bindWorkspaceItems(final ArrayList<ItemInfo> workspaceItems){
        // Bind the workspace items
        int N = workspaceItems.size();
        Log.v(TAG, "bindWorkspaceItems N = " + N);
        for (int i = 0; i < N; i += ITEMS_CHUNK) {
            final int start = i;
            final int chunkSize = (i+ITEMS_CHUNK <= N) ? ITEMS_CHUNK : (N-i);
            Log.v(TAG, "bindWorkspaceItems chunkSize = " + chunkSize);
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                        Log.v(TAG, "bindWorkspaceItems run");
                        bindItems(workspaceItems, start, start+chunkSize);
                    }
                };
            r.run();
            }
    }

    View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
        Log.v(TAG, "createShortcut");
        BubbleTextView favorite = (BubbleTextView) mInflater.inflate(layoutResId, parent, false);
        favorite.applyFromShortcutInfo(info, mIconCache, true);
        //favorite.setOnClickListener(this);
        //favorite.setOnFocusChangeListener(mFocusHandler);
        return favorite;
    }

    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.application,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentPage()), info);
    }

    public void bindItems(final ArrayList<ItemInfo> shortcuts, final int start, final int end){
        Log.v(TAG, "bindItems");
        Workspace workspace = mWorkspace;
        for (int i = start; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);

            switch (item.itemType){
                case ITEM_TYPE_APPLICATION:
                case ITEM_TYPE_SHORTCUT:
                    ShortcutInfo info = (ShortcutInfo) item;
                    View shortcut = createShortcut(info);//BubbleTextView
                    workspace.addInScreenFromBind(shortcut, item.container, item.screenId, item.cellX,
                            item.cellY, item.spanX, item.spanY);
                    workspace.requestLayout();
                    break;
                default:
                    break;
            }
        }
    }

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT >= 17) {
            return View.generateViewId();
        } else {
            // View.generateViewId() is not available. The following fallback logic is a copy
            // of its implementation.
            for (;;) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        }
    }

    public int getViewIdForItem(ItemInfo info) {
        int itemId = (int) info.id;
        if (mItemIdToViewId.containsKey(itemId)) {
            return mItemIdToViewId.get(itemId);
        }
        int viewId = generateViewId();
        mItemIdToViewId.put(itemId, viewId);
        return viewId;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.v(TAG, "onAttachedToWindow");
    }
}
