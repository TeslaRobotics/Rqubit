package com.example.santiago.btqubit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static String EXTRA_TABLENAME;
    DataBaseHandler db;
    EditText mainEditText;
    ListView casinosListView;
    ArrayList<String> tablesList;
    ArrayAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainEditText = (EditText) findViewById(R.id.mainEditText);

        db = DataBaseHandler.getInstance(this);


        tablesList = db.listTables();
        /*for (int i = 0; i < tempString_list.size(); i++) {
            ItemInMyList item = new ItemInMyList();
            item.image = BitmapFactory.decodeResource(getResources(), this.imgIDs[i % this.imgIDs.length]);
            item.title = (String) tempString_list.get(i);
            this.itemList.add(item);
        }
        this.mProjectsAdapter = new MyAdapter(this, this.itemList, C0165R.layout.main_list_item);*/

        adapter = new ArrayAdapter<String>(this,
                R.layout.main_listitem, tablesList);

        casinosListView = (ListView) findViewById(R.id.casinosListView);
        casinosListView.setAdapter(adapter);

        casinosListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                String tableName = ((TextView) arg1.findViewById(R.id.main_listitem)).getText().toString();
                Intent i = new Intent(MainActivity.this, SecondActivity.class);
                i.putExtra(MainActivity.EXTRA_TABLENAME, tableName);
                startActivity(i);
            }
        });

    }

    public void addClick(View view){
        String tablename = mainEditText.getText().toString();
        if(!tablesList.contains(tablename) && !tablename.isEmpty()){
            tablesList.add(tablename);
            adapter.notifyDataSetChanged();
            db.createTable(tablename);
        }
        else Toast.makeText(this, "ya existe o no se puede", Toast.LENGTH_SHORT).show();


    }

    public void delClick(View view){
        String tablename = mainEditText.getText().toString();

        if(tablesList.contains(tablename)){
            tablesList.remove(tablename);
            adapter.notifyDataSetChanged();
            db.deleteTable(tablename);
        }
        else Toast.makeText(this, "no existe", Toast.LENGTH_SHORT).show();

    }

}


