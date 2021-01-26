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
import java.util.HashMap;

import android.content.SharedPreferences.Editor;

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
        ArrayList<String> unDuplicateAlbumNameList =
                getUnDuplicateAlbumsNameList();
        for (int i = 0; i < unDuplicateAlbumNameList.size(); i++) {
            ArrayList<Music> albumMusics = new ArrayList<>();
            for (int j = 0; j < musicArrayList.size(); j++) {
                if (musicArrayList.get(j).getAlbum().
                        equals(unDuplicateAlbumNameList.get(i))) {
                    albumMusics.add(musicArrayList.get(j));
                }
            }
            albumHashMap.put(unDuplicateAlbumNameList.get(i), albumMusics);
        }
        return albumHashMap;
    }

    public ArrayList<String> getUnDuplicateAlbumsNameList() {
        ArrayList<Music> musicArrayList = getAllMusicsList();

        ArrayList<String> albumList = new ArrayList<>();
        for (int i = 0; i < musicArrayList.size(); i++) {
            albumList.add(musicArrayList.get(i).getAlbum());
        }
        ArrayList<String> albumListUnDuplicate = new ArrayList<>();
        for (int i = 0; i < albumList.size(); i++) {
            if (!albumListUnDuplicate.contains(albumList.get(i))) {
                albumListUnDuplicate.add(albumList.get(i));
            }
        }
        return albumListUnDuplicate;
    }

     public HashMap<String, ArrayList<Music>> getArtists() {
        ArrayList<Music> musicArrayList = getAllMusicsList();
        HashMap<String, ArrayList<Music>> artistHashMap = new HashMap<>();
        ArrayList<String> unDuplicateArtistNameList =
                getUnDuplicateArtistsNameList();
        for (int i = 0; i < unDuplicateArtistNameList.size(); i++) {
            ArrayList<Music> artistMusics = new ArrayList<>();
            for (int j = 0; j < musicArrayList.size(); j++) {
                if (musicArrayList.get(j).getArtist().
                        equals(unDuplicateArtistNameList.get(i))) {
                    artistMusics.add(musicArrayList.get(j));
                }
            }
            artistHashMap.put(unDuplicateArtistNameList.get(i), artistMusics);
        }
        return artistHashMap;
    }

    public ArrayList<String> getUnDuplicateArtistsNameList() {
        ArrayList<Music> musicArrayList = getAllMusicsList();

        ArrayList<String> artistList = new ArrayList<>();
        for (int i = 0; i < musicArrayList.size(); i++) {
            artistList.add(musicArrayList.get(i).getArtist());
        }
        ArrayList<String> artistListUnDuplicate = new ArrayList<>();
        for (int i = 0; i < artistList.size(); i++) {
            if (!artistListUnDuplicate.contains(artistList.get(i))) {
                artistListUnDuplicate.add(artistList.get(i));
            }
        }
        return artistListUnDuplicate;
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
