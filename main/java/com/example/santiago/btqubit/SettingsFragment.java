package com.example.santiago.btqubit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import static android.R.color.darker_gray;
import static android.R.color.holo_green_dark;

public class SettingsFragment extends Fragment{

    Button ShandsButton;
    static Boolean handsfree = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        ShandsButton = (Button) view.findViewById(R.id.ShandsButton);

        restoreState();

        ShandsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                View view = getView();
                if (null != view) {

                    if (handsfree) {
                        ShandsButton.setTextColor(getResources().getColor(darker_gray));

                    } else {
                        ShandsButton.setTextColor(getResources().getColor(holo_green_dark));
                    }

                    handsfree = !handsfree;

                }
            }
        });



    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    void restoreState(){
        if (handsfree) {
            ShandsButton.setTextColor(getResources().getColor(holo_green_dark));
        } else {
            ShandsButton.setTextColor(getResources().getColor(darker_gray));
        }


    }


}
