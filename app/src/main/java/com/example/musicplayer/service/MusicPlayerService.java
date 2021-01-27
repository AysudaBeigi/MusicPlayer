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
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
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
import com.example.musicplayer.controller.activity.PagerActivity;
import com.example.musicplayer.model.Music;
import com.example.musicplayer.model.PlaybackState;
import com.example.musicplayer.utilities.MusicUtils;
import com.example.musicplayer.repository.MusicRepository;

import java.io.IOException;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MusicPlayerService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String ERROR_TAG = "MediaPlayerError";
    public static final String AUDIO_PLAYER = "AudioPlayer";
    public static final String ACTION_PLAY = "com.example.musicplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.musicplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.musicplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.musicplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.musicplayer.ACTION_STOP";
    private static final int NOTIFICATION_ID = 100;
    public static final int NEXT = 100;
    public static final int PREVIOUS = 200;
    private final IBinder mIBinder = new LocalBinder();
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private AudioFocusRequest mFocusRequest;
    //private Handler mHandler=new Handler();
    private PhoneStateListener mPhoneStateListener;
    private TelephonyManager mTelephonyManager;
    private ArrayList<Music> mCurrentMusicArrayList;
    private Music mActiveMusic;
    private MediaSessionManager mMediaSessionManager;
    private MediaSessionCompat mMediaSession;
    private MediaControllerCompat.TransportControls mTransportControls;
    private int mCurrentMusicIndex = -1;
    private int mResumePosition;
    private boolean mOngoingCall = false;
    private MusicRepository mMusicRepository;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, MusicPlayerService.class);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(ERROR_TAG, "onCreate MediaPlayerService");
        // callStateListener();
    }


    public class LocalBinder extends Binder {

        public MusicPlayerService getService() {
            Log.d(ERROR_TAG, " getService");

            return MusicPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(ERROR_TAG, " onBind");

        return mIBinder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(PagerActivity.TAG, "onStartCommand");
        try {
            mMusicRepository = MusicRepository.getInstance(getApplicationContext());
            mCurrentMusicArrayList = mMusicRepository.getCurrentMusicsList();
            mCurrentMusicIndex = mMusicRepository.getCurrentMusicIndex();

            mActiveMusic = mCurrentMusicArrayList.get(mCurrentMusicIndex);


        } catch (NullPointerException e) {
            stopSelf();

        }

        if (!requestAudioFocus()) {
            stopSelf();
        }

        if (mMediaSessionManager == null) {
            try {
                initMediaSession();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackState.PLAYING);
        }

        initMediaPLayer();
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(PagerActivity.TAG, "onPrepared");
        playMedia();
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(PagerActivity.TAG, "onCompletion");
        nextMediaPlay();

    }


    public void seekMedia(int progress) {
        mMediaPlayer.seekTo(progress);
    }

    public int getMediaCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    private void initMediaPLayer() {
        Log.d(PagerActivity.TAG, "initMediaPLayer");
        if (mMediaPlayer == null)
            mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        try {
            mMediaPlayer.setDataSource(mActiveMusic.getData());
        } catch (IOException e) {
            Log.d(ERROR_TAG, "initMediaPLayer : catch (IOException e)");
            e.printStackTrace();
            stopSelf();
        }
        mMediaPlayer.prepareAsync();

    }

    public void playMedia() {
        Log.d(PagerActivity.TAG, "playMedia");
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public void stopMedia() {
        Log.d(PagerActivity.TAG, "stopMedia");

        if (mMediaPlayer == null)
            return;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    public void pauseMedia() {
        Log.d(PagerActivity.TAG, "pauseMedia");

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mResumePosition = mMediaPlayer.getCurrentPosition();
        }
    }

    public void resumeMedia() {
        Log.d(PagerActivity.TAG, "resumeMedia");

        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(mResumePosition);
            mMediaPlayer.start();
        }
    }

    public void onPlay() {
        resumeMedia();
        buildNotification(PlaybackState.PLAYING);
    }

    public void onPause() {
        pauseMedia();
        buildNotification(PlaybackState.PAUSED);

    }

    public void nextMediaPlay() {

        skipToNext();
        updateMetaData();
        buildNotification(PlaybackState.PLAYING);

    }

    public void previousMediaPlay() {

        skipToPrevious();
        updateMetaData();
        buildNotification(PlaybackState.PLAYING);

    }

    /*private class BecomingNoisyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
            buildNotification(PlaybackState.PAUSED);

        }
    }
*/;

   /* private void registerBecomingNoisyReceiver() {
        Log.d(ERROR_TAG, " registerBecomingNoisyReceiver");

        mBecomingNoisyReceiver = new BecomingNoisyReceiver();
        IntentFilter intentFilter =
                new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mBecomingNoisyReceiver, intentFilter);

    }
*/


    private void initMediaSession() throws RemoteException {
        Log.d(PagerActivity.TAG, "initMediaSession");
        if (mMediaSessionManager != null)
            return;
        mMediaSessionManager = getSystemService(MediaSessionManager.class);

        mMediaSession =
                new MediaSessionCompat(getApplicationContext(),
                        AUDIO_PLAYER);
        mTransportControls =
                mMediaSession.getController().getTransportControls();

        mMediaSession.setActive(true);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        //set media session's  metadata
        updateMetaData();
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {

            @Override
            public void onPlay() {
                Log.d(PagerActivity.TAG, "initMediaSession :setCallback");
                Log.d(PagerActivity.TAG, "initMediaSession :onPlay");

                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackState.PLAYING);
            }

            @Override
            public void onPause() {
                Log.d(PagerActivity.TAG, "initMediaSession :onPause");

                super.onPause();
                pauseMedia();
                buildNotification(PlaybackState.PAUSED);

            }

            @Override
            public void onSkipToNext() {
                Log.d(PagerActivity.TAG, "initMediaSession :onSkipToNext");

                super.onSkipToNext();
                nextMediaPlay();
            }

            @Override
            public void onSkipToPrevious() {
                Log.d(PagerActivity.TAG, "initMediaSession :onSkipToPrevious");

                super.onSkipToPrevious();
                previousMediaPlay();
            }

            @Override
            public void onStop() {
                Log.d(PagerActivity.TAG, "initMediaSession :onStop");

                super.onStop();
                removeNotification();
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                Log.d(PagerActivity.TAG, "onSeekTo ");

                super.onSeekTo(position);
            }
        });

    }

    private void updateMetaData() {
        Log.d(PagerActivity.TAG, "updateMetaData ");

        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.drawable.violon);
        mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mActiveMusic.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mActiveMusic.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mActiveMusic.getTitle())
                .build());

    }

    private void skipToNext() {
        Log.d(PagerActivity.TAG, "skipToNext ");
        checkMusicState(NEXT);

        mMusicRepository.setCurrentMusicIndex(mCurrentMusicIndex);
        stopMedia();
        mMediaPlayer.reset();
        initMediaPLayer();
    }

    private void checkMusicState(int nextPrevious) {
        if (mMusicRepository.getRepeatState()) {
            mActiveMusic = mCurrentMusicArrayList.get(mCurrentMusicIndex);
        } else if (mMusicRepository.getShuffleState()) {
            int randomIndex = MusicUtils.
                    getRandomMMusicIndex(mCurrentMusicArrayList.size());
            mActiveMusic = mCurrentMusicArrayList.get(randomIndex);
        } else {

            switch (nextPrevious) {
                case 100://next
                    if (mCurrentMusicIndex == mCurrentMusicArrayList.size() - 1) {
                        mCurrentMusicIndex = 0;
                        mActiveMusic = mCurrentMusicArrayList.get(mCurrentMusicIndex);
                    } else {
                        mActiveMusic = mCurrentMusicArrayList.get(++mCurrentMusicIndex);
                    }
                    break;
                case 200://prevois
                    if (mCurrentMusicIndex == 0) {
                        mCurrentMusicIndex = mCurrentMusicArrayList.size() - 1;
                        mActiveMusic = mCurrentMusicArrayList.get(mCurrentMusicIndex);
                    } else {
                        mActiveMusic = mCurrentMusicArrayList.get(--mCurrentMusicIndex);
                    }
                    break;
            }
        }
    }

    private void skipToPrevious() {
        Log.d(PagerActivity.TAG, "skipToPrevious ");

        checkMusicState(PREVIOUS);
        mMusicRepository.setCurrentMusicIndex(mCurrentMusicIndex);
        stopMedia();
        mMediaPlayer.reset();
        initMediaPLayer();
    }


    private void buildNotification(PlaybackState playbackState) {
        Log.d(PagerActivity.TAG, "buildNotification");
        int notificationAction = android.R.drawable.ic_media_pause;
        PendingIntent playPauseAction = null;
        if (playbackState == PlaybackState.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            playPauseAction = playbackAction(1);
        } else if (playbackState == PlaybackState.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            playPauseAction = playbackAction(0);
        }
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.violon);
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

                                        .setShowActionsInCompactView(0, 1, 2))
                        .setColor(getResources().getColor(R.color.orange_00))
                        .setLargeIcon(largeIcon)
                        .setSmallIcon(android.R.drawable.stat_sys_headset)
                        .setContentText(mActiveMusic.getArtist())
                        .setContentTitle(mActiveMusic.getAlbum())
                        .setContentInfo(mActiveMusic.getTitle())
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
        Log.d(PagerActivity.TAG, "removeNotification ");

        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(this);

        notificationManagerCompat.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int requestCode) {
        Log.d(PagerActivity.TAG, "playbackAction : requestCode" + requestCode);
        Intent playbackAction =
                new Intent(this, MusicPlayerService.class);
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
                        requestCode, playbackAction, 0);

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
        Log.d(PagerActivity.TAG, "handleIncomingActions");
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
        Log.d(PagerActivity.TAG, "onDestroy");

        super.onDestroy();
        if (mMediaPlayer != null) {
            stopMedia();
            mMediaPlayer.release();
        }
        removeAudioFocus();
        if (mPhoneStateListener != null) {
            mTelephonyManager.listen(mPhoneStateListener,
                    PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();
        MusicRepository.getInstance(getApplicationContext()).clearCashedAllMusicsList();

    }

    private void callStateListener() {

        Log.d(PagerActivity.TAG, "callStateListener");

        mTelephonyManager = getSystemService(TelephonyManager.class);
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

    private boolean removeAudioFocus() {

        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.abandonAudioFocusRequest(mFocusRequest);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d(ERROR_TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d(ERROR_TAG, "MEDIA_ERROR_SERVER_DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d(ERROR_TAG, "MEDIA_ERROR_UNKNOWN " + extra);
                break;

        }
        return false;
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        Log.d(PagerActivity.TAG, "onAudioFocusChange ");

        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
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


    private AudioFocusRequest buildAudioFocusRequest() {
        return new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(MusicUtils.buildAudioAttributes())
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(this)//setAcceptsDelayedFocusGain
                .build();

    }

    private boolean requestAudioFocus() {
        mFocusRequest = buildAudioFocusRequest();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = mAudioManager.requestAudioFocus(mFocusRequest);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        return false;

    }

}