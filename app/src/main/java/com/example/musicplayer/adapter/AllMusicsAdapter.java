package com.example.musicplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.bumptech.glide.Glide;
import com.example.musicplayer.R;
import com.example.musicplayer.controller.activity.MusicActivity;
import com.example.musicplayer.controller.activity.PagerActivity;
import com.example.musicplayer.model.Music;
import com.example.musicplayer.utilities.StorageUtils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AllMusicsAdapter extends Adapter<AllMusicsAdapter.AllMusicsHolder> {
    private Context mContext;
    private ArrayList<Music> mAllMusicsList;

    public AllMusicsAdapter(Context context) {
        Log.d(PagerActivity.TAG, " AllMusicsAdapter");

        mContext = context;
        mAllMusicsList = new StorageUtils(mContext).loadMusicsList();

    }

    @NonNull
    @Override
    public AllMusicsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(PagerActivity.TAG, " onCreateViewHolder");

        View view = LayoutInflater.from(mContext).
                inflate(R.layout.music_item, parent, false);
        return new AllMusicsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllMusicsHolder holder, int position) {
        Log.d(PagerActivity.TAG, " onBindViewHolder");

        holder.bindView(position);

    }

    @Override
    public int getItemCount() {
        return mAllMusicsList.size();
    }

    public class AllMusicsHolder extends RecyclerView.ViewHolder {

        private ShapeableImageView mImageViewMusicItem;
        private MaterialTextView mTextViewTitle;
        private int mPosition;

        public AllMusicsHolder(@NonNull View itemView) {
            super(itemView);
            findItemViews(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StorageUtils storageUtils = new StorageUtils(mContext);
                    storageUtils.storeMusicIndex(mPosition);
                    storageUtils.storeMusicsList(mAllMusicsList);

                    startMusicActvity();
                }
            });

        }

        private void bindView(int position) {
            Log.d(PagerActivity.TAG, " bindView");

            mPosition = position;
            mTextViewTitle.setText(mAllMusicsList.get(position).getTitle());
            byte[] coverBitmap = retrieveCover(position);
            if (coverBitmap != null) {
                Glide.with(mContext)
                        .asBitmap()
                        .load(coverBitmap)
                        .into(mImageViewMusicItem);

            }

        }

        private void findItemViews(@NonNull View itemView) {
            mImageViewMusicItem = itemView.findViewById(R.id.image_view_item_music);
            mTextViewTitle = itemView.findViewById(R.id.text_view_item_music_title);
        }

    }

    private void startMusicActvity() {
        Intent intent = MusicActivity.newIntent(mContext);
        mContext.startActivity(intent);
    }

    private byte[] retrieveCover(int position) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mAllMusicsList.get(position).getData());
        byte[] coverBitmap = retriever.getEmbeddedPicture();
        retriever.release();
        return coverBitmap;
    }
}


