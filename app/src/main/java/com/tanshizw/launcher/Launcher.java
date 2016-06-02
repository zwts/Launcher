package com.tanshizw.launcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Launcher extends Activity {
    private Button chooseWallpaperBt;
    private Button allApplicationBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

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
}
