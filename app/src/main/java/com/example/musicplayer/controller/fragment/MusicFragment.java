package com.example.musicplayer.controller.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.musicplayer.R;
import com.example.musicplayer.controller.activity.PagerActivity;
import com.example.musicplayer.model.Music;
import com.example.musicplayer.service.MusicPlayerService;
import com.example.musicplayer.utilities.MusicUtils;
import com.example.musicplayer.repository.MusicRepository;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;


public class MusicFragment extends Fragment {
    private boolean mServiceBound;
    private ArrayList<Music> mCurrentMusicArrayList;
    private int mCurrentMusicIndex;
    private Music mActiveMusic;
    private MusicPlayerService mMusicPlayerService;
    private ShapeableImageView mImageViewCover;
    private MaterialTextView mTextViewTitle;
    private MaterialTextView mTextViewArtist;
    private AppCompatSeekBar mAppCompatSeekBar;
    private MaterialTextView mTextViewDuration;
    private MaterialTextView mTextViewTimeComeThrough;

    MusicRepository mMusicRepository;


    public MusicFragment() {
        // Required empty public constructor
    }


    public static MusicFragment newInstance() {
        MusicFragment fragment = new MusicFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMusicRepository = MusicRepository.getInstance(getActivity());
        mCurrentMusicArrayList = mMusicRepository.getCurrentMusicsList();
        mCurrentMusicIndex = mMusicRepository.getCurrentMusicIndex();
        mActiveMusic = mCurrentMusicArrayList.get(mCurrentMusicIndex);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music,
                container, false);
        findViews(view);
        initViews();
        playMusic();
        return view;
    }

    private void initViews() {
        byte[] coverBitmap = MusicUtils.
                retrieveCover(mActiveMusic.getData());
        if (coverBitmap != null)
            MusicUtils.setCover(getContext(), coverBitmap, mImageViewCover);
        mTextViewArtist.setText(mActiveMusic.getArtist());
        mTextViewTitle.setText(mActiveMusic.getTitle());
        mTextViewDuration.setText(mActiveMusic.getDuration());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void playMusic() {
        Intent playerIntent = MusicPlayerService.newIntent(getActivity());
        if (!mServiceBound) {
            Log.d(PagerActivity.TAG, "playAudio + !service bound:");

            getActivity().startService(playerIntent);
            getActivity().bindService(playerIntent,
                    mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            Log.d(PagerActivity.TAG, "playAudio + service bound:");
            getActivity().bindService(playerIntent, mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    private void findViews(View view) {
        mTextViewTitle = view.findViewById(R.id.text_view_music_fragment);
        mTextViewArtist = view.findViewById(R.id.text_view_artist_music_fragment);
        mTextViewDuration = view.findViewById(R.id.text_view_duration);
        mTextViewTimeComeThrough = view.findViewById(R.id.text_view_time_come_through);
        mImageViewCover = view.findViewById(R.id.image_view_cover);
        mAppCompatSeekBar = view.findViewById(R.id.seekbar_playing);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(PagerActivity.TAG, "MusicF: onServiceConnected");

            MusicPlayerService.LocalBinder binder =
                    (MusicPlayerService.LocalBinder) service;
            mMusicPlayerService = binder.getService();
            mServiceBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            mServiceBound = false;
        }
    };


}