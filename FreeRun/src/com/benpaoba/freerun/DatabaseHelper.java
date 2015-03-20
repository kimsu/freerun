package com.benpaoba.freerun;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "distance_info.db";

    public DatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS milestone(" +
        		"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        		"distance INTEGER," +
        		"longitude DOUBLE, " +
        		"latitude DOUBLE," +
        		"usedTime INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table milestone");
        db.execSQL("CREATE TABLE IF NOT EXISTS milestone(" +
        		"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        		"distance INTEGER," +
        		"longitude FLOAT, " +
        		"latitude FLOAT" +
        		"usedTime INTEGER)");
    }

}
