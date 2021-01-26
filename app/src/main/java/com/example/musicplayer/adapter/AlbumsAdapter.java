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
import com.example.musicplayer.controller.activity.AlbumDetailActivity;
import com.example.musicplayer.model.Music;
import com.example.musicplayer.repository.MusicRepository;
import com.example.musicplayer.utilities.MusicUtils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.HashMap;


public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumsHolder> {
    private Context mContext;
    private MusicRepository mMusicRepository;
    private HashMap<String, ArrayList<Music>> mAlbumsHashMap;
    private ArrayList<String> mAlbumsName;

    @RequiresApi(api = Build.VERSION_CODES.O)

    public AlbumsAdapter(Context context) {
        mContext = context;
        mMusicRepository = MusicRepository.getInstance(mContext);
        mAlbumsHashMap = mMusicRepository.getAlbums();
        mAlbumsName = mMusicRepository.getUnDuplicateAlbumsNameList();
    }

    @NonNull
    @Override
    public AlbumsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).
                inflate(R.layout.album_item, parent, false);
        return new AlbumsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumsHolder holder, int position) {

        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return mAlbumsName.size();
    }

    public class AlbumsHolder extends RecyclerView.ViewHolder {

        private ShapeableImageView mImageViewCover;
        private MaterialTextView mTextViewAlbum;
        private String mCurrentAlbumName;
        private ArrayList<Music> mCurrentMusicArrayList = new ArrayList<>();

        public AlbumsHolder(@NonNull View itemView) {
            super(itemView);
            findItemViews(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {

                    mMusicRepository.setCurrentAlbumName(mCurrentAlbumName);
                    mMusicRepository.setCurrentMusicsList(mCurrentMusicArrayList);
                    startAlbumDetailActivity();

                }

            });

        }

        private void findItemViews(@NonNull View itemView) {
            mImageViewCover = itemView.findViewById(R.id.image_view_cover_album_item);
            mTextViewAlbum = itemView.findViewById(R.id.text_view_album_item);
        }

        public void bindView(int position) {
            mCurrentAlbumName = mAlbumsName.get(position);
            mCurrentMusicArrayList = (mAlbumsHashMap.get(mCurrentAlbumName));
            mTextViewAlbum.setText(mAlbumsName.get(position));
            byte[] coverBitmap = MusicUtils.
                    retrieveCover(mCurrentMusicArrayList.get(0).getData());
            if (coverBitmap != null)
                MusicUtils.setCover(mContext, coverBitmap, mImageViewCover);

        }
    }

    private void startAlbumDetailActivity() {
        Intent intent = AlbumDetailActivity.newIntent(mContext);
        mContext.startActivity(intent);
    }
}
