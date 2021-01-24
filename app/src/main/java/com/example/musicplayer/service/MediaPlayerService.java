package com.example.musicplayer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.musicplayer.R;
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
    public static final String AUDIO_PLAYER = "AudioPlayer";
    public static final int NOTIFICATION_ID = 0;
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
    private int mAudioIndex = -1;
    private Audio mActiveAudio;
    public static final String ACTION_PLAY = "com.example.musicplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.musicplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.musicplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.musicplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.musicplayer.ACTION_STOP";
    private MediaSessionManager mMediaSessionManager;
    private MediaSessionCompat mMediaSession;
    private MediaControllerCompat.TransportControls mTransportControls;
    private static final int NOTIFICATION_ID = 1;

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
            StorageUtils storageUtils = new StorageUtils(getApplicationContext());
            mAudioArrayList = storageUtils.loadAudios();
            mAudioIndex = storageUtils.loadAudioIndex();
            if (mAudioIndex != -1 && mAudioIndex < mAudioArrayList.size()) {
                //index is in a valid range
                mActiveAudio = mAudioArrayList.get(mAudioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }
        if (mMediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPLayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        handleIncomingActions(intent);
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
            mMediaPlayer.setDataSource(mActiveAudio.getData());
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
                        if (mMediaPlayer != null && mOngoingCall) {

                            mOngoingCall = false;
                            resumeMedia();
                        }
                        break;


                }
            }
        };
        mTelephonyManager.listen(
                mPhoneStateListener
                , PhoneStateListener.LISTEN_CALL_STATE);
    }


    private BroadcastReceiver mPlayNewAudioReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mAudioIndex = new StorageUtils(getApplicationContext()).loadAudioIndex();
            if (mAudioIndex != -1 && mAudioIndex < mAudioArrayList.size()) {
                mActiveAudio = mAudioArrayList.get(mAudioIndex);
            } else {
                stopSelf();
            }
            stopMedia();
            mMediaPlayer.reset();
            initMediaPLayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);

        }
    };

    private void registerPlayNewAudioReceiver() {
        IntentFilter intentFilter =
                new IntentFilter(MainActivity.ACTION_PLAY_NEW_AUDIO);
        registerReceiver(mPlayNewAudioReceiver, intentFilter);
    }

    private void initMediaSession() throws RemoteException {
        if (mMediaSessionManager != null)
            return;
        mMediaSessionManager = getSystemService(MediaSessionManager.class);

        mMediaSession =
                new MediaSessionCompat(getApplicationContext(),
                        AUDIO_PLAYER);
        mTransportControls = mMediaSession.
                getController().getTransportControls();

        mMediaSession.setActive(true);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        //set media session's  metadata
        updateMetaData();
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);

            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);

            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);

            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });

    }

    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.mipmap.music_player_image);
        mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mActiveAudio.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mActiveAudio.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mActiveAudio.getTitle())
                .build());

    }

    private void skipToNext() {
        if (mAudioIndex == mAudioArrayList.size() - 1) {
            mAudioIndex = NOTIFICATION_ID;
            mActiveAudio = mAudioArrayList.get(mAudioIndex);
        } else {
            mActiveAudio = mAudioArrayList.get(++mAudioIndex);
        }
        new StorageUtils(getApplicationContext()).storeAudioIndex(mAudioIndex);
        stopMedia();
        mMediaPlayer.reset();
        initMediaPLayer();
    }

    private void skipToPrevious() {
        if (mAudioIndex == NOTIFICATION_ID) {
            mAudioIndex = mAudioArrayList.size() - 1;
            mActiveAudio = mAudioArrayList.get(mAudioIndex);
        } else {
            mActiveAudio = mAudioArrayList.get(--mAudioIndex);
        }
        new StorageUtils(getApplicationContext()).storeAudioIndex(mAudioIndex);
        stopMedia();
        mMediaPlayer.reset();
        initMediaPLayer();
    }


    private void buildNotification(PlaybackStatus playbackStatus) {
        int notificationAction = android.R.drawable.ic_media_pause;
        PendingIntent playPauseAction = null;
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            playPauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            playPauseAction = playbackAction(NOTIFICATION_ID);
        }
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.music_player_image);
        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(this);

        Notification notification =
                new NotificationCompat.Builder(
                        this,
                        "music_player_channel_id")
                        .setShowWhen(false)
                        .setStyle(
                                new androidx.media.app.NotificationCompat.
                                        MediaStyle().
                                        setMediaSession(mMediaSession.getSessionToken())
                                        .setShowActionsInCompactView(NOTIFICATION_ID, 1, 2))
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setLargeIcon(largeIcon)
                        .setSmallIcon(android.R.drawable.stat_sys_headset)
                        .setContentText(mActiveAudio.getArtist())
                        .setContentTitle(mActiveAudio.getAlbum())
                        .setContentInfo(mActiveAudio.getTitle())
                        .addAction(android.R.drawable.ic_media_previous,
                                "previous", playbackAction(3))
                        .addAction(notificationAction, "pause",
                                playPauseAction)
                        .addAction(android.R.drawable.ic_media_next, "next",
                                playbackAction(2))
                        .build();

        notificationManagerCompat.notify(NOTIFICATION_ID, notification);

    }

    private void removeNotification() {
        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(this);

        notificationManagerCompat.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int requestCode) {
        Intent playbackAction =
                new Intent(this, MediaPlayerService.class);
        switch (requestCode) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this,
                        requestCode,
                        playbackAction,
                        0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this,
                        requestCode, playbackAction,
                        0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this,
                        requestCode, playbackAction,
                        0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this,
                        requestCode,
                        playbackAction,
                        0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null)
            return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            mTransportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            mTransportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            mTransportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mTransportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            mTransportControls.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopMedia();
            mMediaPlayer.release();
        }
        removeAudioFocus();
        //stop listening  to incoming calls and release TelephonyManager
        if (mPhoneStateListener != null) {
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
