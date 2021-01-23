package com.example.musicplayer.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.musicplayer.controller.activity.MainActivity;

public class ServicePreferences {
    private static final String PREF_SERVICE_BOUND = "serviceBound";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean getServiceBound(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        boolean serviceBound=
                sharedPreferences.getBoolean(PREF_SERVICE_BOUND,false);
        Log.d(MainActivity.TAG,"service bound " +serviceBound );

        return serviceBound;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(
                context.getPackageName(),
                Context.MODE_PRIVATE);
    }

    public static void setServiceBound(Context context,boolean serviceBound){
        SharedPreferences sharedPreferences=getSharedPreferences(context);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(PREF_SERVICE_BOUND,serviceBound);
        editor.apply();

    }


}
