package com.example.musicplayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MusicPlayerApplication  extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            String channelId=getString(R.string.channel_id);
            String channelName=getString(R.string.channel_name);
            String description=getString(R.string.description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel=new NotificationChannel(
                    channelId,
                    channelName,
                    importance
            );
            notificationChannel.setDescription(description);
            NotificationManager notificationManager=
                    getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(notificationChannel);


        }
    }
}
