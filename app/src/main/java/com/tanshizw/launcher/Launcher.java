package com.tanshizw.launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Launcher extends Activity {
    //private Button chooseWallpaperBt;
    //private Button allApplicationBt;
    DragLayer mDragLayer;
    Workspace mWorkspace;
    ArrayList<ItemInfo> workspaceItems = new ArrayList<ItemInfo>();
    private HashMap<Integer, Integer> mItemIdToViewId = new HashMap<Integer, Integer>();
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        mInflater = getLayoutInflater();

        setupWorkspaceItems();
        setupViews();
/*
        chooseWallpaperBt = (Button)findViewById(R.id.choose_wallpaper);
        chooseWallpaperBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSetWallpaper();
            }
        });

        allApplicationBt = (Button)findViewById(R.id.all_applications);
        allApplicationBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                browseAllApplicationsInstalled();
            }
        });
*/
    }

    public void browseAllApplicationsInstalled() {
        Intent listApplications = new Intent(this, BrowseAllApplications.class);
        startActivity(listApplications);
    }

    public void onSetWallpaper() {
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper, "Choose Wallpaper");
        startActivity(chooser);
    }

    private void setupViews(){
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        mWorkspace = (Workspace) mDragLayer.findViewById(R.id.workspace);

    }

    private void setupWorkspaceItems(){
        ItemInfo itemInfoCall = new ItemInfo();
        itemInfoCall.container = CONTAINER_DESKTOP;
        itemInfoCall.itemType = ITEM_TYPE_SHORTCUT;
        itemInfoCall.screenId = 0;
        workspaceItems.add(itemInfoCall);
        bindWorkspaceItems(workspaceItems);
    }

    private void bindWorkspaceItems(final ArrayList<ItemInfo> workspaceItems){
        // Bind the workspace items
        int N = workspaceItems.size();
        for (int i = 0; i < N; i += ITEMS_CHUNK) {
            final int start = i;
            final int chunkSize = (i+ITEMS_CHUNK <= N) ? ITEMS_CHUNK : (N-i);
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                        bindItems(workspaceItems, start, start+chunkSize, false);
                    }
                };
            }
    }

    View createShortcut(int layoutResId, ViewGroup parent, ShortcutInfo info) {
        BubbleTextView favorite = (BubbleTextView) mInflater.inflate(layoutResId, parent, false);
        favorite.applyFromShortcutInfo(info, true);//mIconCache
        //favorite.setOnClickListener(this);
        //favorite.setOnFocusChangeListener(mFocusHandler);
        return favorite;
    }

    View createShortcut(ShortcutInfo info) {
        return createShortcut(R.layout.application,
                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentPage()), info);
    }

    public void bindItems(final ArrayList<ItemInfo> shortcuts, final int start, final int end,
                          final boolean forceAnimateIcons){
        Workspace workspace = mWorkspace;
        for (int i = start; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);

            switch (item.itemType){
                case ITEM_TYPE_APPLICATION:
                case ITEM_TYPE_SHORTCUT:
                    ShortcutInfo info = (ShortcutInfo) item;
                    View shortcut = createShortcut(info);//BubbleTextView
                    workspace.addInScreenFromBind(shortcut, item.container, item.screenId, item.cellX,
                            item.cellY, 1, 1);
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
        // This cast is safe given the > 2B range for int.
        int itemId = (int) info.id;
        if (mItemIdToViewId.containsKey(itemId)) {
            return mItemIdToViewId.get(itemId);
        }
        int viewId = generateViewId();
        mItemIdToViewId.put(itemId, viewId);
        return viewId;
    }
}
