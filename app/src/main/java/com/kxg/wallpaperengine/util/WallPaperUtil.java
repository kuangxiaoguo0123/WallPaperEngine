package com.kxg.wallpaperengine.util;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;

/**
 * Created by kuangxiaoguo on 2017/8/14.
 */

public class WallPaperUtil {

    /**
     * 判断一个动态壁纸是否已经在运行
     */
    public static boolean isLiveWallpaperRunning(Context context) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        // 如果系统使用的壁纸是动态壁纸话则返回该动态壁纸的信息,否则会返回null
        WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
        // 如果是动态壁纸,则得到该动态壁纸的包名,并与想知道的动态壁纸包名做比较
        if (wallpaperInfo != null) {
            String currentLiveWallpaperPackageName = wallpaperInfo.getPackageName();
            if (currentLiveWallpaperPackageName.equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }
}
