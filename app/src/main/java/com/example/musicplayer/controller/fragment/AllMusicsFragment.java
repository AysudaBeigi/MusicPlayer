package com.example.musicplayer.controller.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.AllMusicsAdapter;


public class AllMusicsFragment extends Fragment {

    private RecyclerView mRecyclerView;


    public AllMusicsFragment() {
        // Required empty public constructor
    }


    public static AllMusicsFragment newInstance() {
        AllMusicsFragment fragment = new AllMusicsFragment();
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
        View view= inflater.inflate(R.layout.fragment_all_musics,
                container, false);
        findViews(view);

        initViews();

        return view;
    }

    private void findViews(View view) {
        mRecyclerView = view.findViewById(R.id.recycler_view_all_musics);
    }

    private void initViews() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        AllMusicsAdapter adapter=new AllMusicsAdapter();
        mRecyclerView.setAdapter(adapter);
    }

}