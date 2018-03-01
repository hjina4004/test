package com.minimon.diocian.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

/**
 * Created by GOOD on 2018-02-26.
 */

public class VideoPlayGestureDetector implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    Context mContext;
    VideoPlayScreenActivity videoActivity;

    int mWidth, mHeight = 0;
    private Path volumePath;
    private RectF volumeRectf;
    private Path brightPath;
    private RectF brightRectf;

    private VerticalSeekBar brightSeekBar;
    private VerticalSeekBar volumeSeekBar;
    private boolean isShowBrightSeekBar = false;
    private boolean isShowVolumeSeekBar = false;

    private SimpleExoPlayerView playerView;
    private int doub = 0;
//    private long movingTime = 0;

//    protected MotionEvent motionEvent = null;
    public VideoPlayGestureDetectorListener mListener;
    public interface VideoPlayGestureDetectorListener{

    }

    public void setListener(VideoPlayGestureDetectorListener listener){
        mListener = listener;
    }

    public VideoPlayGestureDetector(Context context, VideoPlayScreenActivity activity, int width, int height){
        mContext = context;
        brightSeekBar = activity.findViewById(R.id.BrightSeekBar);
        volumeSeekBar = activity.findViewById(R.id.VolumeSeekBar);
        brightSeekBar.setEnabled(false);
        volumeSeekBar.setEnabled(false);
        int brightnessValue = Settings.System.getInt(
                mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                0
        );
        brightSeekBar.setProgress(brightnessValue);
        videoActivity = activity;
        playerView = activity.findViewById(R.id.player_view);
        mWidth = width;
        mHeight = height;
        brightPath = new Path();
        volumePath = new Path();
        brightRectf = new RectF();
        volumeRectf = new RectF();
        brightPath.moveTo(0,0);
        brightPath.moveTo(0,height);
        brightPath.moveTo(width/4,height);
        brightPath.moveTo(width/4,200);
        brightPath.close();

        volumePath.moveTo(width/4*3,0);
        volumePath.moveTo(width/4*3,height);
        volumePath.moveTo(width,height);
        volumePath.moveTo(width,0);
        volumePath.close();

        brightPath.computeBounds(brightRectf,true);
        volumePath.computeBounds(volumeRectf,true);

//        bottomBarHeight = videoActivity.findViewById(R.id.exo_view_play_info).getHeight();
//        playlistHeight = videoActivity.findViewById(R.id.rec_playing_playlist).getHeight();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if(videoActivity.findViewById(R.id.view_playing_bright_seekbar).getVisibility() ==View.VISIBLE) {
            videoActivity.findViewById(R.id.view_playing_bright_seekbar).setVisibility(View.GONE);
            isShowBrightSeekBar = false;
            return false;
        }
        if(videoActivity.findViewById(R.id.view_playing_volume_seekbar).getVisibility() == View.VISIBLE) {
            videoActivity.findViewById(R.id.view_playing_volume_seekbar).setVisibility(View.GONE);
            isShowVolumeSeekBar = false;
            return false;
        }

        if(brightRectf.contains(e.getX(),e.getY())) {
            videoActivity.findViewById(R.id.view_playing_bright_seekbar).setVisibility(View.VISIBLE);
            isShowBrightSeekBar = true;
        }
        else if(volumeRectf.contains(e.getX(),e.getY())) {
            videoActivity.findViewById(R.id.view_playing_volume_seekbar).setVisibility(View.VISIBLE);
            isShowVolumeSeekBar = true;
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//        Log.d("GestureTag", "y1: "+e1.getY()+", y2: "+e2.getY());
        if (brightRectf.contains(e1.getX(), e1.getY()) && brightRectf.contains(e2.getX(), e2.getY()) && isShowBrightSeekBar) {
            controlBright(e1.getY(), e2.getY());
        } else if (volumeRectf.contains(e1.getX(), e1.getY()) && volumeRectf.contains(e2.getX(), e2.getY()) && isShowVolumeSeekBar) {
            controlMediaVolume(e1.getY(), e2.getY());
        }else{
            controlPlayTime(e1.getX(), e2.getX());
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(e1.getY()>mHeight-100)
            controlBottomMenu(e1.getY(), e2.getY(),true);
        if(e1.getY()>300 && e1.getY() < 400)
            controlBottomMenu(e1.getY(), e2.getY(),false);
        return true;
    }

    private void controlPlayTime(float x1, float x2){
        if(x1 > x2){
            if(x1-x2>100){
                doub = Math.round((x1-x2)/100);
                if(doub > 6) return;
                Log.d("DETECTORTAG",String.valueOf(doub));
                long movingTime = doub*10000;
                long currentTime = playerView.getPlayer().getCurrentPosition();
                playerView.getPlayer().setPlayWhenReady(false);
                playerView.getPlayer().seekTo(currentTime-movingTime);
            }
        }else{
            if(x2-x1>100){
                doub = Math.round((x2-x1)/100);
                if(doub > 6) return;
                Log.d("DETECTORTAG",String.valueOf(doub));
                long movingTime = doub*10000;
                long currentTime = playerView.getPlayer().getCurrentPosition();
                playerView.getPlayer().setPlayWhenReady(false);
                playerView.getPlayer().seekTo(currentTime+movingTime);
            }
        }
    }

    private void controlBottomMenu(float y1, float y2, boolean isShow){
        if(y1>y2 && isShow){
            videoActivity.findViewById(R.id.view_playlist).setVisibility(View.VISIBLE);
            videoActivity.findViewById(R.id.exo_view_play_info).setVisibility(View.GONE);
        }
        if(y2>y1 && !isShow){
            videoActivity.findViewById(R.id.view_playlist).setVisibility(View.GONE);
            videoActivity.findViewById(R.id.exo_view_play_info).setVisibility(View.VISIBLE);
        }
    }

    private void controlBright(float y1, float y2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(mContext)) {
                int brightnessValue = Settings.System.getInt(
                        mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS,
                        0
                );
                if(brightnessValue <= 255 && brightnessValue>= 0) {
                    if (y1 > y2) {
                        if (y1 - y2 > 10)
                            brightnessValue += 8;
                    } else {
                        if (y2 - y1 > 10)
                            brightnessValue -= 8;
                    }
                    if (brightnessValue > 255) brightnessValue = 255;
                    if (brightnessValue < 0) brightnessValue = 0;
                    Settings.System.putInt(mContext.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS,
                            brightnessValue);
                    brightSeekBar.setProgress(brightnessValue);
                }
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + videoActivity.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        } else {
            int brightnessValue = Settings.System.getInt(
                    mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    0
            );
            if(brightnessValue <= 255 && brightnessValue>= 0) {
                if (y1 > y2) {
                    if (y1 - y2 > 10)
                        brightnessValue += 8;
                } else {
                    if (y2 - y1 > 10)
                        brightnessValue -= 8;
                }
                if (brightnessValue > 255) brightnessValue = 255;
                if (brightnessValue < 0) brightnessValue = 0;
                Settings.System.putInt(mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS,
                        brightnessValue);
                brightSeekBar.setProgress(brightnessValue);
            }
        }
    }


    private void controlMediaVolume(float y1, float y2){
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(currentVolume <= 15) {
            if(currentVolume == 15)
                return;
            if (y1 > y2) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
            } else {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
            }
        }
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d("TAGVOLUME", String.valueOf(currentVolume));
        volumeSeekBar.setProgress(currentVolume);
    }
}
