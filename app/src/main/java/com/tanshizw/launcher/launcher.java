package com.tanshizw.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

public class launcher extends Activity {
    private List<ResolveInfo> apps;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        loadApps();
        gridView  = (GridView)findViewById(R.id.apps_list);
        AppAdapter adapter = new AppAdapter(this);
        gridView.setAdapter(adapter);
    }

    private void loadApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        apps = getPackageManager().queryIntentActivities(intent, 0);
    }

    public class AppAdapter extends BaseAdapter {
        private  Context context;
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
