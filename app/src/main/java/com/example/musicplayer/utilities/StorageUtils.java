package com.example.musicplayer.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.musicplayer.model.Audio;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import android.content.SharedPreferences.Editor;

@RequiresApi(api = Build.VERSION_CODES.O)
public class StorageUtils {
    public static final String PREF_STORAGE = "om.example.musicplayer.sharedPreferencesStorage";
    public static final String PREF_AUDIO_ARRAY_LIST = "prefAudioArrayList";
    public static final String PREF_AUDIO_INDEX = "prefAudioIndex";
    private SharedPreferences mSharedPreferences;
    private Context mContext;

    public StorageUtils(Context context) {
        mContext = context;
    }

    public void storeAudios(ArrayList<Audio> audioArrayList) {
        mSharedPreferences = getSharedPreferences();
        Editor editor = mSharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(audioArrayList);
        editor.putString(PREF_AUDIO_ARRAY_LIST, json);
        editor.apply();
    }

    public ArrayList<Audio> loadAudios() {
        mSharedPreferences = getSharedPreferences();
        Gson gson = new Gson();
        String json = mSharedPreferences.getString(PREF_AUDIO_ARRAY_LIST, null);
        Type type = new TypeToken<ArrayList<Audio>>() {

        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeAudioIndex(int audioIndex) {
        getSharedPreferences().edit().
                putInt(PREF_AUDIO_INDEX, audioIndex)
                .apply();

    }


    public int loadAudioIndex() {
        mSharedPreferences = getSharedPreferences();
        return mSharedPreferences.getInt(PREF_AUDIO_INDEX, -1);
    }

    private SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(PREF_STORAGE, Context.MODE_PRIVATE);
    }

    public void clearCashedAudioPlayList() {
        mSharedPreferences = getSharedPreferences();
        Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

}
