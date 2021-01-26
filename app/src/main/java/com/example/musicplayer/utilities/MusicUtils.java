package com.example.musicplayer.utilities;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.google.android.material.imageview.ShapeableImageView;

public class MusicUtils {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public   static AudioAttributes buildAudioAttributes() {

        return new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

    }
    public static byte[] retrieveCover(String data) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(data);
        byte[] coverBitmap = retriever.getEmbeddedPicture();
        retriever.release();
        return coverBitmap;
    }
    public static void setCover(Context context, byte[] bitmap, ShapeableImageView imageView){
        Glide.with(context).
                asBitmap().
                load(bitmap).
                into(imageView);
    }

}
