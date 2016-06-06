package com.tanshizw.launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class Launcher extends Activity {
    private Button chooseWallpaperBt;
    private Button allApplicationBt;

    private View view1, view2;
    private ViewPager workSpace;
    private List<View> viewList;
    private PagerTabStrip pagerTabStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        initViewPager();

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

    private void initViewPager() {
        workSpace = (ViewPager) findViewById(R.id.work_space);

        LayoutInflater inflater = getLayoutInflater().from(this);
        view1 = inflater.inflate(R.layout.work_page1, null);
        view2 = inflater.inflate(R.layout.work_page2, null);

        viewList = new ArrayList<View>();
        viewList.add(view1);
        viewList.add(view2);

        workSpace.setAdapter(new MyPagerAdapter(viewList));
        workSpace.setCurrentItem(0);
    }

    public class MyPagerAdapter extends PagerAdapter {
        private  List<View> list;

        public MyPagerAdapter(List<View> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(list.get(position),0);
            return list.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(list.get(position));
        }
    }
}
