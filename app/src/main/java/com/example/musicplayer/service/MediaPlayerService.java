package com.example.musicplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.musicplayer.controller.activity.MainActivity;
import com.example.musicplayer.model.Audio;
import com.example.musicplayer.model.PlaybackStatus;
import com.example.musicplayer.utilities.StorageUtils;

import java.io.IOException;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MediaPlayerService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {


    public static final String TAG = "MediaPlayerError";
    public static final String EXTRA_MEDIA_FILE = "com.example.musicplayer.extraMedia";
    private final IBinder mIBinder = new LocalBinder();
    private MediaPlayer mMediaPlayer;
    private String mMediaFile;
    private int mResumePosition;
    private AudioManager mAudioManager;
    private AudioAttributes mPlaybackAttributes = buildAudioAttributes();
    private AudioFocusRequest mFocusRequest = buildAudioFocusRequest();
    //private Handler mHandler=new Handler();
    private boolean mOngoingCall = false;
    private PhoneStateListener mPhoneStateListener;
    private TelephonyManager mTelephonyManager;
    private ArrayList<Audio> mAudioArrayList;
    private int mAudioIndex=-1;
    private Audio mActiveAudio;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, MediaPlayerService.class);
        return intent;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
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

    @Override
    public void onCreate() {
        super.onCreate();

        callStateListener();
        registerBecomingNoisyReceiver();
        registerPlayNewAudioReceiver();
    }

    private AudioAttributes buildAudioAttributes() {

        mPlaybackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        return mPlaybackAttributes;
    }


    private AudioFocusRequest buildAudioFocusRequest() {
        mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mPlaybackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true) //setAcceptsDelayedFocusGain
                .setOnAudioFocusChangeListener(this) //todo: generate handler for notif thread
                .build();
        return mFocusRequest; //willPauseWhenDucked(true

    }

    private boolean requestAudioFocus() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = mAudioManager.requestAudioFocus(mFocusRequest);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        return false;

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            mMediaFile = intent.getExtras().getString(EXTRA_MEDIA_FILE);
        } catch (NullPointerException e) {
            stopSelf();
        }
        if (!requestAudioFocus())
            stopSelf();
        if (mMediaFile != null && !mMediaFile.equals(""))
            initMediaPLayer();

        return super.onStartCommand(intent, flags, startId);
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


    private BroadcastReceiver mBecomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);

        }
    };

    private void registerBecomingNoisyReceiver() {
        IntentFilter intentFilter =
                new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mBecomingNoisyReceiver, intentFilter);

    }

    private void callStateListener() {

        mTelephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mMediaPlayer != null) {
                            pauseMedia();
                            mOngoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if(mMediaPlayer!=null&&mOngoingCall){

                            mOngoingCall=false;
                            resumeMedia();
                        }
                        break;


                }
            }
        };
        mTelephonyManager.listen(
                mPhoneStateListener
                ,PhoneStateListener.LISTEN_CALL_STATE);
    }


    private  BroadcastReceiver mPlayNewAudioReceiver =new  BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            mAudioIndex= new StorageUtils(getApplicationContext()).loadAudioIndex();
            if(mAudioIndex!=-1&&mAudioIndex<mAudioArrayList.size()){
                mActiveAudio=mAudioArrayList.get(mAudioIndex);
            }else {
                stopSelf();
            }
            stopMedia();
            mMediaPlayer.reset();
            initMediaPLayer();
            updateMetaDate();
            buildNotification(PlaybackStatus.PLAYING);

        }
    };
    private void registerPlayNewAudioReceiver(){
        IntentFilter intentFilter=new IntentFilter(MainActivity.ACTION_PLAY_NEW_AUDIO);
        registerReceiver(mPlayNewAudioReceiver,intentFilter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopMedia();
            mMediaPlayer.release();
        }
        removeAudioFocus();
        if(mPhoneStateListener!=null){
            mTelephonyManager.listen(mPhoneStateListener,
                    PhoneStateListener.LISTEN_NONE);
        }
        removeNotification();
        unregisterReceiver(mBecomingNoisyReceiver);
        unregisterReceiver(mPlayNewAudioReceiver);
        new StorageUtils(getApplicationContext()).clearCashedAudioPlayList();

    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.abandonAudioFocusRequest(mFocusRequest);
    }
}
