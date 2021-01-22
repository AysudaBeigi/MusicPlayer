package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.example.musicplayer.service.MediaPlayerService;
import com.example.musicplayer.utilities.ServicePreferences;

import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {
    private MediaPlayerService mMediaPlayerService;
    private boolean mServiceBound =
            ServicePreferences.getServiceBound(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder =
                    (MediaPlayerService.LocalBinder) service;
            mMediaPlayerService = binder.getService();
            mServiceBound = true;
            ServicePreferences.setServiceBound(MainActivity.this,mServiceBound);

            Toast.makeText(MainActivity.this,
                    "Service bound", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            mServiceBound = false;
            ServicePreferences.setServiceBound(MainActivity.this,mServiceBound);
        }
    };

    private void playAudio(String mediaFile) {
        if (!mServiceBound) {
            Intent playerIntent = MediaPlayerService.newIntent(this, mediaFile);
            startService(playerIntent);
            bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {

            //todo:Send media with BroadcastReceiver
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mServiceBound){
            unbindService(mServiceConnection);
            mMediaPlayerService.stopSelf();
        }
    }
}