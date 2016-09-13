package com.example.android.bluetoothlegatt;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Created by kane on 15. 11. 30..
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {
    Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.right_drawer_layout, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mContext = getActivity();
        final Fragment frag = this;
        getView().findViewById(R.id.closeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(frag).commit();
            }
        });
        getView().findViewById(R.id.nothingBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        ((TextView)getView().findViewById(R.id.connectivity)).setText(DeviceControlActivity.connected);

        getView().findViewById(R.id.yellow).setOnClickListener(this);
        getView().findViewById(R.id.green).setOnClickListener(this);
        getView().findViewById(R.id.grey).setOnClickListener(this);
        getView().findViewById(R.id.blue).setOnClickListener(this);
        getView().findViewById(R.id.black).setOnClickListener(this);
        getView().findViewById(R.id.brown).setOnClickListener(this);
        getView().findViewById(R.id.red).setOnClickListener(this);
        getView().findViewById(R.id.resetBtn).setOnClickListener(this);

        ((TextView)getView().findViewById(R.id.savedTime)).setText(UserInfo.getLastExerciseDate(getActivity()) + "시간 전");
        if(DeviceControlActivity.battery == 0)
            ((TextView)getView().findViewById(R.id.battery)).setText("알 수 없음");
        else
            ((TextView)getView().findViewById(R.id.battery)).setText(DeviceControlActivity.battery + "%");
        loadBandType();
    }

    @Override
    public void onClick(View v) {
        ((RadioButton)getView().findViewById(R.id.yellow)).setChecked(false);
        ((RadioButton)getView().findViewById(R.id.green)).setChecked(false);
        ((RadioButton)getView().findViewById(R.id.grey)).setChecked(false);
        ((RadioButton)getView().findViewById(R.id.blue)).setChecked(false);
        ((RadioButton)getView().findViewById(R.id.black)).setChecked(false);
        ((RadioButton)getView().findViewById(R.id.brown)).setChecked(false);
        ((RadioButton)getView().findViewById(R.id.red)).setChecked(false);
        if(v.getId() == R.id.resetBtn) {
            UserInfo.setBandType(mContext, -1);
            ((TextView)getView().findViewById(R.id.savedTime)).setText("0시간 전");
            UserInfo.resetLastExerciseDate(mContext);
        } else {
            ((RadioButton)getView().findViewById(v.getId())).setChecked(true);
            saveBandType(v.getId());
        }
    }

    private void saveBandType(int id) {
        switch (id) {
            case R.id.yellow:
                UserInfo.setBandType(getActivity(), 1);
                break;
            case R.id.green:
                UserInfo.setBandType(getActivity(), 2);
                break;
            case R.id.grey:
                UserInfo.setBandType(getActivity(), 3);
                break;
            case R.id.blue:
                UserInfo.setBandType(getActivity(), 4);
                break;
            case R.id.black:
                UserInfo.setBandType(getActivity(), 5);
                break;
            case R.id.brown:
                UserInfo.setBandType(getActivity(), 6);
                break;
            case R.id.red:
                UserInfo.setBandType(getActivity(), 7);
                break;
        }
    }

    private void loadBandType() {
        switch (UserInfo.getBandType(getActivity())) {
            case 1:
                ((RadioButton)getView().findViewById(R.id.yellow)).setChecked(true);
                break;
            case 2:
                ((RadioButton)getView().findViewById(R.id.green)).setChecked(true);
                break;
            case 3:
                ((RadioButton)getView().findViewById(R.id.grey)).setChecked(true);
                break;
            case 4:
                ((RadioButton)getView().findViewById(R.id.blue)).setChecked(true);
                break;
            case 5:
                ((RadioButton)getView().findViewById(R.id.black)).setChecked(true);
                break;
            case 6:
                ((RadioButton)getView().findViewById(R.id.brown)).setChecked(true);
                break;
            case 7:
                ((RadioButton)getView().findViewById(R.id.red)).setChecked(true);
                break;
            default:
                break;
        }
    }
}
