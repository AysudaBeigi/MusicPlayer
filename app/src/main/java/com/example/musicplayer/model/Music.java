package com.example.musicplayer.model;

import com.example.musicplayer.service.MusicPlayerService;

import java.io.Serializable;

public class Music implements Serializable {
    private String mData;
    private String mTitle;
    private String mAlbum;
    private String mArtist;
    private String mDuration;

    public Music(String data, String title, String album, String artist, String duration) {
        mData = data;
        mTitle = title;
        mAlbum = album;
        mArtist = artist;
        mDuration=duration;
    }

    public String getData() {
        return mData;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setData(String data) {
        mData = data;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setAlbum(String album) {
        mAlbum = album;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public void setDuration(String duration) {
        mDuration = duration;
    }
}
