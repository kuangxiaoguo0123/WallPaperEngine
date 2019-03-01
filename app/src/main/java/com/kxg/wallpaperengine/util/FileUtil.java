package com.kxg.wallpaperengine.util;

import android.content.Context;
import android.os.Environment;

import com.kxg.wallpaperengine.constant.Constant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by kuangxiaoguo on 2017/9/5.
 */

public class FileUtil {

    private static final String TAG = "FileUtil";
    private static String mPathFinder;

    public static void setFilePath(Context context) {
        if (isSDCardAvailable()) {
            mPathFinder = Environment.getExternalStorageDirectory().getPath() +
                    Constant.OUTER_VIDEO_PATH_FINDER;
            return;
        }
        mPathFinder = context.getFilesDir().getPath() + Constant.INNER_VIDEO_PATH_FINDER;
    }

    private static boolean isSDCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static void writeDataToFile(String content, String fileName) {
        File file = new File(mPathFinder);
        if (!file.exists()) {
            file.mkdirs();
        }
        File pathFile = new File(mPathFinder + fileName);
        if (!pathFile.exists()) {
            try {
                pathFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathFile));
            bufferedWriter.write(content);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readDataFromFile(String fileName) {
        File pathFile = new File(mPathFinder + fileName);
        String videoPath = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(pathFile));
            videoPath = bufferedReader.readLine();
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoPath;
    }
}
