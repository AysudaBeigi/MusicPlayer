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
import com.example.musicplayer.utilities.StorageUtils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;


public class MusicFragment extends Fragment {
    private boolean mServiceBound;
    private ArrayList<Music> mMusicArrayList;
    private int mMusicIndex;
    private Music mActiveMusic;
    private MusicPlayerService mMusicPlayerService;
    private ShapeableImageView mImageViewCover;
    private AppCompatSeekBar mAppCompatSeekBar;
    private MaterialTextView mTextViewTitle;
    private MaterialTextView mTextViewArtist;
    private MaterialTextView mTextViewDuration;
    private MaterialTextView mTextViewTimeComeThrough;


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
        StorageUtils storageUtils = new StorageUtils(getActivity());
        mMusicArrayList = storageUtils.loadMusicsList();
        mMusicIndex = storageUtils.loadMusicIndex();
        mActiveMusic=mMusicArrayList.get(mMusicIndex);
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
        setCover();
        mTextViewArtist.setText(mActiveMusic.getArtist());
        mTextViewTitle.setText(mActiveMusic.getTitle());
        mTextViewDuration.setText(mActiveMusic.getDuration());
    }

    private void setCover() {
        Glide.with(this)
                .asBitmap()
                .load(MusicUtils.retrieveCover(mActiveMusic.getData()))
                .into(mImageViewCover);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void playMusic(){
            Intent playerIntent = MusicPlayerService.newIntent(getActivity());
        if (!mServiceBound) {
            Log.d(PagerActivity.TAG, "playAudio + !service bound:" );

            getActivity().startService(playerIntent);
            getActivity().bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            Log.d(PagerActivity.TAG, "playAudio + service bound:" );
            getActivity().bindService(playerIntent,mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    private void findViews(View view) {
        mTextViewTitle=view.findViewById(R.id.text_view_title);
        mTextViewArtist=view.findViewById(R.id.text_view_artist);
        mTextViewDuration=view.findViewById(R.id.text_view_duration);
        mTextViewTimeComeThrough=view.findViewById(R.id.text_view_time_come_through);
        mImageViewCover=view.findViewById(R.id.image_view_cover);
        mAppCompatSeekBar=view.findViewById(R.id.seekbar_playing);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(PagerActivity.TAG, " onServiceConnected");

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