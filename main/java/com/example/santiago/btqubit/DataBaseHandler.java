package com.example.santiago.btqubit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DataBaseHandler extends SQLiteOpenHelper {


    private static DataBaseHandler sInstance = null;

    public static final String COLUMN_BALLTIMINGS = "_balltimings";
    public static final String COLUMN_FALLPOSITION = "_fallposition";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ROTORTIMINGS = "_rotortimings";
    public static final String DATABASE_NAME = "QubitProjects.db";
    public static final int DATABASE_VERSION = 1;


    public static synchronized DataBaseHandler getInstance(Context context) {

        if(sInstance == null){
            sInstance = new DataBaseHandler(context.getApplicationContext());
        }

        return sInstance;
    }

    private DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void addSample(String table_name, Sample sample) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROTORTIMINGS, sample.get_rotortimings());
        values.put(COLUMN_BALLTIMINGS, sample.get_balltimings());
        values.put(COLUMN_FALLPOSITION, sample.get_fallposition());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.insert(table_name, null, values);
        sqLiteDatabase.close();
    }

    public void createTable(String TABLE_NAME) {
        getWritableDatabase().execSQL("CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_BALLTIMINGS + " TEXT," + COLUMN_ROTORTIMINGS + " TEXT," + COLUMN_FALLPOSITION + " TEXT" + ");");
    }

    public void deleteTable(String TABLE_NAME) {
        getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public ArrayList databaseToArrayList(String TABLE_NAME, String _columnName) {
        ArrayList<String> arrColumnList = new ArrayList();
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE 1", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getString(cursor.getColumnIndex(_columnName)) != null) {
                arrColumnList.add(cursor.getString(cursor.getColumnIndex(_columnName)));
            }
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return arrColumnList;
    }

    public String databaseToString(String TABLE_NAME, String _columnName) {
        String dbString = BuildConfig.FLAVOR;
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE 1", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getString(cursor.getColumnIndex(_columnName)) != null) {
                dbString = (dbString + cursor.getString(cursor.getColumnIndex(_columnName))) + "\n";
            }
            cursor.moveToNext();
        }
        cursor.close();
        sqLiteDatabase.close();
        return dbString;
    }

    public void deleteSample(String TABLE_NAME, int id) {
        getWritableDatabase().execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + "=\"" + id + "\"");
    }



    public ArrayList<String> listTables() {
        ArrayList<String> arrTblNames = new ArrayList();
        Cursor c = getWritableDatabase().rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String table_name = c.getString(c.getColumnIndex("name"));
                if (!(table_name.equals("android_metadata") || table_name.equals("sqlite_sequence"))) {
                    arrTblNames.add(table_name);
                }
                c.moveToNext();
            }
        }
        return arrTblNames;
    }


}
