package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class UserInfoDatabase extends SQLiteOpenHelper implements BaseColumns {
    com.example.myapplication.MainActivity mainActivity;
    String TAG = "DATABASE";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MaitrySmita.db";
    public static final String TABLE_NAME = "User_Info";
    public static final String COL_1 = "heart_rate";
    public static final String COL_2 = "respiratory_rate";
    public static final String COL_3 = "feeling_tired";
    public static final String COL_4 = "shortness_of_breath";
    public static final String COL_5 = "cough";
    public static final String COL_6 = "loss_of_smell_or_taste";
    public static final String COL_7 = "muscle_ache";
    public static final String COL_8 = "fever";
    public static final String COL_9 = "nausea";
    public static final String COL_10 = "headache";
    public static final String COL_11 = "diarrhea";
    public static final String COL_12 = "soar_throat";

    public String heartRateValue = "0";
    public String respiratorytRateValue = "0";


    public UserInfoDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase sqliteDB) {
        if (sqliteDB.getPath()!=null) {
            String query = "select DISTINCT tbl_name from sqlite_master where tbl_name = '"+TABLE_NAME+"'";
            try (Cursor cursor = sqliteDB.rawQuery(query, null)) {
                if(cursor!=null) {
                    if(!(cursor.getCount()>0)) {
                        sqliteDB.execSQL("CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY, " + COL_1 + " TEXT, " + COL_2 + " TEXT, "
                                + COL_3 + " INT, " + COL_4 + " INT, " + COL_5 + " INT, " + COL_6 + " INT, "
                                + COL_7 + " INT, " + COL_8 + " INT, " + COL_9 + " INT, " + COL_10 + " INT, "
                                + COL_11 + " INT, " + COL_12 + " INT)");
                    }
                }
            }
        }



    }

    public void setRespiratoryRateValue(String respiratorytRateValue){
        this.respiratorytRateValue = respiratorytRateValue;
    }

    public void setHeartRateValue(String heartRateValue){
        this.heartRateValue = heartRateValue;
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean insertIntoDB(int[] ratingList) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for (int i = 0; i < ratingList.length; i++) {
            Log.i(TAG, "insertData: " + ratingList[i]);
        }
//        Log.i(TAG, "rating list inside insertData: "+ratingList.toString());
        values.put(COL_1, respiratorytRateValue);
        values.put(COL_2, heartRateValue);
        values.put(COL_3, ratingList[0]);
        values.put(COL_4, ratingList[1]);
        values.put(COL_5, ratingList[2]);
        values.put(COL_6, ratingList[3]);
        values.put(COL_7, ratingList[4]);
        values.put(COL_8, ratingList[5]);
        values.put(COL_9, ratingList[6]);
        values.put(COL_10, ratingList[7]);
        values.put(COL_11, ratingList[8]);
        values.put(COL_12, ratingList[9]);
        Log.i("TAG", values.toString());
        long result = db.insert(TABLE_NAME, null, values);
        if (result == -1)
            return false;
        else
            return true;
    }


}