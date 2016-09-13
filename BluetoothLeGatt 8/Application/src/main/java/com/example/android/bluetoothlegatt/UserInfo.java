package com.example.android.bluetoothlegatt;

import android.content.Context;

import java.util.Calendar;

/**
 * Created by kane on 15. 12. 26..
 */
public class UserInfo {

    public static int getBandType(Context context) {
        return Integer.parseInt(SharedPreferenceInfo.getData(context, "bandType"));
    }

    public static void setBandType(Context context, int bandType) {
        SharedPreferenceInfo.saveData(context, "bandType", String.valueOf(bandType));
    }

    public static String getLastExerciseDate(Context context) {
        String time = SharedPreferenceInfo.getData(context, "lastDate");
        Calendar cal = Calendar.getInstance();
        long diff = cal.getTimeInMillis() - Long.parseLong(time);
        diff = diff/3600000;
        if(time.equals("0"))
            return time;
        else
            return String.valueOf(diff);
    }

    public static void setLastExerciseDate(Context context) {
        Calendar cal = Calendar.getInstance();
        long time = cal.getTimeInMillis();
        SharedPreferenceInfo.saveData(context, "lastDate", String.valueOf(time));
    }

    public static void resetLastExerciseDate(Context context) {
        SharedPreferenceInfo.saveData(context, "lastDate", String.valueOf(0));
    }
}
