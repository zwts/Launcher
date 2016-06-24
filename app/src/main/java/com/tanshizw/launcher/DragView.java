package com.tanshizw.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.tanshizw.launcher.items.ShortcutInfo;
import com.tanshizw.launcher.items.BubbleTextView;
import com.tanshizw.launcher.items.ItemInfo;

import java.util.HashSet;

/**
 * Created by sdduser on 6/17/16. DragView dragView = new DragView(Launcher.this,favorite,mDragLayer,workspaceItems.get(i),info.getIcon());
 * dragView.show();
 */
public class DragView extends View {
    private BubbleTextView favorite;
    private DragLayer dragLayer;
    private ItemInfo itemInfo;
    private Bitmap bitmap;
    private HashSet<Integer> locationMarker;
    private Workspace workspace;
    private Launcher launcher;
    private int i;


    private float x;
    private float y;
    private Paint paint;
    private float halfOfIconWPx;
    private Boolean trsPageFlag = false;
    private int newCellX = 0;
    private int newCellY = 0;
    private int newScreenId;



    public DragView(Context context, BubbleTextView favorite, DragLayer dragLayer, ItemInfo itemInfo,
                    Bitmap bitmap, HashSet<Integer> locationMarker, Workspace workspace, int i) {
        super(context);
        this.favorite = favorite;
        this.dragLayer = dragLayer;
        this.itemInfo = itemInfo;
        this.bitmap = bitmap;
        this.locationMarker = locationMarker;
        this.workspace = workspace;
        this.launcher = (Launcher) context;
        this.i = i;

        paint = new Paint();
        halfOfIconWPx = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size)/2;
        x = (float)(itemInfo.cellX+0.5) * LauncherSettings.ICON_WIDTH - halfOfIconWPx - 10;
        y = (itemInfo.cellY)* LauncherSettings.ICON_HEIGHT+LauncherSettings.WORKSPACE_TOPPADDING*2+10;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, x, y, paint);
    }

    public void show() {
        dragLayer.addView(DragView.this);
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
                Log.i("ACTION_MOVE", "========xxxx==" + event.getX() + "+++YYY" + event.getY());
                move();
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                x = event.getX();
                y = event.getY();
                up();
                Log.i("ACTION_UP", "========xxxx==" + event.getX() + "+++YYY" + event.getY());
                break;
        }
        return true;
    }

    private void move() {
        Log.i("---------", workspace.getChildCount() + "CCC" + workspace.getCurrentPage());
        if (x >= Launcher.mScreenW - halfOfIconWPx) {
            if (workspace.getCurrentPage() <= workspace.getChildCount() - 1 - 1){
                workspace.snapPage(workspace.getCurrentPage() + 1);
                trsPageFlag = true;
                Log.i("---------", workspace.getChildCount() + "CCC" + workspace.getCurrentPage());
            } else {
                //workspace.insertNewWorkspaceScreen(workspace.getCurrentPage()+1);
            }
        }
    }


    private void up() {
        newScreenId = workspace.getCurrentPage();
        checkCellX();
        checkCellY();
        moveView();
    }

    private void checkCellX(){
        if( x < (LauncherSettings.mCountX - 0.5) * LauncherSettings.ICON_WIDTH){
            int xIndex =  (int) x/LauncherSettings.ICON_WIDTH;
            if (x%LauncherSettings.ICON_WIDTH < LauncherSettings.ICON_WIDTH/2){
                newCellX = xIndex;
            } else {
                newCellX = xIndex + 1;
            }
        }else {
            newCellX = LauncherSettings.mCountX - 1;
        }
    }

    private void checkCellY(){
        if(y < LauncherSettings.WORKSPACE_TOPPADDING+LauncherSettings.WORKSPACE_HEIGHT){
            if(y < (LauncherSettings.mCountY - 0.5) * LauncherSettings.ICON_HEIGHT+LauncherSettings.WORKSPACE_TOPPADDING*2){
                if( (x>=0 && x<= Launcher.mScreenW) && ( y>=0 && y<=(LauncherSettings.WORKSPACE_TOPPADDING*2) )){
                    Log.i("-------","卸载");
                    uninstall();
                }else if( y < ( LauncherSettings.WORKSPACE_TOPPADDING*2+LauncherSettings.ICON_HEIGHT/2)){
                    newCellY = 0;
                } else {
                    int yIndex = (int)(y - LauncherSettings.WORKSPACE_TOPPADDING*2)/LauncherSettings.ICON_HEIGHT;
                    if( (y - LauncherSettings.WORKSPACE_TOPPADDING*2)%LauncherSettings.ICON_HEIGHT < LauncherSettings.ICON_HEIGHT/2){
                        newCellY= yIndex;
                    }else {
                        newCellY = yIndex + 1;
                    }
                }
            }else {
                newCellY = LauncherSettings.mCountY - 1;
            }
        } else {
            Log.i("-------","移动到hotset");
            moveToHotSet();
        }
    }

    private void moveView(){
        Log.i("--contains-----",(locationMarker.contains(newScreenId*100+newCellY*10+newCellX ))+"");
        if( !locationMarker.contains(newScreenId*100+newCellY*10+newCellX )){
            Log.i("--trsPageFlag-----",trsPageFlag+"翻页");
            if(trsPageFlag){
                locationMarker.remove((int)itemInfo.screenId*100+itemInfo.cellY*10+itemInfo.cellX);
                locationMarker.add(newScreenId*100 + newCellY*10 +newCellX);
                itemInfo.cellX = newCellX;
                itemInfo.cellY = newCellY;
                itemInfo.screenId = newScreenId;
                itemInfo.screenId = newScreenId;
                itemInfo.initCellX = newCellX;
                itemInfo.initCellY = newCellY;
                CellLayout cellLayout = (CellLayout) workspace.getChildAt(workspace.getCurrentPage()-1);
                ShortcutAndWidgetContainer container = (ShortcutAndWidgetContainer)cellLayout.getChildAt(0);
                Log.i("--removeView-----","removeView favorite ");
                container.removeView(favorite);
                reAddFavorite();


            }else {
                favorite.setTranslationX( (newCellX- itemInfo.initCellX) * LauncherSettings.ICON_WIDTH);
                favorite.setTranslationY( (newCellY -itemInfo.initCellY)  * LauncherSettings.ICON_HEIGHT );
                favorite.invalidate();
                locationMarker.remove((int)itemInfo.screenId*100+itemInfo.cellY*10+itemInfo.cellX);
                locationMarker.add(newScreenId*100 + newCellY*10 +newCellX);

                itemInfo.cellX = newCellX;
                itemInfo.cellY = newCellY;
                itemInfo.screenId = newScreenId;
                favorite.setVisibility(VISIBLE);
            }


        } else {
            favorite.setVisibility(VISIBLE);
        }
        dragLayer.removeView(DragView.this);
        Log.i("--newcellx-----",newCellX+"");
        Log.i("-----newcelly--",""+newCellY);
        Log.i("----newscreenid---",""+newScreenId);

    }
    private void reAddFavorite() {
        ShortcutInfo info = (ShortcutInfo) itemInfo;
        View shortcut = launcher.createShortcut(info, i);
        workspace.addInScreenFromBind(shortcut, itemInfo.container, itemInfo.screenId, itemInfo.cellX,
                itemInfo.cellY, itemInfo.spanX, itemInfo.spanY);

    }

    private void uninstall() {
    }

    private void moveToHotSet() {
    }


}
