package com.example.musicplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.controller.activity.ArtistDetailActivity;
import com.example.musicplayer.model.Music;
import com.example.musicplayer.repository.MusicRepository;
import com.example.musicplayer.utilities.MusicUtils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ArtistsHolder> {
    private Context mContext;

    private MusicRepository mMusicRepository;
    private HashMap<String, ArrayList<Music>> mArtistsHashMap;
    private ArrayList<String> mArtistsName;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArtistsAdapter(Context context) {
        mContext = context;
        mMusicRepository=MusicRepository.getInstance(mContext);
        mArtistsHashMap=mMusicRepository.getArtists();
        mArtistsName =mMusicRepository.getUnDuplicateArtistsNameList();
    }

    @NonNull
    @Override
    public ArtistsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).
                inflate(R.layout.artist_item, parent, false);
        return new ArtistsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistsHolder holder, int position) {
        holder.bindView(position);

    }

    @Override
    public int getItemCount() {
        return mArtistsName.size();
    }

    public class ArtistsHolder extends RecyclerView.ViewHolder{
        private ShapeableImageView mImageViewCover;
        private MaterialTextView mTextViewArtist;
        private String mCurrentArtistName;
        private ArrayList<Music> mCurrentMusicArrayList = new ArrayList<>();

        public ArtistsHolder(@NonNull View itemView) {
            super(itemView);
            findItemViews(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    mMusicRepository.setCurrentArtistName(mCurrentArtistName);
                    mMusicRepository.setCurrentMusicsList(mCurrentMusicArrayList);

                    startArtistDetailActivity();

                }
            });
        }

        private void findItemViews(@NonNull View itemView) {
            mImageViewCover=itemView.findViewById(R.id.image_view_cover_artist_item);
            mTextViewArtist=itemView.findViewById(R.id.text_view_artist_item);
        }

        private void bindView(int position){
            mCurrentArtistName=mArtistsName.get(position);
            mCurrentMusicArrayList=mArtistsHashMap.get(mCurrentArtistName);
            mTextViewArtist.setText(mCurrentArtistName);
            byte[] coverBitmap=MusicUtils.
                    retrieveCover(mCurrentMusicArrayList.get(0).getData());
            MusicUtils.setCover(mContext,coverBitmap,mImageViewCover);
        }
    }

    private void startArtistDetailActivity() {
        Intent intent= ArtistDetailActivity.newIntent(mContext);
        mContext.startActivity(intent);
    }
}
