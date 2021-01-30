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
import android.os.Handler;
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

@RequiresApi(api = Build.VERSION_CODES.O)

public class MusicActivity extends AppCompatActivity {


    private ShapeableImageView mImageViewNext;
    private ShapeableImageView mImageViewPlayPause;
    private ShapeableImageView mImageViewPrevious;
    private ShapeableImageView mImageViewRepeat;
    private ShapeableImageView mImageViewShuffle;
    private ShapeableImageView mImageViewCover;
    private AppCompatSeekBar mSeekBar;
    private MusicRepository mMusicRepository;
    private MaterialTextView mTextViewTitle;
    private MaterialTextView mTextViewArtist;
    private MaterialTextView mTextViewDuration;
    private MaterialTextView mTextViewDurationPlayed;
    private MusicPlayerService mMusicPlayerService;
    private ArrayList<Music> mCurrentMusicsList;
    private int mCurrentMusicIndex;
    private Music mActiveMusic;
    private boolean mServiceBound;
    private boolean mFirstInit=true;
    private Handler mSeekbarUpdateHandler = new Handler();
    private Runnable mUpdateSeekBar = new Runnable() {
        @Override
        public void run() {
            int currentPosition = mMusicPlayerService.
                    getMediaCurrentPosition() / 1000;
            mSeekBar.setProgress(currentPosition);
            mTextViewDurationPlayed.
                    setText(SeekbarUtils.getProgressTimeFormat(currentPosition));
            mSeekbarUpdateHandler.postDelayed(this, 1000);
        }

    };

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, MusicActivity.class);
        return intent;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(PagerActivity.TAG, "MusicActivity : onCreate ");

        setContentView(R.layout.activity_music);
        startService(MusicPlayerService.newIntent(this));
        mMusicRepository = MusicRepository.getInstance(this);
        findViews();
        resetPlayPauseState();
        setListeners();

    }

    private void resetPlayPauseState() {
        mMusicRepository.setPlayPauseSate("play");
        mImageViewPlayPause.setImageResource(R.drawable.pause_2);
    }

    @Override
    protected void onStart() {
        Log.d(PagerActivity.TAG, "MusicActivity : onStart ");
        Intent serviceIntent = MusicPlayerService.newIntent(this);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);

        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConnection);
        mMusicRepository.setServiceBound(false);
    }


    private void setListeners() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                    setShufflePlayState(true, R.drawable.shuffle_on);
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
                    setRepeatPlayState(true, R.drawable.repeat_one_on);
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
                Log.d(PagerActivity.TAG, "getPlayPauseSate is " +
                        mMusicRepository.getPlayPauseSate());
                if (mMusicRepository.getPlayPauseSate().equals("play")) {
                    Log.d(PagerActivity.TAG, "state is play");
                    mMusicRepository.setPlayPauseSate("pause");
                    mImageViewPlayPause.setImageResource(R.drawable.play_2);
                    mMusicPlayerService.onPause();
                } else {
                    Log.d(PagerActivity.TAG, "state is pause");

                    mMusicRepository.setPlayPauseSate("play");
                    mImageViewPlayPause.setImageResource(R.drawable.pause_2);
                    mMusicPlayerService.onPlay();
                }
            }
        });
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initViews() {
        resetPlayPauseState();
        initData();
        byte[] coverBitmap = MusicUtils.
                retrieveCover(mActiveMusic.getData());
        if (coverBitmap != null)
            MusicUtils.setCover(this, coverBitmap, mImageViewCover);
        else
            mImageViewCover.setImageResource(R.drawable.violon10);
        mTextViewArtist.setText(mActiveMusic.getArtist());
        mTextViewTitle.setText(mActiveMusic.getTitle());
        int duration = Integer.parseInt(mActiveMusic.getDuration()) / 1000;
        mTextViewDuration.setText(SeekbarUtils.getProgressTimeFormat(duration));
        if (mMusicPlayerService != null) {

            mSeekBar.setMax(mMusicPlayerService.getMediaDuration() / 1000);
            int currentPosition = mMusicPlayerService.getMediaCurrentPosition() / 1000;
            mSeekBar.setProgress(currentPosition);
            mTextViewDurationPlayed.
                    setText(SeekbarUtils.getProgressTimeFormat(currentPosition));
        }


    }

    private void initData() {
        mServiceBound = mMusicRepository.getServiceBound();
        mCurrentMusicIndex=mMusicRepository.getCurrentMusicIndex();
       // if(mFirstInit){
            mActiveMusic=mMusicRepository.getCurrentMusicsList().get(mCurrentMusicIndex);
            mFirstInit=false;
       // }
       /*else {
        mActiveMusic = mMusicRepository.getActiveMusic();

        }*/
    }


    private void findViews() {

        mTextViewTitle = findViewById(R.id.text_view_music_fragment);
        mTextViewArtist = findViewById(R.id.text_view_artist_music_fragment);
        mTextViewDuration = findViewById(R.id.text_view_duration);
        mTextViewDurationPlayed = findViewById(R.id.text_view_duration_played);
        mImageViewCover = findViewById(R.id.image_view_cover);
        mSeekBar = findViewById(R.id.seekbar_playing);
        mImageViewNext = findViewById(R.id.image_view_next);
        mImageViewPlayPause = findViewById(R.id.image_view_play_pause);
        mImageViewPrevious = findViewById(R.id.image_view_prev);
        mImageViewShuffle = findViewById(R.id.image_view_shuffle);
        mImageViewRepeat = findViewById(R.id.image_view_repeat);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(PagerActivity.TAG, "MusicActivity: onServiceConnected");

            MusicPlayerService.LocalBinder binder =
                    (MusicPlayerService.LocalBinder) service;
            mMusicPlayerService = binder.getService();
            mMusicRepository.setServiceBound(true);

            initViews();
            mSeekbarUpdateHandler.postDelayed(mUpdateSeekBar, 1000);


        }


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(PagerActivity.TAG, "MusicF: onServiceDisconnected");

            mMusicRepository.setServiceBound(false);
        }
    };

}

