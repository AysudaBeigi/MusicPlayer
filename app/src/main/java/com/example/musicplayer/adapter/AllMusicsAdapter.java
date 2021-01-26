package com.example.musicplayer.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.musicplayer.utilities.MusicUtils;
import com.example.musicplayer.repository.MusicRepository;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AllMusicsAdapter extends Adapter<AllMusicsAdapter.AllMusicsHolder> {
    private Context mContext;
    private ArrayList<Music> mAllMusicsList;
    private MusicRepository mMusicRepository;

    public AllMusicsAdapter(Context context) {
        Log.d(PagerActivity.TAG, " AllMusicsAdapter");

        mContext = context;
        mMusicRepository=MusicRepository.getInstance(mContext);
        mAllMusicsList = mMusicRepository.getAllMusicsList();

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
                    mMusicRepository.setCurrentMusicIndex(mPosition);
                    mMusicRepository.setCurrentMusicsList(mAllMusicsList);
                    startMusicActivity();
                }
            });

        }

        private void bindView(int position) {
            Log.d(PagerActivity.TAG, " bindView");

            mPosition = position;
            mTextViewTitle.setText(mAllMusicsList.get(position).getTitle());
            byte[] coverBitmap = MusicUtils.
                    retrieveCover(mAllMusicsList.get(position).getData());
            if (coverBitmap != null) {
                Glide.with(mContext)
                        .asBitmap()
                        .load(coverBitmap)
                        .into(mImageViewMusicItem);

            }

        }

        private void findItemViews(@NonNull View itemView) {
            mImageViewMusicItem = itemView.findViewById(R.id.image_view_music_item);
            mTextViewTitle = itemView.findViewById(R.id.text_view_title_music_item);
        }

    }

    private void startMusicActivity() {
        Intent intent = MusicActivity.newIntent(mContext);
        mContext.startActivity(intent);
    }


}


