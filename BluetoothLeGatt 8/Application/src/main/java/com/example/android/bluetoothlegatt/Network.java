package com.example.android.bluetoothlegatt;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

/**
 * Created by kane on 9/11/16.
 */
public class Network {

    public static void sendPostExerciseData(Context context, String[] data) {
        String telNo = Util.getPhoneNumber(context);
        if(telNo == null)
            telNo = data[11];
        Ion.with(context)
                .load(Const.insertExerciseDataUrl)
                .setBodyParameter("USER_TEL_NO", telNo)
                .setBodyParameter("TIME", data[0])
                .setBodyParameter("ACC_X", data[1])
                .setBodyParameter("ACC_Y", data[2])
                .setBodyParameter("ACC_Z", data[3])
                .setBodyParameter("MAG_X", data[4])
                .setBodyParameter("MAG_Y", data[5])
                .setBodyParameter("MAG_Z", data[6])
                .setBodyParameter("EXE_TYPE", data[7])
                .setBodyParameter("ALGORITHM_COUNT", data[8])
                .setBodyParameter("RSSI", data[9])
                .setBodyParameter("BATTERY", data[10])
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error

                    }
                });
    }

    public static void sendPostUserData(Context context) {
        String telNo = Util.getPhoneNumber(context);

        String userName = SharedPreferenceInfo.getData(context, "userName");
        String userAge = SharedPreferenceInfo.getData(context, "userAge");
        String userHeight = SharedPreferenceInfo.getData(context, "userHeight");
        String userWeight = SharedPreferenceInfo.getData(context, "userWeight");
        String userSentinel = SharedPreferenceInfo.getData(context, "userSentinel");
        String userTarget = SharedPreferenceInfo.getData(context, "userTarget");
        String userBandType = SharedPreferenceInfo.getData(context, "bandType");
        Ion.with(context)
                .load(Const.insertUserDataUrl)
                .setBodyParameter("USER_TEL_NO", telNo)
                .setBodyParameter("userName", userName)
                .setBodyParameter("userAge", userAge)
                .setBodyParameter("userHeight", userHeight)
                .setBodyParameter("userWeight", userWeight)
                .setBodyParameter("userSentinel", userSentinel)
                .setBodyParameter("userTarget", userTarget)
                .setBodyParameter("userBandType", userBandType)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error

                    }
                });
    }
}
