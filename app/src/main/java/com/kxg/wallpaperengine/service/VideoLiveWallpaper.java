package com.kxg.wallpaperengine.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.text.TextUtils;
import android.view.SurfaceHolder;

import com.kxg.wallpaperengine.constant.Constant;
import com.kxg.wallpaperengine.util.FileUtil;

/**
 * Created by kuangxiaoguo on 2017/8/11.
 */

public class VideoLiveWallpaper extends WallpaperService {

    public static final String WALL_PAPER_SET_ACTION = "wall_paper_set_action";
    private static final String VIDEO_PATH_CHANGE_TAG = "video_path_change_tag";
    private static final String VIDEO_VOICE_CONTROL_KEY = "video_voice_control_key";
    private static final int ACTION_VOICE_SILENCE = 10;
    private static final int ACTION_VOICE_NORMAL = 11;

    @Override
    public Engine onCreateEngine() {
        return new WallEngine();
    }

    public static void changeVideoPath(Context context, String videoPath) {
        Intent intent = new Intent(WALL_PAPER_SET_ACTION);
        intent.putExtra(VIDEO_PATH_CHANGE_TAG, videoPath);
        context.sendBroadcast(intent);
    }

    public static void voiceSilence(Context context) {
        Intent intent = new Intent(WALL_PAPER_SET_ACTION);
        intent.putExtra(VIDEO_VOICE_CONTROL_KEY, VideoLiveWallpaper.ACTION_VOICE_SILENCE);
        context.sendBroadcast(intent);
    }

    public static void voiceNormal(Context context) {
        Intent intent = new Intent(WALL_PAPER_SET_ACTION);
        intent.putExtra(VIDEO_VOICE_CONTROL_KEY, ACTION_VOICE_NORMAL);
        context.sendBroadcast(intent);
    }

    private class WallEngine extends Engine implements SurfaceHolder.Callback {

        private MediaPlayer mMediaPlayer;
        private SurfaceHolder mSurfaceHolder;
        private boolean hasStarEngine = false;
        private boolean isPlayVideo = false;
        private boolean isOnErrorDid;
        private BroadcastReceiver mVideoParamsControlReceiver;
        private String mVideoPath;
        private float volumeValue = 0f;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(false);
            surfaceHolder.addCallback(this);
            FileUtil.setFilePath(getApplicationContext());

            IntentFilter intentFilter = new IntentFilter(WALL_PAPER_SET_ACTION);
            registerReceiver(mVideoParamsControlReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String path = intent.getStringExtra(VIDEO_PATH_CHANGE_TAG);
                    if (!TextUtils.isEmpty(path)) {
                        mVideoPath = path;
                        start(path);
                    }
                    int volumeTag = intent.getIntExtra(VIDEO_VOICE_CONTROL_KEY, -1);
                    switch (volumeTag) {
                        case ACTION_VOICE_SILENCE:
                            volumeValue = 0.0f;
                            break;
                        case ACTION_VOICE_NORMAL:
                            volumeValue = 1.0f;
                            break;
                        default:
                            break;
                    }
                    if (volumeTag != -1 && mMediaPlayer != null) {
                        mMediaPlayer.setVolume(volumeValue, volumeValue);
                    }
                }
            }, intentFilter);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                //如果可见
                if (hasStarEngine && !isPlayVideo) {
                    if (isOnErrorDid || mMediaPlayer == null) {
                        start(null);
                    } else {
                        isPlayVideo = true;
                        mMediaPlayer.start();
                    }
                }
                //
            } else {
                //如果不可见
                if (hasStarEngine && isPlayVideo && mMediaPlayer != null) {
                    isPlayVideo = false;
                    mMediaPlayer.pause();
                }
            }
        }

        private void start(String videoPath) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
                hasStarEngine = false;
                isPlayVideo = false;
                isOnErrorDid = false;
            }

            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setSurface(mSurfaceHolder.getSurface());
            try {
                if (TextUtils.isEmpty(videoPath)) {
                    videoPath = FileUtil.readDataFromFile(Constant.VIDEO_PATH_NAME);
                }
                mMediaPlayer.setDataSource(videoPath);
                String volumeTag = FileUtil.readDataFromFile(Constant.VOLUME_FILE_NAME);
                if (TextUtils.isEmpty(volumeTag) || "false".equals(volumeTag)) {
                    mMediaPlayer.setVolume(0f, 0f);
                } else {
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mMediaPlayer.start();
                    }
                });
                hasStarEngine = true;
                isPlayVideo = true;
            } catch (Exception e) {
                hasStarEngine = true;
            }
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    isOnErrorDid = true;
                    return true;
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    start(mVideoPath);
                }
            });
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            super.onSurfaceCreated(holder);
            start(null);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
        }

        @Override
        public void onDestroy() {
            unregisterReceiver(mVideoParamsControlReceiver);
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            hasStarEngine = false;
        }
    }

}
