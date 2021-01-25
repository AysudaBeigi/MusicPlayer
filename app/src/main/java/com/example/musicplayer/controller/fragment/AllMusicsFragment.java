package com.example.musicplayer.controller.fragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.musicplayer.R;
import com.example.musicplayer.adapter.AllMusicsAdapter;
import com.example.musicplayer.controller.activity.PagerActivity;


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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(PagerActivity.TAG,"all music F onCreate");
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(PagerActivity.TAG,"all music F onCreateView");

        View view= inflater.inflate(R.layout.fragment_all_musics,
                container, false);
        findViews(view);

        initViews();

        return view;
    }

    private void findViews(View view) {
        mRecyclerView = view.findViewById(R.id.recycler_view_all_musics);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initViews() {
        Log.d(PagerActivity.TAG,"all music F initViews");

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        AllMusicsAdapter adapter=new AllMusicsAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);
    }

}