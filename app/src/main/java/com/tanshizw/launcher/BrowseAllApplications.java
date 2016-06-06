package com.tanshizw.launcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Use another page to list all applications.
 */
public class BrowseAllApplications extends Activity {
    private List<ResolveInfo> apps;
    private GridView appGridView;
    private int itemHeight = 100;
    private int itemWidth = 100;
    private int iconHeight = 60;
    private int iconWidth = 60;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_applications_list);
        loadApps();

        appGridView  = (GridView)findViewById(R.id.apps_list);
        appGridView.setAdapter(new AppAdapter(this));
        appGridView.setOnItemClickListener(itemClickListener);
    }

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            ResolveInfo info = apps.get(i);
            String pkgName = info.activityInfo.packageName;
            String mainActivityName = info.activityInfo.name;

            ComponentName componentName = new ComponentName(pkgName, mainActivityName);
            Intent intent = new Intent();
            intent.setComponent(componentName);
            startActivity(intent);
        }
    };

    private void loadApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        apps = getPackageManager().queryIntentActivities(intent, 0);
    }

    public class AppAdapter extends BaseAdapter {
        private Context context;
        public AppAdapter(Context context) {
            super();
            this.context = context;
        }

        @Override
        public int getCount() {
            return apps.size();
        }

        @Override
        public Object getItem(int i) {
            return apps.get(i);
        }

        @Override
        public  long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View appItem;

            if (view == null) {
                LayoutInflater inflater = getLayoutInflater().from(context);
                appItem = inflater.inflate(R.layout.application_item, null);

                ImageView icon = (ImageView) appItem.findViewById(R.id.item_icon);
                TextView name = (TextView) appItem.findViewById(R.id.item_name);

                icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ViewGroup.LayoutParams iconParams = icon.getLayoutParams();
                iconParams.height = iconHeight;
                iconParams.width = iconWidth;
                icon.setLayoutParams(iconParams);
                appItem.setLayoutParams(new GridView.LayoutParams(itemWidth, itemHeight));

                ResolveInfo info = apps.get(i);
                name.setText(info.activityInfo.loadLabel(getPackageManager()));
                icon.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
            } else {
                appItem = view;
            }

            return appItem;
        }
    }

}
