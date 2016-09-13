package com.example.android.bluetoothlegatt;

import java.util.ArrayList;

/**
 * Created by kane on 15. 12. 26..
 */
public class ExerciseInfo {
    private int bandType;
    private int exerciseNo;
    private ArrayList<TrialInfo> trialInfo = new ArrayList<>();

    public int getBandType() {
        return bandType;
    }

    public void setBandType(int bandType) {
        this.bandType = bandType;
    }

    public int getExerciseNo() {
        return exerciseNo;
    }

    public void setExerciseNo(int exerciseNo) {
        this.exerciseNo = exerciseNo;
    }

    public ArrayList<TrialInfo> getTrialInfo() {
        return trialInfo;
    }

    public void setTrialInfo(ArrayList<TrialInfo> trialInfo) {
        this.trialInfo = trialInfo;
    }
}