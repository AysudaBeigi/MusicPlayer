package com.example.musicplayer.controller.fragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.AlbumDetailAdapter;
import com.example.musicplayer.model.Music;
import com.example.musicplayer.repository.MusicRepository;
import com.example.musicplayer.utilities.MusicUtils;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumDetailFragment extends Fragment {

    private ShapeableImageView mImageViewCoverAlbumDetail;
    private RecyclerView mRecyclerViewAlbumDetail;
    private String mCurrentAlbumName;
    private HashMap<String , ArrayList<Music>> mAlbumHashMap;
    private MusicRepository mMusicRepository;

    public AlbumDetailFragment() {
        // Required empty public constructor
    }


    public static AlbumDetailFragment newInstance() {
        AlbumDetailFragment fragment = new AlbumDetailFragment();
        Bundle args = new Bundle();
           fragment.setArguments(args);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMusicRepository= MusicRepository.getInstance(getActivity());
        mCurrentAlbumName=mMusicRepository.getCurrentAlbumName();
        mAlbumHashMap=mMusicRepository.getAlbums();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_album_detail,
                container, false);

        findViews(view);
        initViews();

        return view;
    }

    private void initViews() {
        byte[] coverBitmap= MusicUtils.retrieveCover(
                mAlbumHashMap.get(mCurrentAlbumName).get(0).getData());
        MusicUtils.setCover(getActivity(),coverBitmap,mImageViewCoverAlbumDetail);
        mRecyclerViewAlbumDetail.setLayoutManager
                (new GridLayoutManager(getActivity(),3));

        AlbumDetailAdapter adapter=new AlbumDetailAdapter(getActivity());
        mRecyclerViewAlbumDetail.setAdapter(adapter);
    }

    private void findViews(View view) {
        mImageViewCoverAlbumDetail=view.findViewById(R.id.image_view_cover_album_detail);
        mRecyclerViewAlbumDetail=view.findViewById(R.id.recycler_view_album_detail);
    }
}