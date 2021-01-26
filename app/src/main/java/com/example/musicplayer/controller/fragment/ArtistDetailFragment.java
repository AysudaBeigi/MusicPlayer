package com.example.musicplayer.controller.fragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.AlbumDetailAdapter;
import com.example.musicplayer.adapter.ArtistDetailAdopter;
import com.example.musicplayer.model.Music;
import com.example.musicplayer.repository.MusicRepository;
import com.example.musicplayer.utilities.MusicUtils;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.HashMap;


public class ArtistDetailFragment extends Fragment {
    private ShapeableImageView mImageViewCoverArtistDetail;
    private RecyclerView mRecyclerViewArtistDetail;
    private String mCurrentArtistName;
    private HashMap<String , ArrayList<Music>> mArtistHashMap;
    private MusicRepository mMusicRepository;


    public ArtistDetailFragment() {
        // Required empty public constructor
    }


    public static ArtistDetailFragment newInstance() {
        ArtistDetailFragment fragment = new ArtistDetailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMusicRepository= MusicRepository.getInstance(getActivity());
        mCurrentArtistName=mMusicRepository.getCurrentArtistName();
        mArtistHashMap=mMusicRepository.getArtists();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_artist_detail,
                container, false);
        findViews(view);
        initViews();
        return view;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initViews() {
        byte[] coverBitmap= MusicUtils.retrieveCover(
                mArtistHashMap.get(mCurrentArtistName).get(0).getData());
        MusicUtils.setCover(getActivity(),coverBitmap,mImageViewCoverArtistDetail);
        mRecyclerViewArtistDetail.setLayoutManager
                (new LinearLayoutManager(getActivity()));

        ArtistDetailAdopter adapter=new ArtistDetailAdopter(getActivity());
        mRecyclerViewArtistDetail.setAdapter(adapter);
    }

    private void findViews(View view) {
        mImageViewCoverArtistDetail=view.findViewById(R.id.image_view_cover_artist_detail);
        mRecyclerViewArtistDetail=view.findViewById(R.id.recycler_view_artist_detail);
    }
}