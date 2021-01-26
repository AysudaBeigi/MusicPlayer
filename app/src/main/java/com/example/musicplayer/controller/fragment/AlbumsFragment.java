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
import com.example.musicplayer.adapter.AlbumsAdapter;


public class AlbumsFragment extends Fragment {

    private RecyclerView mRecyclerViewAlbums;

    public AlbumsFragment() {
        // Required empty public constructor
    }


    public static AlbumsFragment newInstance() {
        AlbumsFragment fragment = new AlbumsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_album,
                container, false);
        findViews(view);
        initViews();

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initViews() {
        mRecyclerViewAlbums.
                setLayoutManager(new GridLayoutManager(
                        getActivity(),2));
        AlbumsAdapter albumsAdapter=new AlbumsAdapter(getActivity());
        mRecyclerViewAlbums.setAdapter(albumsAdapter);
    }

    private void findViews(View view) {
        mRecyclerViewAlbums=view.findViewById(R.id.recycler_view_albums);
    }
}