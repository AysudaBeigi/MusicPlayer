package com.example.musicplayer.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.model.Music;
import com.example.musicplayer.repository.MusicRepository;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;


public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumsHolder> {
    private Context mContext;
    private MusicRepository mMusicRepository;
    private ArrayList<Music> mAllMusicsArrayList;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public AlbumsAdapter(Context context) {

        mContext=context;
        mMusicRepository= MusicRepository.getInstance(mContext);
        mAllMusicsArrayList=mMusicRepository.loadAllMusicsList();
    }

    @NonNull
    @Override
    public AlbumsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

       View view= LayoutInflater.from(mContext).
               inflate(R.layout.album_item,parent,false);
       return  new AlbumsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumsHolder holder, int position) {

        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return 0;
    }
    public class AlbumsHolder extends RecyclerView.ViewHolder{

        private ShapeableImageView mImageViewCover;
        private MaterialTextView mTextViewAlbum;
        public AlbumsHolder(@NonNull View itemView) {
            super(itemView);
            findItemViews(itemView);

        }

        private void findItemViews(@NonNull View itemView) {
            mImageViewCover=itemView.findViewById(R.id.image_view_cover_album_item);
            mTextViewAlbum=itemView.findViewById(R.id.text_view_album_item);
        }

        public void bindView(int position){
            mTextViewAlbum.setText(mAllMusicsArrayList.get(position).getAlbum());

        }
    }
}
