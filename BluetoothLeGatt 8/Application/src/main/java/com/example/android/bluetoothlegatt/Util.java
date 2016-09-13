package com.example.android.bluetoothlegatt;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

/**
 * Created by kane on 9/11/16.
 */
public class Util {

    public static String getPhoneNumber(Context context) {
        TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if(tMgr != null) {
            String phoneNumber = tMgr.getLine1Number();
            if (phoneNumber == null)
                return Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            else
                return phoneNumber;
        }
        else {
            return Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }
    }
}
