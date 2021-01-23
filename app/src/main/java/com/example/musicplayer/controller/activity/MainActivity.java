package com.example.musicplayer.controller.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.musicplayer.R;
import com.example.musicplayer.model.Audio;
import com.example.musicplayer.service.MediaPlayerService;

import java.util.ArrayList;

import android.provider.MediaStore.Audio.Media;

@RequiresApi(api = Build.VERSION_CODES.O)

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MusicPlayerMainActivity";
    public static final String BUNDLE_SERVICE_STATE = "ServiceState";
    private static final int REQUEST_CODE = 1;
    private MediaPlayerService mMediaPlayerService;
    private boolean mServiceBound = false;

    private ArrayList<Audio> mAudioArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isPermissionGranted()) {
            loadAudio();
            Log.d(TAG, " first audio path :" + mAudioArrayList.get(0).getData());
            playAudio(mAudioArrayList.get(0).getData());

        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(BUNDLE_SERVICE_STATE, mServiceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mServiceBound = savedInstanceState.getBoolean(BUNDLE_SERVICE_STATE);
    }

    private boolean isPermissionGranted() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
            return false;

        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAudio();
                Log.d(TAG, " first audio path :" + mAudioArrayList.get(0).getData());
                playAudio(mAudioArrayList.get(0).getData());

            } else {
                requestPermission();
            }
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.MEDIA_CONTENT_CONTROL},
                REQUEST_CODE);
    }

    private void loadAudio() {
        Log.d(TAG, "loadAudio :");

        ContentResolver contentResolver = getContentResolver();
        Uri uri = Media.EXTERNAL_CONTENT_URI;
        String selection = Media.IS_MUSIC + "!= 0";
        String sortOrder = Media.TITLE + " ASC";
        Cursor cursor =
                contentResolver.query(uri,
                        null,
                        selection,
                        null,
                        sortOrder);
        try {
            if (cursor != null && cursor.getCount() > 0) {
                mAudioArrayList = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String data = cursor.getString(cursor.getColumnIndex(Media.DATA));
                    String title = cursor.getString(cursor.getColumnIndex(Media.TITLE));
                    String album = cursor.getString(cursor.getColumnIndex(Media.ALBUM));
                    String artist = cursor.getString(cursor.getColumnIndex(Media.ARTIST));

                    mAudioArrayList.add(new Audio(data, title, album, artist));
                }
            }
        } finally {
            cursor.close();
        }

    }

    private void playAudio(String mediaFile) {
        Log.d(TAG, "playAudio + service bound:" + mServiceBound);

        if (!mServiceBound) {
            Log.d(TAG, "playAudio");
            Intent playerIntent = MediaPlayerService.newIntent(this, mediaFile);
            startService(playerIntent);
            bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {

            //todo:Send media with BroadcastReceiver
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, " onServiceConnected");

            MediaPlayerService.LocalBinder binder =
                    (MediaPlayerService.LocalBinder) service;
            mMediaPlayerService = binder.getService();
            mServiceBound = true;

            Toast.makeText(MainActivity.this,
                    "Service bound", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            mServiceBound = false;
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mMediaPlayerService.stopSelf();
        }
    }
}
