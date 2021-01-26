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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import android.content.SharedPreferences.Editor;
import android.widget.ArrayAdapter;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MusicRepository {

    public static final String PREF_STORAGE = "om.example.musicplayer.sharedPreferencesStorage";
    public static final String PREF_ALL_MUSICS_LIST = "prefAllMusicsList";
    public static final String PREF_CURRENT_MUSIC_INDEX = "prefMusicIndex";
    private static final String PREF_ALL_CURRENT_MUSICS_LIST = "prefCurrentMusicsList";
    private static final String PREF_CURRENT_ALBUM_NAME = "prefCurrentAlbumName";
    private static final String PREF_CURRENT_ARTIST_NAME = "prefCurrentArtistName";
    private SharedPreferences mSharedPreferences;
    private Context mContext;
    private static MusicRepository sInstance;

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

    public void setAllMusicsList(ArrayList<Music> musicArrayList) {
        mSharedPreferences = getSharedPreferences();
        Editor editor = mSharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(musicArrayList);
        editor.putString(PREF_ALL_MUSICS_LIST, json);
        editor.apply();

    }


    public void setCurrentMusicsList(ArrayList<Music> musicArrayList) {
        mSharedPreferences = getSharedPreferences();
        Editor editor = mSharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(musicArrayList);
        editor.putString(PREF_ALL_CURRENT_MUSICS_LIST, json);
        editor.apply();

    }

    public void setCurrentMusicIndex(int currentMusicIndex) {
        getSharedPreferences().edit().
                putInt(PREF_CURRENT_MUSIC_INDEX, currentMusicIndex)
                .apply();

    }

    public void setCurrentAlbumName(String currentAlbumName) {
        getSharedPreferences().edit().
                putString(PREF_CURRENT_ALBUM_NAME, currentAlbumName)
                .apply();

    }


    public void setCurrentArtistName(String currentArtistName) {
        getSharedPreferences().edit().
                putString(PREF_CURRENT_ARTIST_NAME, currentArtistName)
                .apply();

    }

    public ArrayList<Music> getAllMusicsList() {
        ArrayList<Music> result = new ArrayList<>();
        mSharedPreferences = getSharedPreferences();
        Gson gson = new Gson();
        String json = mSharedPreferences.getString(PREF_ALL_MUSICS_LIST, null);
        Type type = new TypeToken<ArrayList<Music>>() {

        }.getType();
        result = gson.fromJson(json, type);
        return result;

    }

    public ArrayList<Music> getCurrentMusicsList() {
        ArrayList<Music> result = new ArrayList();
        mSharedPreferences = getSharedPreferences();
        Gson gson = new Gson();
        String json = mSharedPreferences.getString(PREF_ALL_CURRENT_MUSICS_LIST,
                null);
        Type type = new TypeToken<ArrayList<Music>>() {

        }.getType();
        result = gson.fromJson(json, type);
        return result;

    }

    public HashMap<String, ArrayList<Music>> getAlbums() {
        ArrayList<Music> musicArrayList = getAllMusicsList();
        HashMap<String, ArrayList<Music>> albumHashMap = new HashMap<>();
        ArrayList<String> unDoplecateAlbumNameList =
                getUnDoplecateAlbumNameList(musicArrayList);
        for (int i = 0; i < unDoplecateAlbumNameList.size(); i++) {
            ArrayList<Music> albumMusics = new ArrayList<>();
            for (int j = 0; j < musicArrayList.size(); j++) {
                if (musicArrayList.get(j).getAlbum().
                        equals(unDoplecateAlbumNameList.get(i))) {
                    albumMusics.add(musicArrayList.get(j));
                }

            }
            albumHashMap.put(unDoplecateAlbumNameList.get(i), albumMusics);

        }


        return albumHashMap;
    }

    private ArrayList<String> getUnDoplecateAlbumNameList(ArrayList<Music> musicArrayList) {
        ArrayList<String> albumList = new ArrayList<>();
        for (int i = 0; i < musicArrayList.size(); i++) {
            albumList.add(musicArrayList.get(i).getAlbum());
        }
        ArrayList<String> albumListUnDoplecate = new ArrayList<>();
        for (int i = 0; i < albumList.size(); i++) {
            if (!albumListUnDoplecate.contains(albumList.get(i))) {
                albumListUnDoplecate.add(albumList.get(i));
            }
        }
        return albumListUnDoplecate;
    }

    public ArrayList<String> getAlbumNames() {
        ArrayList<Music> musicArrayList = getAllMusicsList();
        ArrayList<String> albumNames = new ArrayList<>();

        for (int i = 0; i < musicArrayList.size() - 1; i++) {
            for (int j = 1; j < musicArrayList.size(); j++) {
                if (!musicArrayList.get(i).getAlbum()
                        .equals(musicArrayList.get(j).getAlbum())) {
                    albumNames.add(musicArrayList.get(i).getAlbum());
                }
            }
        }

        return albumNames;
    }

    public HashMap<String, Music> getArtists() {
        ArrayList<Music> musicArrayList = getAllMusicsList();
        HashMap<String, Music> artistHashMap = new HashMap<>();

        for (int i = 0; i < musicArrayList.size(); i++) {
            for (int j = 0; j < musicArrayList.size(); j++) {
                if (!musicArrayList.get(i).getArtist()
                        .equals(musicArrayList.get(j).getArtist())) {
                    artistHashMap.put(musicArrayList.get(i).getArtist()
                            , musicArrayList.get(i));
                }
            }
        }
        return artistHashMap;
    }

    public int getCurrentMusicIndex() {
        mSharedPreferences = getSharedPreferences();
        return mSharedPreferences.getInt(PREF_CURRENT_MUSIC_INDEX, -1);
    }

    public String getCurrentAlbumName() {
        mSharedPreferences = getSharedPreferences();
        return mSharedPreferences.getString(PREF_CURRENT_ALBUM_NAME, "");
    }

    public String getCurrentArtistName() {
        mSharedPreferences = getSharedPreferences();
        return mSharedPreferences.getString(PREF_CURRENT_ARTIST_NAME, "");
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
