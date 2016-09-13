package com.example.android.bluetoothlegatt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by kane on 15. 12. 26..
 */
public class SharedPreferenceInfo {

    public static void saveData(Context context, String key, String value) {
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getData(Context context, String key) {
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        return mPref.getString(key, "0");
    }

    public static void saveExerciseData(Context context, ArrayList<ExerciseInfoByDay> exerciseData) {
        JSONArray jsonDataArray = new JSONArray();
        for(int i = 0; i < exerciseData.size(); i++) {
            try {
                JSONObject infoByDayObject = new JSONObject();
                ExerciseInfoByDay infoByDay = exerciseData.get(i);

                JSONObject dateObject = new JSONObject();
                dateObject.put("month", infoByDay.getExerciseDate().month);
                dateObject.put("day", infoByDay.getExerciseDate().day);
                dateObject.put("year", infoByDay.getExerciseDate().year);
                infoByDayObject.put("date", dateObject);

                JSONArray exerciseArray = new JSONArray();
                for(int j = 0; j < infoByDay.getExcerciseInfo().size(); j++) {
                    ExerciseInfo exercise = infoByDay.getExcerciseInfo().get(j);
                    JSONObject exerciseObject = new JSONObject();
                    exerciseObject.put("exerciseNo", exercise.getExerciseNo());
                    exerciseObject.put("bandType", exercise.getBandType());
                    JSONArray trialArray = new JSONArray();
                    for(int k = 0; k < exercise.getTrialInfo().size(); k++) {
                        JSONObject trialInfoObject = new JSONObject();
                        TrialInfo trialInfo = exercise.getTrialInfo().get(k);
                        trialInfoObject.put("count", trialInfo.getExerciseCount());
                        trialArray.put(trialInfoObject);
                    }
                    exerciseObject.put("trial", trialArray);
                    exerciseArray.put(exerciseObject);
                }
                infoByDayObject.put("exercise", exerciseArray);

                jsonDataArray.put(infoByDayObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString("data", jsonDataArray.toString());
        editor.commit();
    }

    public static ArrayList<ExerciseInfoByDay> loadExerciseData(Context context) {
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        String data = mPref.getString("data", "0");
        if(data.equals("0"))
            return new ArrayList<>();
        else {
            ArrayList<ExerciseInfoByDay> exerciseInfos = new ArrayList<>();
            String filename = "data_file.txt";
            try {
                File myFile = new File(Environment
                        .getExternalStorageDirectory(), filename);
                if (!myFile.exists())
                    myFile.createNewFile();
                FileOutputStream fos;
                byte[] dataBytes = data.getBytes();
                fos = new FileOutputStream(myFile);
                fos.write(dataBytes);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                JSONArray jsonDataArray = new JSONArray(data);
                for(int i = 0; i < jsonDataArray.length(); i++) {
                    JSONObject infoByDayObject = new JSONObject(jsonDataArray.get(i).toString());
                    ExerciseInfoByDay infoByDay = new ExerciseInfoByDay(context);
                    infoByDay.setExerciseDate(infoByDayObject.getJSONObject("date").getInt("year"),
                            infoByDayObject.getJSONObject("date").getInt("month"),
                            infoByDayObject.getJSONObject("date").getInt("day"));

                    for(int j = 0; j < infoByDayObject.getJSONArray("exercise").length(); j++) {
                        JSONObject exerciseObject = infoByDayObject.getJSONArray("exercise").getJSONObject(j);
                        ExerciseInfo exercise = new ExerciseInfo();
                        exercise.setBandType(exerciseObject.getInt("bandType"));
                        exercise.setExerciseNo(exerciseObject.getInt("exerciseNo"));

                        JSONArray trialArray = exerciseObject.getJSONArray("trial");
                        for(int k = 0; k < trialArray.length(); k++) {
                            TrialInfo trialInfo = new TrialInfo();
                            trialInfo.setExerciseCount(trialArray.getJSONObject(k).getInt("count"));
                            exercise.getTrialInfo().add(trialInfo);
                        }
                        infoByDay.getExcerciseInfo().add(exercise);
                    }
                    exerciseInfos.add(infoByDay);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return exerciseInfos;
        }
    }
}
