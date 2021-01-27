package com.example.musicplayer.controller.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.example.musicplayer.R;
import com.example.musicplayer.model.Music;
import com.example.musicplayer.repository.MusicRepository;
import com.example.musicplayer.service.MusicPlayerService;
import com.example.musicplayer.utilities.MusicUtils;
import com.example.musicplayer.utilities.SeekbarUtils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class MusicActivity extends AppCompatActivity {


    private ShapeableImageView mImageViewNext;
    private ShapeableImageView mImageViewPlayPause;
    private ShapeableImageView mImageViewPrevious;
    private ShapeableImageView mImageViewRepeat;
    private ShapeableImageView mImageViewShuffle;
    private ShapeableImageView mImageViewCover;
    private AppCompatSeekBar mAppCompatSeekBar;
    private MusicRepository mMusicRepository;
    private MaterialTextView mTextViewTitle;
    private MaterialTextView mTextViewArtist;
    private MaterialTextView mTextViewDuration;
    private MaterialTextView mTextViewDurationPlayed;
    private MusicPlayerService mMusicPlayerService;
    private ArrayList<Music> mCurrentMusicArrayList;
    private Music mActiveMusic;
    private int mCurrentMusicIndex;
    private boolean mServiceBound = false;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, MusicActivity.class);
        return intent;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        mMusicRepository = MusicRepository.getInstance(this);
        findViews();
        initViews();
        setListeners();
        //  playMusic();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();
        playMusic();

    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConnection);
        mServiceBound = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)

    private void setListeners() {
        mAppCompatSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mMusicPlayerService != null && fromUser) {
                    mMusicPlayerService.seekMedia(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mImageViewShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMusicRepository.getShuffleState()) {
                    setShufflePlayState(false, R.drawable.shuffle_off);
                } else {
                    setShufflePlayState(true, R.drawable.shufffle_on);
                }
            }

            private void setShufflePlayState(boolean state,
                                             int shuffleImage) {
                mMusicRepository.setShuffleState(state);
                mImageViewShuffle.setImageResource(shuffleImage);
            }
        });
        mImageViewRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMusicRepository.getRepeatState()) {
                    setRepeatPlayState(false, R.drawable.repeat_off);
                } else {
                    setRepeatPlayState(true, R.drawable.repeat_on);
                }
            }

            private void setRepeatPlayState(boolean state, int repeatImage) {
                mMusicRepository.setRepeatState(state);
                mImageViewRepeat.setImageResource(repeatImage);
            }
        });
        mImageViewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMusicPlayerService.nextMediaPlay();
                initViews();
            }
        });
        mImageViewPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMusicPlayerService.previousMediaPlay();
                initViews();
            }
        });
        mImageViewPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicRepository.getPlayPauseSate().equals("play")){
                    mMusicRepository.setPlayPauseSate("pause");
                    mMusicPlayerService.onPause();
                }
                else{
                    mMusicRepository.setPlayPauseSate("play");
                    mMusicPlayerService.onPlay();
                }
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initViews() {
        mServiceBound = mMusicRepository.getServiceBound();
        mCurrentMusicArrayList = mMusicRepository.getCurrentMusicsList();
        mCurrentMusicIndex = mMusicRepository.getCurrentMusicIndex();
        mActiveMusic = mCurrentMusicArrayList.get(mCurrentMusicIndex);
        byte[] coverBitmap = MusicUtils.
                retrieveCover(mActiveMusic.getData());
        if (coverBitmap != null)
            MusicUtils.setCover(this, coverBitmap, mImageViewCover);
        mTextViewArtist.setText(mActiveMusic.getArtist());
        mTextViewTitle.setText(mActiveMusic.getTitle());
        mTextViewDuration.setText(mActiveMusic.getDuration());
        if (mServiceBound) {
            int currentPosition = mMusicPlayerService.getMediaCurrentPosition() / 1000;
            mAppCompatSeekBar.setProgress(currentPosition);
            mTextViewDurationPlayed.
                    setText(SeekbarUtils.getProgressPlayedTimeFormat(currentPosition));

        }

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void playMusic() {
        Log.d(PagerActivity.TAG, "playAudio + !service bound:");
        Intent serviceIntent = MusicPlayerService.newIntent(this);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
        // mMusicPlayerService.playMedia();
        //startService(MusicPlayerService.newIntent(this));
        //  stopService()

    }

    private void findViews() {
        mTextViewTitle = findViewById(R.id.text_view_music_fragment);
        mTextViewArtist = findViewById(R.id.text_view_artist_music_fragment);
        mTextViewDuration = findViewById(R.id.text_view_duration);
        mTextViewDurationPlayed = findViewById(R.id.text_view_duration_played);
        mImageViewCover = findViewById(R.id.image_view_cover);
        mAppCompatSeekBar = findViewById(R.id.seekbar_playing);
        mImageViewNext = findViewById(R.id.image_view_next);
        mImageViewPlayPause = findViewById(R.id.image_view_play_pause);
        mImageViewPrevious = findViewById(R.id.image_view_previouse);
        mImageViewShuffle = findViewById(R.id.image_view_shuffle);
        mImageViewRepeat = findViewById(R.id.image_view_repeat);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(PagerActivity.TAG, "MusicF: onServiceConnected");

            MusicPlayerService.LocalBinder binder =
                    (MusicPlayerService.LocalBinder) service;
            mMusicPlayerService = binder.getService();
            mMusicRepository.setServiceBound(true);

        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(PagerActivity.TAG, "MusicF: onServiceDisconnected");

            mMusicRepository.setServiceBound(false);
        }
    };


}

