package com.example.santiago.btqubit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;

import java.util.ArrayList;
import java.util.List;

import static android.R.color.holo_blue_dark;
import static android.R.color.holo_green_dark;
import static com.example.santiago.btqubit.SecondActivity.tableName;

public class GeneralStatsFragment extends Fragment {

    TextView distTextView;
    EditText cutEditText;
    Button updateCutButton;
    RadarChart radChart;
    LineChart ballTimesChart,rotorTimesChart;

    DataAnalysis Dana;
    private FragmentActivity activity2 ;
    DataPreProcessing Dpp = new DataPreProcessing();
    static int cutTiming = 900;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_genstats, container, false);

        activity2 = (FragmentActivity) getActivity();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        distTextView = (TextView) view.findViewById(R.id.distribution);
        cutEditText = (EditText) view.findViewById(R.id.cutEditText);
        updateCutButton = (Button) view.findViewById(R.id.updateCutButton);
        radChart = (RadarChart) view.findViewById(R.id.radHist);
        ballTimesChart = (LineChart) view.findViewById(R.id.ballTimesChart);
        rotorTimesChart = (LineChart) view.findViewById(R.id.rotorTimesChart);

        cutEditText.setText(Integer.toString(cutTiming));
        restoreState();

        updateCutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    if(cutEditText.getText() != null){
                        cutTiming = Integer.parseInt(cutEditText.getText().toString());
                        restoreState();
                    }
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

        Legend legend = ballTimesChart.getLegend();
        legend.setEnabled(false);

        Description d = ballTimesChart.getDescription();
        d.setText("BALL TIMES");

        Legend legend1 = radChart.getLegend();
        legend1.setEnabled(false);

        Description d1 = radChart.getDescription();
        d1.setText("LUCKY NUMBERS");

        Legend legend2 = rotorTimesChart.getLegend();
        legend2.setEnabled(false);

        Description d2 = rotorTimesChart.getDescription();
        d2.setText("ROTOR TIMES");



        Dana = new DataAnalysis(activity2,tableName,cutTiming);
        DataPreProcessing Dpp = new DataPreProcessing();
        int[] hist37 = Dana.getHist37();
        int[] luckys = Dana.getLuckyNumbers();
        float hist37mean = Dana.getHist37Mean();
        ArrayList<String> arrBallSamplesList = Dana.getArrBallSamplesList();
        ArrayList<String> arrRotorSamplesList = Dana.getArrRotorSamplesList();


        distTextView.setText(Dpp.intArray2String(hist37) + "\n" + Dpp.intArray2String(luckys));






////////// BAll times chart
        LineData ballLineData = new LineData();

        for (int i = 0; i < Dana.getNumberDataPoints(); i++) {
            int[] ballArrayInt = Dpp.string2array(arrBallSamplesList.get(i));
            List<Entry> ballTimesEntries = new ArrayList<Entry>();
            for (int j = 0; j < ballArrayInt.length ; j++) {
                ballTimesEntries.add(new Entry(j,ballArrayInt[j]-ballArrayInt[0]));
            }
            LineDataSet dataSetBallTimesLin = new LineDataSet(ballTimesEntries,"Ball Times");
            dataSetBallTimesLin.setColor(getResources().getColor(holo_green_dark));
            dataSetBallTimesLin.setCircleColor(getResources().getColor(holo_blue_dark));

            ballLineData.addDataSet(dataSetBallTimesLin);
        }

        ballTimesChart.setData(ballLineData);
        ballTimesChart.invalidate();


        ////////// Rotor times chart
        LineData rotorLineData = new LineData();

        for (int i = 0; i < Dana.getNumberDataPoints(); i++) {
            int[] rotorArrayInt = Dpp.string2array(arrRotorSamplesList.get(i));
            List<Entry> rotorTimesEntries = new ArrayList<Entry>();
            for (int j = 0; j < rotorArrayInt.length ; j++) {
                rotorTimesEntries.add(new Entry(j,rotorArrayInt[j]-rotorArrayInt[0]));
            }
            LineDataSet dataSetRotorTimesLin = new LineDataSet(rotorTimesEntries,"Rotor Times");
            dataSetRotorTimesLin.setColor(getResources().getColor(holo_green_dark));
            dataSetRotorTimesLin.setCircleColor(getResources().getColor(holo_blue_dark));

            rotorLineData.addDataSet(dataSetRotorTimesLin);
        }

        rotorTimesChart.setData(rotorLineData);
        rotorTimesChart.invalidate();


/// radar Chart
        List<RadarEntry> radEntries = new ArrayList<RadarEntry>();
        for(int i = 0; i < 37 ; i++){
            radEntries.add(new RadarEntry(hist37[i],i));
        }

        RadarDataSet dataSetRad = new RadarDataSet(radEntries, "Label2");

        dataSetRad.setDrawFilled(true);

        List<RadarEntry> radMean = new ArrayList<RadarEntry>();

        for(int i = 0; i < 37 ; i++){
            radMean.add(new RadarEntry(hist37mean,i));
        }

        RadarDataSet dataSetMean = new RadarDataSet(radMean, "Mean");

        dataSetMean.setDrawFilled(true);
        dataSetMean.setColor(getResources().getColor(holo_green_dark));
        dataSetMean.setDrawValues(false);

        RadarData radData = new RadarData();
        radData.addDataSet(dataSetRad);
        radData.addDataSet(dataSetMean);

        radChart.setRotationEnabled(false);
        radChart.setData(radData);
        radChart.invalidate();

    }

}
