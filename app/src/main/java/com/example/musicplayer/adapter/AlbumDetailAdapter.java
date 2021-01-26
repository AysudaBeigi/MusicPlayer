package com.example.musicplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumDetailAdapter extends
        RecyclerView.Adapter<AlbumDetailAdapter.AlbumDetailHolder> {

    private Context mContext;
    public AlbumDetailAdapter(Context context) {
        mContext=context;
    }

    @NonNull
    @Override
    public AlbumDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumDetailHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
    public class AlbumDetailHolder extends RecyclerView.ViewHolder{

        public AlbumDetailHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
