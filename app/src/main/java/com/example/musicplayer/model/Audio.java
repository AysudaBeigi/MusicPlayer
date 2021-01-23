package com.example.musicplayer.model;

import java.io.Serializable;

public class Audio implements Serializable {
    private String mData;
    private String mTitle;
    private String mAlbum;
    private String mArtist;

    public Audio(String data, String title, String album, String artist) {
        mData = data;
        mTitle = title;
        mAlbum = album;
        mArtist = artist;
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
}
