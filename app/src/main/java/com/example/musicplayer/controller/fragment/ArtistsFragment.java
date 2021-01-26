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
import com.example.musicplayer.adapter.ArtistsAdapter;

public class ArtistsFragment extends Fragment {

    private RecyclerView mRecyclerViewArtists;

    public ArtistsFragment() {
        // Required empty public constructor
    }


    public static ArtistsFragment newInstance() {
        ArtistsFragment fragment = new ArtistsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view= inflater.inflate(R.layout.fragment_artists,
               container, false);
       return view;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initViews() {
        mRecyclerViewArtists.
                setLayoutManager(new GridLayoutManager(
                        getActivity(),2));
        ArtistsAdapter artistsAdapter=new ArtistsAdapter(getActivity());
        mRecyclerViewArtists.setAdapter(artistsAdapter);
    }

    private void findViews(View view) {
        mRecyclerViewArtists=view.findViewById(R.id.recycler_view_artists);
    }
}