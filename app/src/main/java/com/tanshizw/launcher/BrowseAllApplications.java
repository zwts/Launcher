package com.tanshizw.launcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

/**
 * Use another page to list all applications.
 */
public class BrowseAllApplications extends Activity {
    private List<ResolveInfo> apps;
    private GridView appGridView;

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
            ImageView imagev;
            if (view == null) {
                imagev = new ImageView(context);
                imagev.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imagev.setLayoutParams(new GridView.LayoutParams(105, 105));
            } else {
                imagev = (ImageView) view;
            }

            ResolveInfo info = apps.get(i);
            imagev.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
            return imagev;
        }
    }

}
