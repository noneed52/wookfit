package com.example.android.bluetoothlegatt;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by kane on 15. 12. 26..
 */
public class ExerciseInfoByDay {
    Context mContext;
    private ExerciseDate exerciseDate;
    ArrayList<ExerciseInfo> excerciseInfo;

    public ExerciseInfoByDay(Context context) {
        mContext = context;
        excerciseInfo = new ArrayList<>();
    }

    public ExerciseDate getExerciseDate() {
        return exerciseDate;
    }

    public void setExerciseDate() {
        Calendar cal = Calendar.getInstance();
        this.exerciseDate = new ExerciseDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    public void setExerciseDate(int year, int month, int day) {
        this.exerciseDate = new ExerciseDate(year, month, day);
    }

    public ArrayList<ExerciseInfo> getExcerciseInfo() {
        return excerciseInfo;
    }

    public void setExcerciseInfo(ArrayList<ExerciseInfo> excerciseInfo) {
        this.excerciseInfo = excerciseInfo;
    }

    public class ExerciseDate {
        int year;
        int month;
        int day;

        public ExerciseDate(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
    }
}
