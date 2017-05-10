package com.example.santiago.btqubit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.santiago.btqubit.SecondActivity.tableName;

public class IndividualDataFragment extends Fragment{

    private static final String COLUMN_BALLTIMINGS = "_balltimings";
    private static final String COLUMN_FALLPOSITION = "_fallposition";
    public static final String COLUMN_ROTORTIMINGS = "_rotortimings";
    private static final String COLUMN_ID = "_id";

    ListView indidataListView;
    TextView tablenameTextView;
    DataBaseHandler db;
    FragmentActivity activity ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_individualdata, container, false);

        activity = (FragmentActivity) getActivity();
        db = DataBaseHandler.getInstance(activity);

       // Sample tmpSample = new Sample("[0, 498, 965, 1485, 2118, 2718, 3454, 4088, 4789, 5558, 6391, 7409, 8458, 9676, 10927]","[1145,3299,5708,7980,10203,12639]","2");

   //    db.addSample(tableName,tmpSample);




        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        indidataListView = (ListView) view.findViewById(R.id.indidataListView);
        tablenameTextView = (TextView) view.findViewById(R.id.tablename);

        ArrayList<String> ballString_list = db.databaseToArrayList(tableName, COLUMN_BALLTIMINGS);
        ArrayList<String> fallString_list = db.databaseToArrayList(tableName, COLUMN_FALLPOSITION);
        ArrayList<String> rotorString_list = db.databaseToArrayList(tableName, COLUMN_ROTORTIMINGS);
        ArrayList<String> idString_list = db.databaseToArrayList(tableName, COLUMN_ID);

        ArrayList<String> strShow = new ArrayList<>(idString_list);

        for (int i = 0; i < strShow.size(); i++) {
            SampleAnalysis ana = new SampleAnalysis(ballString_list.get(i),rotorString_list.get(i));
            strShow.set(i,
                    "ball times : " + ballString_list.get(i) + "\n" +
                    "rotor times : " + rotorString_list.get(i) + "\n" +
                    "fall pos : " + fallString_list.get(i) + "\n" +
                    "rotor numbers: " + ana.getNumbers() );
        }

        tablenameTextView.setText(tableName);

        ArrayAdapter adapter = new ArrayAdapter<String>(activity,
                R.layout.indi_listitem, strShow);
        indidataListView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
    }





}
