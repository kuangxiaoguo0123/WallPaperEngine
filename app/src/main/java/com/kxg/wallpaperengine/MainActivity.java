package com.kxg.wallpaperengine;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.kxg.wallpaperengine.constant.Constant;
import com.kxg.wallpaperengine.service.VideoLiveWallpaper;
import com.kxg.wallpaperengine.util.FileUtil;
import com.kxg.wallpaperengine.util.WallPaperUtil;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final String VIDEO_PATH_TAG = "video_path_tag";
    private static final String IS_SET_VOLUME_TAG = "is_set_volume_tag";
    private static final String HAS_VOLUME_TAG = "has_volume_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FileUtil.setFilePath(this);
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        boolean isSetVolume = intent.getBooleanExtra(IS_SET_VOLUME_TAG, false);
        boolean hasVolume = intent.getBooleanExtra(HAS_VOLUME_TAG, false);
        //保存声音状态
        FileUtil.writeDataToFile(hasVolume ? "true" : "false", Constant.VOLUME_FILE_NAME);
        if (isSetVolume) {
            if (hasVolume) {
                VideoLiveWallpaper.voiceNormal(this);
            } else {
                VideoLiveWallpaper.voiceSilence(this);
            }
            finish();
            return;
        }
        String videoPath = intent.getStringExtra(VIDEO_PATH_TAG);
        if (TextUtils.isEmpty(videoPath)) {
            return;
        }
        FileUtil.writeDataToFile(videoPath, Constant.VIDEO_PATH_NAME);
        boolean isWallpaperRunning = WallPaperUtil.isLiveWallpaperRunning(this);
        if (isWallpaperRunning) {
            VideoLiveWallpaper.changeVideoPath(this, videoPath);
            goHome();
            finish();
            return;
        }
        setToWallPaper();
    }

    public void setToWallPaper() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new ComponentName(this, VideoLiveWallpaper.class));
        } else {
            intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
        }
        startActivity(intent);
        finish();
    }

    public void goHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Context context = getApplicationContext();
        if (context == null) {
            return;
        }
        Toast.makeText(context, R.string.set_wall_paper_success, Toast.LENGTH_SHORT).show();
    }

}
