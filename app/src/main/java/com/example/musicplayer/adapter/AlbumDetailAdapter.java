package com.example.musicplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.controller.activity.MusicActivity;
import com.example.musicplayer.model.Music;
import com.example.musicplayer.repository.MusicRepository;
import com.example.musicplayer.utilities.MusicUtils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class AlbumDetailAdapter extends
        RecyclerView.Adapter<AlbumDetailAdapter.AlbumDetailHolder> {

    private Context mContext;
    private MusicRepository mMusicRepository;
    private ArrayList<Music> mAlbumMusicsArrayList;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public AlbumDetailAdapter(Context context) {
        mContext = context;
        mMusicRepository = MusicRepository.getInstance(mContext);
        mAlbumMusicsArrayList = mMusicRepository.getCurrentMusicsList();

    }

    @NonNull
    @Override
    public AlbumDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).
                inflate(R.layout.music_item, parent, false);
        return new AlbumDetailHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumDetailHolder holder, int position) {

        holder.binItemViews(position);
    }

    @Override
    public int getItemCount() {
        return mAlbumMusicsArrayList.size();
    }

    public class AlbumDetailHolder extends RecyclerView.ViewHolder {
        private ShapeableImageView mImageViewMusicItem;
        private MaterialTextView mTextViewTitleMusicItem;
        private int mPosition;

        public AlbumDetailHolder(@NonNull View itemView) {
            super(itemView);
            findItemViews(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    mMusicRepository.setCurrentMusicIndex(mPosition);
                    startMusicActivity();

                }
            });

        }

        private void findItemViews(@NonNull View itemView) {
            mImageViewMusicItem = itemView.findViewById(R.id.image_view_music_item);
            mTextViewTitleMusicItem = itemView.findViewById(R.id.text_view_title_music_item);
        }

        private void binItemViews(int position) {
            mPosition = position;
            mTextViewTitleMusicItem.setText(mAlbumMusicsArrayList.get(position).getTitle());
            byte[] coverBitmap = MusicUtils.
                    retrieveCover(mAlbumMusicsArrayList.get(position).getData());
            if (coverBitmap != null)
                MusicUtils.setCover(mContext, coverBitmap, mImageViewMusicItem);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMusicActivity() {
        Intent intent = MusicActivity.newIntent(mContext);
        mContext.startActivity(intent);
    }
}
