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
    public static final String PREF_ALL_MUSICS_LIST = "prefAllMusicsList";
    public static final String PREF_CURRENT_MUSIC_INDEX = "prefMusicIndex";
    private static final String PREF_ALL_CURRENT_MUSICS_LIST = "prefCurrentMusicsList";
    private static final String PREF_CURRENT_ALBUM_NAME = "prefCurrentAlbumName";
    private static final String PREF_CURRENT_ARTIST_NAME = "prefCurrentArtistName";
    public static final String PREF_SHUFFLE_STATE = "prefShuffleState";
    public static final String PREF_REPEAT_STATE = "prefRepeatState";
    public static final String PREF_SERVICE_BOUND = "prefServiceBound";
    public static final String PREF_PLAY_PAUSE_STATE = "prefPlayPauseState";
    public static final String PREF_ACTIVE_MUSIC = "prefActiveMusic";
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
        mSharedPreferences = getSharedPreferences(PREF_ALL_MUSICS_LIST);
        Editor editor = mSharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(musicArrayList);
        editor.putString(PREF_ALL_MUSICS_LIST, json);
        editor.apply();

    }


    public void setCurrentMusicsList(ArrayList<Music> musicArrayList) {
        mSharedPreferences = getSharedPreferences(PREF_ALL_CURRENT_MUSICS_LIST);
        Editor editor = mSharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(musicArrayList);
        editor.putString(PREF_ALL_CURRENT_MUSICS_LIST, json);
        editor.apply();

    }

    public void setCurrentMusicIndex(int currentMusicIndex) {

        getSharedPreferences(PREF_CURRENT_MUSIC_INDEX).edit().
                putInt(PREF_CURRENT_MUSIC_INDEX, currentMusicIndex)
                .apply();

    }

    public void setActiveMusic(Music activeMusic) {
        Gson gson = new Gson();
        String json = gson.toJson(activeMusic);
        getSharedPreferences(PREF_ACTIVE_MUSIC).edit().
                putString(PREF_ACTIVE_MUSIC, json)
                .apply();

    }
    public Music getActiveMusic(){
        Gson gson = new Gson();
        String json = getSharedPreferences(PREF_ACTIVE_MUSIC).
                getString(PREF_ACTIVE_MUSIC, null);
        Type type = new TypeToken<Music>() {
        }.getType();
        return gson.fromJson(json, type);

    }

    public void setCurrentAlbumName(String currentAlbumName) {
        getSharedPreferences(PREF_CURRENT_ALBUM_NAME).edit().
                putString(PREF_CURRENT_ALBUM_NAME, currentAlbumName)
                .apply();

    }


    public void setCurrentArtistName(String currentArtistName) {
        getSharedPreferences(PREF_CURRENT_ARTIST_NAME).edit().
                putString(PREF_CURRENT_ARTIST_NAME, currentArtistName)
                .apply();

    }

    public void setShuffleState(boolean state) {
        mSharedPreferences = getSharedPreferences(PREF_SHUFFLE_STATE);
        mSharedPreferences.edit().putBoolean(PREF_SHUFFLE_STATE, state)
                .apply();

    }

    public boolean getShuffleState() {
        mSharedPreferences = getSharedPreferences(PREF_SHUFFLE_STATE);
        return mSharedPreferences.getBoolean(PREF_SHUFFLE_STATE, false);
    }

    public void setRepeatState(boolean state) {
        mSharedPreferences = getSharedPreferences(PREF_REPEAT_STATE);
        mSharedPreferences.edit().putBoolean(PREF_REPEAT_STATE, state)
                .apply();

    }

    public boolean getRepeatState() {
        mSharedPreferences = getSharedPreferences(PREF_REPEAT_STATE);
        return mSharedPreferences.getBoolean(PREF_REPEAT_STATE, false);
    }

    public void setServiceBound(boolean state) {
        mSharedPreferences = getSharedPreferences(PREF_SERVICE_BOUND);
        mSharedPreferences.edit().putBoolean(PREF_SERVICE_BOUND, state)
                .apply();

    }

    public boolean getServiceBound() {
        mSharedPreferences = getSharedPreferences(PREF_SERVICE_BOUND);
        return mSharedPreferences.getBoolean(PREF_SERVICE_BOUND, false);
    }

    public void setPlayPauseSate(String state) {
        mSharedPreferences = getSharedPreferences(PREF_PLAY_PAUSE_STATE);
        mSharedPreferences.edit().putString(PREF_PLAY_PAUSE_STATE, state)
                .apply();

    }

    public String getPlayPauseSate() {
        mSharedPreferences = getSharedPreferences(PREF_PLAY_PAUSE_STATE);
        return mSharedPreferences.getString(PREF_PLAY_PAUSE_STATE, "play");
    }


    public ArrayList<Music> getAllMusicsList() {
        ArrayList<Music> result = new ArrayList<>();
        mSharedPreferences = getSharedPreferences(PREF_ALL_MUSICS_LIST);
        Gson gson = new Gson();
        String json = mSharedPreferences.getString(PREF_ALL_MUSICS_LIST, null);
        Type type = new TypeToken<ArrayList<Music>>() {

        }.getType();
        result = gson.fromJson(json, type);
        return result;

    }

    public ArrayList<Music> getCurrentMusicsList() {
        ArrayList<Music> result =new ArrayList<Music>() ;
        mSharedPreferences = getSharedPreferences(PREF_ALL_CURRENT_MUSICS_LIST);
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
        mSharedPreferences = getSharedPreferences(PREF_CURRENT_MUSIC_INDEX);
        return mSharedPreferences.getInt(PREF_CURRENT_MUSIC_INDEX, -1);
    }

    public String getCurrentAlbumName() {
        mSharedPreferences = getSharedPreferences(PREF_CURRENT_ALBUM_NAME);
        return mSharedPreferences.getString(PREF_CURRENT_ALBUM_NAME, "");
    }

    public String getCurrentArtistName() {
        mSharedPreferences = getSharedPreferences(PREF_CURRENT_ARTIST_NAME);
        return mSharedPreferences.getString(PREF_CURRENT_ARTIST_NAME, "");
    }

    private SharedPreferences getSharedPreferences(String name) {
        return mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public void clearCashedAllMusicsList() {
        ArrayList<SharedPreferences>
                sharedPreferencesArrayList = new ArrayList<>();
        sharedPreferencesArrayList.add(getSharedPreferences(PREF_CURRENT_MUSIC_INDEX));
        sharedPreferencesArrayList.add(getSharedPreferences(PREF_CURRENT_ALBUM_NAME));
        sharedPreferencesArrayList.add(getSharedPreferences(PREF_CURRENT_ARTIST_NAME));
        sharedPreferencesArrayList.add(getSharedPreferences(PREF_ALL_CURRENT_MUSICS_LIST));
        sharedPreferencesArrayList.add(getSharedPreferences(PREF_SHUFFLE_STATE));
        sharedPreferencesArrayList.add(getSharedPreferences(PREF_REPEAT_STATE));
        sharedPreferencesArrayList.add(getSharedPreferences(PREF_SERVICE_BOUND));
        sharedPreferencesArrayList.add(getSharedPreferences(PREF_PLAY_PAUSE_STATE));
        sharedPreferencesArrayList.add(getSharedPreferences(PREF_ACTIVE_MUSIC));

        for (int i = 0; i < sharedPreferencesArrayList.size(); i++) {

            Editor editor = mSharedPreferences.edit();
            editor.clear();
            editor.commit();
        }

    }

}
