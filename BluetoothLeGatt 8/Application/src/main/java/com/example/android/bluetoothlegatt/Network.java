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

        Ion.with(context)
                .load(Const.insertDataUrl)
                .setBodyParameter("USER_TEL_NO", Util.getPhoneNumber(context))
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
}
