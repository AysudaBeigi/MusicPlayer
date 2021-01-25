package com.example.musicplayer.utilities;

import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.musicplayer.service.MediaPlayerService;

public class AudioUtils {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public   static AudioAttributes buildAudioAttributes() {

        return new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

    }




}
