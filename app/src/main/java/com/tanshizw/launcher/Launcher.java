package com.tanshizw.launcher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tanshizw.launcher.compat.LauncherActivityInfoCompat;
import com.tanshizw.launcher.compat.LauncherAppsCompat;
import com.tanshizw.launcher.items.AllAppsList;
import com.tanshizw.launcher.items.AppInfo;
import com.tanshizw.launcher.items.BubbleTextView;
import com.tanshizw.launcher.items.IconCache;
import com.tanshizw.launcher.items.ItemInfo;
import com.tanshizw.launcher.items.ShortcutInfo;
import com.tanshizw.launcher.utility.LauncherSettings;
import com.tanshizw.launcher.utility.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Default launcher Activity
 */
public class Launcher extends Activity implements View.OnClickListener {
    static final String TAG = "Launcher";

    // Now we only got shortcut items here, later we may add folder and widgets.
    static final int ITEM_TYPE_SHORTCUT = 0;

    private LayoutInflater mInflater;
    private IconCache mIconCache;
    private HashMap<Object, CharSequence> mLabelCache;
    private LauncherAppsCompat mLauncherApps;
    private ArrayList<ItemInfo> mWorkspaceItems = new ArrayList<>();
    private Point mScreenSize = new Point();

    private boolean mIsSafeModeEnabled;

    private DragLayer mDragLayer;
    private Workspace mWorkspace;
    private Hotseat mHotseat;

    public static int mScreenW;  // remove these two fields
    public static int mScreenH;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.launcher);
        init();  // a simple init is not that good, let's separate views, object init and loading.
    }

    private void init() {
        mInflater = getLayoutInflater();
        mLauncherApps = LauncherAppsCompat.getInstance(this);
        mIsSafeModeEnabled = getPackageManager().isSafeMode();

        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace = (Workspace) mDragLayer.findViewById(R.id.workspace);
        mHotseat = (Hotseat) mDragLayer.findViewById(R.id.hot_seat);

        mIconCache = new IconCache(this);
        mLabelCache = new HashMap<Object, CharSequence>();

        getWindowManager().getDefaultDisplay().getSize(mScreenSize);
        mScreenW = mScreenSize.x;
        mScreenH = mScreenSize.y;

        Utilities.setupScreenSizeSettings(this);
        insertWorkspaceScreens();
        insertHotseatLayout();

        loadWorkspaceItems();
        bindWorkspaceItems();
    }

    private void insertWorkspaceScreens() {
        Long screenId = 0L;
        for (int i = 0; i < LauncherSettings.DEFAULT_PAGE_NUM; i++) {
            mWorkspace.insertNewWorkspaceScreen(screenId + i);
        }
    }

    /**
     * Prepare all items.
     */
    private void loadWorkspaceItems() {
        List<LauncherActivityInfoCompat> apps = mLauncherApps.getActivityList();
        if (apps == null || apps.isEmpty()) {
            return;
        }

        AllAppsList list = new AllAppsList();
        for (LauncherActivityInfoCompat app : apps) {
            list.add(new AppInfo(app, mIconCache, mLabelCache));
        }

        int hotseatAppNum = 0;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            AppInfo info = list.get(i);
            if (isHotseatApp(info)) {
                applyHotseatApps(info, hotseatAppNum++);
            } else {
                applyDesktopApps(info, i - hotseatAppNum);
            }
        }
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

        mWorkspaceItems.add(shortcut);
    }

    private void applyDesktopApps(AppInfo info, int i) {
        ShortcutInfo shortcut = new ShortcutInfo(info);

        shortcut.container = LauncherSettings.CONTAINER_DESKTOP;
        shortcut.itemType = ITEM_TYPE_SHORTCUT;
        shortcut.screenId = 0;
        shortcut.id = 0;
        shortcut.cellX = i % LauncherSettings.mCountX;
        shortcut.cellY = i / LauncherSettings.mCountX;
        shortcut.initCellX = shortcut.cellX;
        shortcut.initCellY = shortcut.cellY;
        shortcut.initScreenId = (int) shortcut.screenId;
        shortcut.spanX = 1;
        shortcut.spanY = 1;

        mWorkspaceItems.add(shortcut);
        mDragLayer.addLocation(shortcut);
    }

    private boolean isHotseatApp(AppInfo info) {
        String packageName = info.intent.getComponent().getPackageName();
        for (String name : LauncherSettings.HOTSET_APPS) {
            if (name.equals(packageName)) { return true; }
        }
        return false;
    }

    /**
     * Add cell layout in hot seat.
     */
    private void insertHotseatLayout() {
        mHotseat.insertHotseatLayout();
    }

    /**
     * Add shortcut items to workspace.
     */
    private void bindWorkspaceItems() {
        for (ItemInfo item : mWorkspaceItems) {
            ShortcutInfo info = (ShortcutInfo) item;
            View shortcut = createShortcut(info);
            mWorkspace.addInScreenFromBind(shortcut, info.container, info.screenId, info.cellX,
                    info.cellY, info.spanX, info.spanY);
        }
    }

    /**
     * Creates a BubbleTextView representing a shortcut.
     *
     * @param info The data structure describing the shortcut.
     * @return A BubbleTextView inflated from R.layout.application
     * which will display in workspace and hotseat.
     */
    View createShortcut(final int layoutResId, ViewGroup parent, final ShortcutInfo info) {
        final BubbleTextView favorite = (BubbleTextView) mInflater.inflate(layoutResId, parent, false);
        favorite.applyFromShortcutInfo(info, mIconCache, true);
        favorite.setOnClickListener(this);

        // you don't need to create a long click listener for each shortcut
        favorite.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                favorite.setVisibility(View.GONE);
                DragView dragView = new DragView(Launcher.this, favorite, mDragLayer, info, info.getIcon(), mWorkspace);
                dragView.show();
                return true;
            }
        });
        return favorite;
    }

    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.application,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentPage()), info);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void onClick(View v) {
        Object tag = v.getTag();

        if (tag instanceof ShortcutInfo) {
            startAppShortcutOrInfoActivity(v);
        }
    }

    private void startAppShortcutOrInfoActivity(View v) {
        Object tag = v.getTag();
        final ShortcutInfo shortcut = (ShortcutInfo) tag;
        startActivitySafely(shortcut.intent, tag);
    }

    private void startActivitySafely(Intent intent, Object tag) {
        if (mIsSafeModeEnabled && !Utilities.isSystemApp(this, intent)) {
            Toast.makeText(this, R.string.safemode_shortcut_error, Toast.LENGTH_SHORT).show();
        }
        try {
            startActivity(intent, tag);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
        }
    }

    private void startActivity(Intent intent, Object tag) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            // Only launch using the new animation if the shortcut has not opted out (this is a
            // private contract between launcher and may be ignored in the future).
            LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(this);
            launcherApps.startActivityForProfile(intent.getComponent(), null, null);
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity. "
                    + "tag=" + tag + " intent=" + intent, e);
        }
    }

    public Hotseat getHotseat() {
        return mHotseat;
    }

    public Point getScreenSize() { return mScreenSize;}

}
