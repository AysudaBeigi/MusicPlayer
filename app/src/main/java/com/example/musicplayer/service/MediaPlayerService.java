package com.example.musicplayer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;

public class MediaPlayerService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {


    public static final String TAG = "MediaPlayerError";
    private final IBinder mIBinder = new LocalBinder();
    private MediaPlayer mMediaPlayer;
    private String mMediaFile;
    private int mResumePosition;
    private AudioManager mAudioManager;
    private AudioAttributes mPlaybackAttributes;
    private AudioFocusRequest mFocusRequest;

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMedia();
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d(TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d(TAG, "MEDIA_ERROR_SERVER_DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d(TAG, "MEDIA_ERROR_UNKNOWN " + extra);
                break;

        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        //Invoked to communicate some info.
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                //resume play back
                if (mMediaPlayer == null) {
                    initMediaPLayer();
                } else if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                }
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            mMediaFile = intent.getExtras().getString("media");
        } catch (NullPointerException e) {
            stopSelf();
        }
        if (!requestAudioFocus())
            stopSelf();
        if (mMediaFile != null && mMediaFile != "")
            initMediaPLayer();

        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopMedia();
            mMediaPlayer.release();
        }
        removeAudioFocus();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AudioAttributes buildAudioAttributes() {

        mPlaybackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        return mPlaybackAttributes;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private AudioFocusRequest buildAudioFocusRequest() {
        mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mPlaybackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(this, null) //todo: generate handler for notif thread
                .build();
        return mFocusRequest;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean requestAudioFocus() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = mAudioManager.
                requestAudioFocus(mFocusRequest);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        return false;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.abandonAudioFocusRequest(mFocusRequest);
    }


    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }


    private void initMediaPLayer() {


        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mMediaFile);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mMediaPlayer.prepareAsync();


    }

    private void playMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mMediaPlayer == null)
            return;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mResumePosition = mMediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(mResumePosition);
            mMediaPlayer.start();
        }
    }


}