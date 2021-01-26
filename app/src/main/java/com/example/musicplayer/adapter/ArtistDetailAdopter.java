package com.example.musicplayer.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ArtistDetailAdopter extends
        RecyclerView.Adapter<ArtistDetailAdopter.ArtistDetailHolder>  {


    @NonNull
    @Override
    public ArtistDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistDetailHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ArtistDetailHolder extends RecyclerView.ViewHolder{

        public ArtistDetailHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
