package com.example.musicplayer.utilities;

public class SeekbarUtils {
    public static String getProgressPlayedTimeFormat(int mCurrentPosition) {
        int second = mCurrentPosition % 60;
        int minute = mCurrentPosition / 60;
        return getTimeFormat(minute) + ":" + getTimeFormat(second);
    }

    private static String getTimeFormat(int time) {
        if (time < 10)
            return "0" + time;
        return time + "";
    }
}