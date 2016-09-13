package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kane on 15. 11. 3..
 */
public class ExerciseListAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<Exercise> exerciseList;
    private final Handler handler;
    private final DeviceControlActivity activity;
    private static boolean exerciseStarted = false;
    public ExerciseListAdapter(Context context, List<Exercise> list, Handler callbackHandler, DeviceControlActivity parentActivity) {
        mContext = context;
        exerciseList = list;
        handler = callbackHandler;
        activity = parentActivity;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getGroupCount() {
        return exerciseList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return exerciseList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return exerciseList.get(0);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.exercise_item, parent, false);
        int trialAmount = 0;
        ArrayList<ExerciseInfoByDay> info = DeviceControlActivity.info;
        if(info != null && info.size() > 0) {
            ExerciseInfoByDay dayInfo = info.get(info.size()-1);
            if(dayInfo != null) {
                for(int i = 0; i < dayInfo.getExcerciseInfo().size(); i++) {
                    ExerciseInfo exerciseInfo = dayInfo.getExcerciseInfo().get(i);
                    if(exerciseInfo.getExerciseNo() == groupPosition) {
                        trialAmount = exerciseInfo.getTrialInfo().size();
                        break;
                    }
                }
            }
        }

        if(isExpanded) {
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.selected_shadow));
            ((TextView)convertView.findViewById(R.id.exercise_name_ko)).setTextColor(Color.WHITE);
            ((TextView)convertView.findViewById(R.id.exercise_name_en)).setTextColor(Color.WHITE);
            ((TextView)convertView.findViewById(R.id.exercise_num)).setTextColor(Color.WHITE);
            if(trialAmount > 2)
                ((ImageView)convertView.findViewById(R.id.threeTimes)).setImageResource(R.drawable.three_orange);
            else if(trialAmount > 1)
                ((ImageView)convertView.findViewById(R.id.threeTimes)).setImageResource(R.drawable.two_orange_one_white);
            else if(trialAmount > 0)
                ((ImageView)convertView.findViewById(R.id.threeTimes)).setImageResource(R.drawable.one_orange_two_white);
            else
                ((ImageView)convertView.findViewById(R.id.threeTimes)).setImageResource(R.drawable.three_times_white);
        }
        else {
            convertView.setBackground(mContext.getResources().getDrawable(R.drawable.card_exercise_list));
            ((TextView)convertView.findViewById(R.id.exercise_name_ko)).setTextColor(Color.rgb(129, 129, 129));
            ((TextView)convertView.findViewById(R.id.exercise_name_en)).setTextColor(Color.rgb(129, 129, 129));
            ((TextView)convertView.findViewById(R.id.exercise_num)).setTextColor(Color.rgb(129, 129, 129));
            if(trialAmount > 2)
                ((ImageView)convertView.findViewById(R.id.threeTimes)).setImageResource(R.drawable.three_orange);
            else if(trialAmount > 1)
                ((ImageView)convertView.findViewById(R.id.threeTimes)).setImageResource(R.drawable.two_orange_one_grey);
            else if(trialAmount > 0)
                ((ImageView)convertView.findViewById(R.id.threeTimes)).setImageResource(R.drawable.one_orange_two_grey);
            else
                ((ImageView)convertView.findViewById(R.id.threeTimes)).setImageResource(R.drawable.three_times_grey);
        }
        ((TextView) convertView.findViewById(R.id.exercise_num)).setText(new Integer(groupPosition+1).toString());
        ((TextView) convertView.findViewById(R.id.exercise_num))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "Roboto-Light.ttf"));
        ((TextView) convertView.findViewById(R.id.exercise_name_ko)).setText(exerciseList.get(groupPosition).getNameKo());
        ((TextView) convertView.findViewById(R.id.exercise_name_ko))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Medium.otf"));
        ((TextView) convertView.findViewById(R.id.exercise_name_en)).setText(exerciseList.get(groupPosition).getNameEn());
        ((TextView) convertView.findViewById(R.id.exercise_name_en))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "Roboto-Regular.ttf"));

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.exercise_content, parent, false);
        final View childView = convertView;
        ((AnimatedGifImageView)childView.findViewById(R.id.gif_view)).setAnimatedGif(getImageResource(groupPosition), AnimatedGifImageView.TYPE.FIT_CENTER);
        childView.findViewById(R.id.gatt_service_start_btn).setVisibility(View.GONE);
        ((TextView)childView.findViewById(R.id.dataCount))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "Roboto-ThinItalic.ttf"));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((TextView) childView.findViewById(R.id.dataCount)).setText(String.format("0"));
                childView.findViewById(R.id.dataCount).setVisibility(View.VISIBLE);
                childView.findViewById(R.id.gif_view).setVisibility(View.GONE);
                childView.findViewById(R.id.gatt_service_start_btn).setVisibility(View.VISIBLE);
            }
        }, 2000);

        convertView.findViewById(R.id.exercise_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExerciseInfoFragment exerciseFrag = new ExerciseInfoFragment();
                FragmentManager fm = ((Activity) mContext).getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                Bundle exerciseNum = new Bundle();
                exerciseNum.putInt("exerciseNum", groupPosition);
                exerciseFrag.setArguments(exerciseNum);
                fragmentTransaction.add(android.R.id.content, exerciseFrag).commit();
            }
        });
        convertView.findViewById(R.id.gatt_service_start_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exerciseStarted) {
                    ((ImageButton) v).setImageResource(R.drawable.exercise_start_selector);
                    exerciseStarted = false;
                    Message msg = new Message();
                    msg.obj = new Integer(-1);
                    handler.sendMessage(msg);
                } else {
                    ((ImageButton) v).setImageResource(R.drawable.exercise_finish_selector);
                    exerciseStarted = true;
                    ((TextView) childView.findViewById(R.id.dataCount)).setText("0");
                    Message msg = new Message();
                    msg.obj = new Integer(groupPosition);
                    handler.sendMessage(msg);
                }
            }
        });
        DeviceControlActivity.dataHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                ((TextView) childView.findViewById(R.id.dataCount)).setText(String.valueOf(((Integer) msg.obj).intValue()));
                return true;
            }
        });

        if(exerciseStarted)
            ((ImageButton) convertView.findViewById(R.id.gatt_service_start_btn)).setImageResource(R.drawable.exercise_finish_selector);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    private int getImageResource(int position) {
        switch (position) {
            case 0:
                return R.drawable.exercise_gif_1;
            case 1:
                return R.drawable.exercise_gif_2;
            case 2:
                return R.drawable.exercise_gif_3;
            case 3:
                return R.drawable.exercise_gif_4;
            case 4:
                return R.drawable.exercise_gif_5;
            case 5:
                return R.drawable.exercise_gif_6;
            default:
                return R.drawable.exercise_gif_1;
        }
    }
}
