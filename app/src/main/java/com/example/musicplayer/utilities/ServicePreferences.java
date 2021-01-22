package com.example.musicplayer.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class ServicePreferences {
    private static final String PREF_SERVICE_BOUND = "serviceBound";

    public static boolean getServiceBound(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        return sharedPreferences.getBoolean(PREF_SERVICE_BOUND,false);
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
