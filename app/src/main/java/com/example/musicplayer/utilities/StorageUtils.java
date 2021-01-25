package com.example.musicplayer.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.musicplayer.model.Music;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import android.content.SharedPreferences.Editor;

@RequiresApi(api = Build.VERSION_CODES.O)
public class StorageUtils {
    public static final String PREF_STORAGE = "om.example.musicplayer.sharedPreferencesStorage";
    public static final String PREF_ALL_MUSICS_LIST = "prefAllMusicsList";
    public static final String PREF_MUSIC_INDEX = "prefMusicIndex";
    private SharedPreferences mSharedPreferences;
    private  Context mContext;

    public StorageUtils(Context context) {
        mContext = context;
    }

    public void storeAllMusicsList(ArrayList<Music> musicArrayList) {
        mSharedPreferences = getSharedPreferences();
        Editor editor = mSharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(musicArrayList);
        editor.putString(PREF_ALL_MUSICS_LIST, json);
        editor.apply();
    }

    public ArrayList<Music> loadAllMusicsList() {
        mSharedPreferences = getSharedPreferences();
        Gson gson = new Gson();
        String json = mSharedPreferences.getString(PREF_ALL_MUSICS_LIST, null);
        Type type = new TypeToken<ArrayList<Music>>() {

        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeMusicIndex(int audioIndex) {
        getSharedPreferences().edit().
                putInt(PREF_MUSIC_INDEX, audioIndex)
                .apply();

    }


    public int loadMusicIndex() {
        mSharedPreferences = getSharedPreferences();
        return mSharedPreferences.getInt(PREF_MUSIC_INDEX, -1);
    }

    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(PREF_STORAGE, Context.MODE_PRIVATE);
    }

    public void clearCashedAllMusicsList() {
        mSharedPreferences = getSharedPreferences();
        Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

}
