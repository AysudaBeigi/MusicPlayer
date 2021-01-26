package com.example.musicplayer.controller.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

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
import com.example.musicplayer.controller.fragment.AlbumsFragment;
import com.example.musicplayer.controller.fragment.AllMusicsFragment;
import com.example.musicplayer.controller.fragment.ArtistsFragment;
import com.example.musicplayer.model.Music;
import com.example.musicplayer.service.MusicPlayerService;
import com.example.musicplayer.utilities.StorageUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

import android.provider.MediaStore.Audio.Media;

@RequiresApi(api = Build.VERSION_CODES.O)

public class PagerActivity extends AppCompatActivity {
    public static final String TAG = "MusicPlayerMainActivity";
    public static final String BUNDLE_SERVICE_STATE = "ServiceState";
    public static final String ACTION_PLAY_NEW_AUDIO =
            "com.example.musicplayer.ACTION_PLAY_NEW_AUDIO";
    private static final int REQUEST_CODE = 1;
    private MusicPlayerService mMusicPlayerService;
    private boolean mServiceBound = false;
    private ArrayList<Music> mMusicArrayList;
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;
    private PageAdapter mPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity : onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);
        if (isPermissionGranted()) {
            loadMusic();
            Log.d(TAG, " first audio path :" + mMusicArrayList.get(0).getData());
           // playMusic(1);

        }
        findViews();
        initView();
    }

    private void findViews() {
        mTabLayout = findViewById(R.id.tab_layout_music_player);
        mViewPager = findViewById(R.id.view_pager2_music_player);
    }

    private void initView() {
        mPageAdapter = new PageAdapter(PagerActivity.this);
        mViewPager.setAdapter(mPageAdapter);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator
                (mTabLayout, mViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0: {
                                tab.setText(R.string.tab_musics);
                                break;
                            }
                            case 1: {
                                tab.setText(R.string.tab_albums);
                                break;
                            }
                            case 2: {
                                tab.setText(R.string.tab_artists);
                                break;
                            }
                        }
                    }
                });

        tabLayoutMediator.attach();

    }

    private class PageAdapter extends FragmentStateAdapter {

        public PageAdapter(@NonNull FragmentActivity fragmentActivity)
        {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return AllMusicsFragment.newInstance();
                case 1:
                    return AlbumsFragment.newInstance();
                case 2:
                    return ArtistsFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getItemCount() {
            return 3;
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
                loadMusic();
                Log.d(TAG, " first audio path :" + mMusicArrayList.get(0).getData());
               // playMusic(4);

            } else {
                requestPermission();
            }
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                PagerActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.MEDIA_CONTENT_CONTROL},
                REQUEST_CODE);
    }

    private void loadMusic() {
        Log.d(TAG, "loadMusic :");

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
                mMusicArrayList = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String data = cursor.getString(cursor.getColumnIndex(Media.DATA));
                    String title = cursor.getString(cursor.getColumnIndex(Media.TITLE));
                    String album = cursor.getString(cursor.getColumnIndex(Media.ALBUM));
                    String artist = cursor.getString(cursor.getColumnIndex(Media.ARTIST));
                    String duration = cursor.getString(cursor.getColumnIndex(Media.DURATION));

                    mMusicArrayList.add(new Music(data, title, album, artist, duration));
                }
            }
        } finally {
            cursor.close();
        }
        StorageUtils storageUtils = new StorageUtils(getApplicationContext());

        storageUtils.storeMusicIndex(0);
        storageUtils.storeMusicsList(mMusicArrayList);


    }

    private void playMusic(int musicIndex) {
        Log.d(TAG, "playMusic");

        if (!mServiceBound) {
            Log.d(TAG, "playAudio + !service bound:" );
             Intent playerIntent = MusicPlayerService.newIntent(this);
            startService(playerIntent);
            bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            Log.d(TAG, "playAudio + service bound:" );

           // storageUtils.storeMusicIndex(musicIndex);
            Intent broadcastIntent = new Intent(ACTION_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);

        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, " onServiceConnected");

            MusicPlayerService.LocalBinder binder =
                    (MusicPlayerService.LocalBinder) service;
            mMusicPlayerService = binder.getService();
            mServiceBound = true;

            Toast.makeText(PagerActivity.this,
                    "Service bound", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            mServiceBound = false;
        }
    };


    @Override
    protected void onDestroy() {
        Log.d(PagerActivity.TAG,"onDestroy");

        super.onDestroy();
        if (mServiceBound) {
            Log.d(PagerActivity.TAG,"unbindService");

            unbindService(mServiceConnection);
            mMusicPlayerService.stopSelf();
        }
    }
}
