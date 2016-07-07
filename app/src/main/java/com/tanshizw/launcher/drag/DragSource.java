package com.tanshizw.launcher.drag;

import android.view.View;

/**
 * Interface defining an object that can originate a drag.
 */
public interface DragSource {

    /**
     * @return whether items dragged from this source supports
     */
    boolean supportsFlingToDelete();

    /**
     * @return whether items dragged from this source supports 'App Info'
     */
    boolean supportsAppInfoDropTarget();

    /**
     * @return whether items dragged from this source supports 'Delete' drop target (e.g. to remove
     * a shortcut.
     */
    boolean supportsDeleteDropTarget();

    /*
     * @return the scale of the icons over the workspace icon size
     */
    float getIntrinsicIconScaleFactor();

    /**
     * A callback specifically made back to the source after an item from this source has been flung
     * to be deleted on a DropTarget.  In such a situation, this method will be called after
     * onDropCompleted, and more importantly, after the fling animation has completed.
     */
    void onFlingToDeleteCompleted();

    /**
     * A callback made back to the source after an item from this source has been dropped on a
     * DropTarget.
     */
    void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete, boolean success);
}
