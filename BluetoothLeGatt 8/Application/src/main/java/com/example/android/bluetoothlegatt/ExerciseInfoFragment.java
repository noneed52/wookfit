package com.example.android.bluetoothlegatt;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Created by kane on 15. 11. 30..
 */
public class ExerciseInfoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.exercise_info, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        ((ImageView)getView().findViewById(R.id.exercise_explain)).setImageResource(getImageSourceId());
        ImageButton closeBtn = (ImageButton)getView().findViewById(R.id.info_close_btn);
        final Fragment frag = this;
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction().remove(frag).commit();
            }
        });
    }

    private int getImageSourceId() {
        int id = 0;
        switch (getArguments().getInt("exerciseNum", 0)) {
            case 0:
                id = R.drawable.popup_card_1;
                break;
            case 1:
                id = R.drawable.popup_card_2;
                break;
            case 2:
                id = R.drawable.popup_card_3;
                break;
            case 3:
                id = R.drawable.popup_card_4;
                break;
            case 4:
                id = R.drawable.popup_card_5;
                break;
            case 5:
                id = R.drawable.popup_card_6;
                break;
            default:
                id = R.drawable.popup_card_1;
                break;
        }
        return id;
    }
}
