package com.tanshizw.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.tanshizw.launcher.items.ShortcutInfo;
import com.tanshizw.launcher.utility.LauncherSettings;
import com.tanshizw.launcher.items.BubbleTextView;
import com.tanshizw.launcher.items.ItemInfo;

import java.util.HashSet;

/**
 * Created by sdduser on 6/17/16. DragView dragView = new DragView(Launcher.this,favorite,mDragLayer,workspaceItems.get(i),info.getIcon());
 *dragView.show();
 */
public class DragView extends View {
    private BubbleTextView favorite;
    private DragLayer dragLayer;
    private ItemInfo itemInfo;
    private Bitmap bitmap;
    private HashSet<Integer> workspaceMarker;
    private HashSet<Integer> hotsetMarker;
    private Workspace workspace;
    private Launcher launcher;
    private int i;//itemIfnfo index

    private float x;
    private float y;
    private Paint paint;
    private float halfOfIconWPx;
    private int newCellX = 0;
    private int newCellY = 0;
    private int newScreenId;

    private final int WORKSPACE_TO_SAME_PAGE = 1;
    private final int WORKSPACE_TO__OTHER_PAGE =2;
    private final int WORKSPACE_TO_HOTSET = 3;
    private final int HOTSET_TO_WORKSPACE = 4;
    private final int HOTSET_TO_HOTSET = 5;
    private int flag = WORKSPACE_TO_SAME_PAGE;


    public DragView(Context context, BubbleTextView favorite, DragLayer dragLayer, ItemInfo itemInfo, Bitmap bitmap,
                    HashSet<Integer> workspaceMarker, HashSet<Integer> hotsetMarker, Workspace workspace, int i ) {
        super(context);
        this.favorite = favorite;
        this.dragLayer = dragLayer;
        this.itemInfo = itemInfo;
        this.bitmap = bitmap;
        this.workspaceMarker = workspaceMarker;
        this.hotsetMarker = hotsetMarker;
        this.workspace = workspace;
        this.launcher = (Launcher) context;
        this.i = i;

        paint = new Paint();
        halfOfIconWPx = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size) / 2;
        x = (float) (itemInfo.cellX + 0.5) * LauncherSettings.ICON_WIDTH - halfOfIconWPx - 10;
        if (itemInfo.container == LauncherSettings.CONTAINER_DESKTOP) {
            y = (itemInfo.cellY) * LauncherSettings.ICON_HEIGHT + LauncherSettings.WORKSPACE_TOPPADDING * 2 + 10;
        } else {
            y = Launcher.mScreenH - LauncherSettings.ICON_HEIGHT + 10;
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, x, y, paint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                Log.i("ACTION_DOWN", "====xxxx==" + event.getX() + "+++YYY" + event.getY());
                postInvalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                y = event.getY();
                Log.i("ACTION_DOWN", "====xxxx==" + event.getX() + "+++YYY" + event.getY());
                if(itemInfo.container == LauncherSettings.CONTAINER_DESKTOP) {
                    move();
                }
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                x = event.getX();
                y = event.getY();
                up();
                Log.i("ACTION_DOWN", "====xxxx==" + event.getX() + "+++YYY" + event.getY());
                break;
        }
        return true;
    }


    public void show() {
        dragLayer.addView(DragView.this);
    }

    private void move() {
        if (x >= Launcher.mScreenW - halfOfIconWPx) {
            if (workspace.getCurrentPage() <= workspace.getChildCount() - 1 - 1) {
                workspace.snapPage(workspace.getCurrentPage() + 1);
                flag = WORKSPACE_TO__OTHER_PAGE;
            } else {
                //workspace.insertNewWorkspaceScreen(workspace.getCurrentPage()+1);
            }
        }
    }


    private void up() {
        newScreenId = workspace.getCurrentPage();
        checkCellX();
        switch ((int) itemInfo.container) {
            case LauncherSettings.CONTAINER_DESKTOP:
                checkY();
                break;
            case LauncherSettings.CONTAINER_HOTSEAT:
                if (y < LauncherSettings.WORKSPACE_TOPPADDING + LauncherSettings.WORKSPACE_HEIGHT) {
                    flag = HOTSET_TO_WORKSPACE;
                    checkCellY();
                } else {
                    flag = HOTSET_TO_HOTSET;
                }
                break;
        }

        moveView();
        dragLayer.removeView(DragView.this);

    }


    private void moveView() {
        switch (flag) {
            case WORKSPACE_TO_SAME_PAGE:
                workspaceToSamePage();
                break;
            case WORKSPACE_TO_HOTSET:
                workspaceToHotset();
                break;
            case WORKSPACE_TO__OTHER_PAGE:
                workspaceToOtherPage();
                break;
            case HOTSET_TO_HOTSET:
                hotsetToHotset();
                break;
            case HOTSET_TO_WORKSPACE:
                hotsetMoveToWorkspace();
                break;
        }
    }


    private void checkCellX() {
        if (x < (LauncherSettings.mCountX - 0.5) * LauncherSettings.ICON_WIDTH) {
            int xIndex = (int) x / LauncherSettings.ICON_WIDTH;
            if (x % LauncherSettings.ICON_WIDTH < LauncherSettings.ICON_WIDTH / 2) {
                newCellX = xIndex;
            } else {
                newCellX = xIndex + 1;
            }
        }else {
            newCellX = LauncherSettings.mCountX - 1;
        }
    }


    private void checkY() {
        if (y < LauncherSettings.WORKSPACE_TOPPADDING + LauncherSettings.WORKSPACE_HEIGHT) {
            checkCellY();
        } else {
            Log.i("-------", "移动到hotset");
            flag = WORKSPACE_TO_HOTSET;
        }
    }


    private void checkCellY() {
        if (y < (LauncherSettings.mCountY - 0.5) * LauncherSettings.ICON_HEIGHT + LauncherSettings.WORKSPACE_TOPPADDING *2 ) {
            if ((x >= 0 && x <= Launcher.mScreenW) && (y >= 0 && y <= (LauncherSettings.WORKSPACE_TOPPADDING * 2) )) {
                Log.i("-------", "卸载");
                uninstall();
            } else if (y < (LauncherSettings.WORKSPACE_TOPPADDING * 2 + LauncherSettings.ICON_HEIGHT / 2)) {
                newCellY = 0;
            } else {
                int yIndex = (int) (y - LauncherSettings.WORKSPACE_TOPPADDING * 2) / LauncherSettings.ICON_HEIGHT;
                if ((y - LauncherSettings.WORKSPACE_TOPPADDING * 2) % LauncherSettings.ICON_HEIGHT < LauncherSettings.ICON_HEIGHT / 2) {
                    newCellY= yIndex;
                } else {
                    newCellY = yIndex + 1;
                }
            }
        }else {
            newCellY = LauncherSettings.mCountY - 1;
        }
    }


    private void workspaceToSamePage() {
        if (!workspaceMarker.contains(newScreenId * 100 + newCellY * 10 + newCellX )) {
            favorite.setTranslationX((newCellX - itemInfo.initCellX) * LauncherSettings.ICON_WIDTH);
            favorite.setTranslationY((newCellY - itemInfo.initCellY) * LauncherSettings.ICON_HEIGHT);
            favorite.invalidate();
            workspaceMarker.remove((int) itemInfo.screenId * 100 + itemInfo.cellY * 10 + itemInfo.cellX);
            workspaceMarker.add(newScreenId * 100 + newCellY * 10 + newCellX);
            itemInfo.cellX = newCellX;
            itemInfo.cellY = newCellY;
            itemInfo.screenId = newScreenId;
        }
        favorite.setVisibility(VISIBLE);
    }


    private void workspaceToHotset() {
        if (!hotsetMarker.contains(newCellX )) {
            CellLayout cellLayout = (CellLayout) workspace.getChildAt(workspace.getCurrentPage());
            ShortcutAndWidgetContainer container = (ShortcutAndWidgetContainer) cellLayout.getChildAt(0);
            container.removeView(favorite);
            workspaceMarker.remove((int) itemInfo.screenId * 100 + itemInfo.cellY * 10 + itemInfo.cellX);
            hotsetMarker.add(newCellX);
            itemInfo.cellX = newCellX;
            itemInfo.cellY = 0;
            itemInfo.initCellX = newCellX;
            itemInfo.container = LauncherSettings.CONTAINER_HOTSEAT;
            reAddFavorite();

        }  else {
            favorite.setVisibility(VISIBLE);
        }
    }


    private void workspaceToOtherPage() {
        if (!workspaceMarker.contains(newScreenId * 100 + newCellY * 10 + newCellX )) {
            CellLayout cellLayout = (CellLayout) workspace.getChildAt(workspace.getCurrentPage() - 1);
            ShortcutAndWidgetContainer container = (ShortcutAndWidgetContainer) cellLayout.getChildAt(0);
            container.removeView(favorite);
            workspaceMarker.remove((int) itemInfo.screenId * 100 + itemInfo.cellY * 10 + itemInfo.cellX);
            workspaceMarker.add(newScreenId * 100 + newCellY * 10 + newCellX);
            itemInfo.cellX = newCellX;
            itemInfo.cellY = newCellY;
            itemInfo.screenId = newScreenId;
            itemInfo.initScreenId = newScreenId;
            itemInfo.initCellX = newCellX;
            itemInfo.initCellY = newCellY;
            reAddFavorite();
        } else {
            favorite.setVisibility(VISIBLE);
        }
    }


    private void hotsetToHotset() {
        if (!hotsetMarker.contains(newCellX )) {
            favorite.setTranslationX((newCellX - itemInfo.initCellX) * LauncherSettings.ICON_WIDTH);
            favorite.invalidate();
            hotsetMarker.remove(itemInfo.cellX);
            hotsetMarker.add(newCellX);
            itemInfo.cellX = newCellX;
        }
        favorite.setVisibility(VISIBLE);

    }

    private void hotsetMoveToWorkspace() {
        if (!workspaceMarker.contains(newScreenId * 100 + newCellY * 10 + newCellX )) {
            Hotseat hotset = (Hotseat) dragLayer.getChildAt(2);
            CellLayout cellLayout = (CellLayout) hotset.getChildAt(0);
            ShortcutAndWidgetContainer container = (ShortcutAndWidgetContainer) cellLayout.getChildAt(0);
            container.removeView(favorite);
            hotsetMarker.remove(itemInfo.cellX);
            workspaceMarker.add(newScreenId * 100 + newCellY * 10 +newCellX);
            itemInfo.cellX = newCellX;
            itemInfo.cellY = newCellY;
            itemInfo.screenId = newScreenId;
            itemInfo.initScreenId = newScreenId;
            itemInfo.initCellX = newCellX;
            itemInfo.initCellY = newCellY;
            itemInfo.container = LauncherSettings.CONTAINER_DESKTOP;
            reAddFavorite();
        }  else {
            favorite.setVisibility(VISIBLE);
        }
    }


    private void reAddFavorite() {
        ShortcutInfo info = (ShortcutInfo) itemInfo;
        View shortcut = launcher.createShortcut(info, i);
        workspace.addInScreenFromBind(shortcut, itemInfo.container, itemInfo.screenId, itemInfo.cellX,
                itemInfo.cellY, itemInfo.spanX, itemInfo.spanY);
    }


    private void uninstall() {

    }



}
