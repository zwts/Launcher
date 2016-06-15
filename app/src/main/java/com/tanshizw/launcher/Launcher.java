package com.tanshizw.launcher;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.tanshizw.launcher.Utility.LauncherSettings;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Launcher extends Activity implements View.OnClickListener {
    private IconCache mIconCache;
    private AllAppsList mBgAllAppsList;
    private LauncherAppsCompat mLauncherApps;
    private HashMap<Object, CharSequence> mLabelCache;
    private Context mContext;
    private boolean mIsSafeModeEnabled;

    DragLayer mDragLayer;
    Workspace mWorkspace;
    Hotseat mHotseat;
    ArrayList<ItemInfo> workspaceItems = new ArrayList<ItemInfo>();
    private HashMap<Integer, Integer> mItemIdToViewId = new HashMap<Integer, Integer>();
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    ArrayList<Long> orderedScreenIds = new ArrayList<Long>();
    ;
    private LayoutInflater mInflater;
    static final int ITEM_TYPE_APPLICATION = 0;
    static final int ITEM_TYPE_SHORTCUT = 1;
    private final String TAG = "Launcher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Log.v(TAG, "onCreate");

        mInflater = getLayoutInflater();
        mIsSafeModeEnabled = getPackageManager().isSafeMode();

        setupViews();

        Long screenId = Long.valueOf(0);
        orderedScreenIds.add(screenId);
        orderedScreenIds.add(screenId + 1);//two screens
        bindAddScreens(orderedScreenIds);

        addHotseatLayout();

        setupWorkspaceItems();
    }

    private void setupViews() {
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace = (Workspace) mDragLayer.findViewById(R.id.workspace);
        mHotseat = (Hotseat) mDragLayer.findViewById(R.id.hot_seat);
    }

    private void setupWorkspaceItems() {
        int hotseatAppNum = 0;
        int desktopAppNum = 0;
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

        for (int i = 0; i < mBgAllAppsList.size(); i++) {
            AppInfo info = mBgAllAppsList.get(i);
            if (isHotseatApp(info)) {
                applyHotseatApps(info, hotseatAppNum);
                hotseatAppNum ++;
            } else {
                applyDesktopApps(info, desktopAppNum);
                desktopAppNum ++;
            }
        }

        bindWorkspaceItems(workspaceItems);
    }

    private void applyHotseatApps(AppInfo info, int i) {
            ShortcutInfo shortcut = new ShortcutInfo(info);

            shortcut.container = LauncherSettings.CONTAINER_HOTSEAT;
            shortcut.itemType = ITEM_TYPE_SHORTCUT;
            shortcut.screenId = 0;
            shortcut.id = 0;
            shortcut.cellX = i;
            shortcut.cellY = 0;
            shortcut.spanX = 1;
            shortcut.spanY = 1;
            workspaceItems.add(shortcut);
    }

    private void applyDesktopApps(AppInfo info, int i) {
            ShortcutInfo shortcut = new ShortcutInfo(info);

            shortcut.container = LauncherSettings.CONTAINER_DESKTOP;
            shortcut.itemType = ITEM_TYPE_SHORTCUT;
            shortcut.screenId = 0;
            shortcut.id = 0;
            shortcut.cellX = i % LauncherSettings.mCountX;
            shortcut.cellY = i / LauncherSettings.mCountX;
            shortcut.spanX = 1;
            shortcut.spanY = 1;
            workspaceItems.add(shortcut);
    }

    private boolean isHotseatApp(AppInfo info) {
        final ComponentName component = info.intent.getComponent();
        String packageName = component.getPackageName();
        for(int i = 0; i < LauncherSettings.HOTSET_APPS.length; i++ ) {
            if (LauncherSettings.HOTSET_APPS[i].equals(packageName)) {
                return true;
            }
        }
        return false;
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
    }

    ;

    public void bindAddScreens(ArrayList<Long> orderedScreenIds) {
        int count = orderedScreenIds.size();
        for (int i = 0; i < count; i++) {
            mWorkspace.insertNewWorkspaceScreen(orderedScreenIds.get(i));
        }
    }

    public void addHotseatLayout() {
        mHotseat.insertHotseatLayout();
    }

    private void bindWorkspaceItems(final ArrayList<ItemInfo> workspaceItems) {
        // Bind the workspace items
        int N = workspaceItems.size();
        int itemschunk = LauncherSettings.ITEMS_CHUNK;
        Log.v(TAG, "bindWorkspaceItems N = " + N);
        for (int i = 0; i < N; i += itemschunk) {
            final int start = i;
            final int chunkSize = (i + itemschunk <= N) ? itemschunk : (N - i);
            Log.v(TAG, "bindWorkspaceItems chunkSize = " + chunkSize);
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, "bindWorkspaceItems run");
                    bindItems(workspaceItems, start, start + chunkSize);
                }
            };
            r.run();
        }
    }

    View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
        Log.v(TAG, "createShortcut");
        BubbleTextView favorite = (BubbleTextView) mInflater.inflate(layoutResId, parent, false);
        favorite.applyFromShortcutInfo(info, mIconCache, true);
        favorite.setOnClickListener(this);
        //favorite.setOnFocusChangeListener(mFocusHandler);
        return favorite;
    }

    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.application,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentPage()), info);
    }

    public void bindItems(final ArrayList<ItemInfo> shortcuts, final int start, final int end) {
        Log.v(TAG, "bindItems");
        for (int i = start; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);

            switch (item.itemType) {
                case ITEM_TYPE_APPLICATION:
                case ITEM_TYPE_SHORTCUT:
                    ShortcutInfo info = (ShortcutInfo) item;
                    View shortcut = createShortcut(info);//BubbleTextView
                    mWorkspace.addInScreenFromBind(shortcut, item.container, item.screenId, item.cellX,
                            item.cellY, item.spanX, item.spanY);

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
            for (; ; ) {
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

    public void onClick(View v) {
        if (v instanceof Workspace) {
            //TODO: workspace over view mode
            return;
        }

        if (v instanceof CellLayout) {
            //TODO: workspace over view mode
        }

        Object tag = v.getTag();

        if (tag instanceof ShortcutInfo) {
            onClickAppShortcut(v);
        } else if (tag instanceof AppInfo) {
            startAppShortcutOrInfoActivity(v);
        } else {
            //TODO: FolderIcon and PendingWidget onclick
        }
    }

    /**
     * Event handler for an app shortcut click.
     *
     * @param v The view that was clicked. Must be a tagged with a {@link ShortcutInfo}.
     */
    protected void onClickAppShortcut(final View v) {
        Log.d(TAG, "onClickAppShortcut");
        Object tag = v.getTag();
        if (!(tag instanceof ShortcutInfo)) {
            throw new IllegalArgumentException("Input must be a Shortcut");
        }

        // Open shortcut
        final ShortcutInfo shortcut = (ShortcutInfo) tag;
        final Intent intent = shortcut.intent;

        // Start activities
        startAppShortcutOrInfoActivity(v);
    }

    private void startAppShortcutOrInfoActivity(View v) {
        Object tag = v.getTag();
        final ShortcutInfo shortcut;
        final Intent intent;
        if (tag instanceof ShortcutInfo) {
            shortcut = (ShortcutInfo) tag;
            intent = shortcut.intent;
            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            intent.setSourceBounds(new Rect(pos[0], pos[1],
                    pos[0] + v.getWidth(), pos[1] + v.getHeight()));
        } else if (tag instanceof AppInfo) {
            shortcut = null;
            intent = ((AppInfo) tag).intent;
        } else {
            throw new IllegalArgumentException("Input must be a Shortcut or AppInfo");
        }

        boolean success = startActivitySafely(v, intent, tag);
        if (success && v instanceof BubbleTextView) {
            //TODO: when success do something show the icon shadow
        }
    }

    boolean startActivitySafely(View v, Intent intent, Object tag) {
        boolean success = false;
        if (mIsSafeModeEnabled && !Utilities.isSystemApp(this, intent)) {
            Toast.makeText(this, R.string.safemode_shortcut_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            success = startActivity(v, intent, tag);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        }
        return success;
    }

    boolean startActivity(View v, Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            // Only launch using the new animation if the shortcut has not opted out (this is a
            // private contract between launcher and may be ignored in the future).
            LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(this);
            Bundle optsBundle = null;

            launcherApps.startActivityForProfile(intent.getComponent(), intent.getSourceBounds(),
                    optsBundle);
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag=" + tag + " intent=" + intent, e);
        }
        return false;
    }

    public Hotseat getHotseat() {
        return mHotseat;
    }
}
