package com.example.musicplayer.repository;

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
public class MusicRepository {

    public static final String PREF_STORAGE = "om.example.musicplayer.sharedPreferencesStorage";
    public static final String PREF_ALL_MUSICS_LIST = "prefAllMusicsList";
    public static final String PREF_MUSIC_INDEX = "prefMusicIndex";
    private static final String PREF_ALL_CURRENT_MUSICS_LIST = "prefCurrentMusicsList";
    private SharedPreferences mSharedPreferences;
    private Context mContext;
    private static MusicRepository sInstance;
    private ArrayList<Music> mAllMusicsArrayList;
    private boolean mIsAllMusicsStored = false;

    private MusicRepository(Context context) {
        mContext = context;
    }

    public static MusicRepository getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MusicRepository(context);
            return sInstance;
        }
        return sInstance;
    }

    public void storeAllMusicsList(ArrayList<Music> musicArrayList) {
       /* mSharedPreferences = getSharedPreferences();
        Editor editor = mSharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(musicArrayList);
        editor.putString(PREF_ALL_MUSICS_LIST, json);
        editor.apply();*/
        if(mIsAllMusicsStored)
            return;
            mAllMusicsArrayList = musicArrayList;
            mIsAllMusicsStored = true;

    }

    public void storeCurrentMusicsList(ArrayList<Music> musicArrayList) {
        mSharedPreferences = getSharedPreferences();
        Editor editor = mSharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(musicArrayList);
        editor.putString(PREF_ALL_CURRENT_MUSICS_LIST, json);
        editor.apply();

    }

    public ArrayList<Music> loadAllMusicsList() {
       /* ArrayList<Music> result = new ArrayList<>();
        mSharedPreferences = getSharedPreferences();
        Gson gson = new Gson();
        String json = mSharedPreferences.getString(PREF_ALL_MUSICS_LIST, null);
        Type type = new TypeToken<ArrayList<Music>>() {

        }.getType();
        result = gson.fromJson(json, type);
        return result;*/
        return mAllMusicsArrayList;
    }

    public ArrayList<Music> loadCurrentMusicsList() {
        ArrayList<Music> result = new ArrayList<>();
        mSharedPreferences = getSharedPreferences();
        Gson gson = new Gson();
        String json = mSharedPreferences.getString(PREF_ALL_CURRENT_MUSICS_LIST,
                null);
        Type type = new TypeToken<ArrayList<Music>>() {

        }.getType();
        result = gson.fromJson(json, type);
        return result;
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
