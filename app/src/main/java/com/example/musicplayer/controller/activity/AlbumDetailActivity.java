package com.example.musicplayer.controller.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.musicplayer.R;
import com.example.musicplayer.controller.fragment.AlbumDetailFragment;

public class AlbumDetailActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, AlbumDetailActivity.class);
        return intent;
    }
    @Override
    public Fragment getFragment() {
        return AlbumDetailFragment.newInstance();
    }
}