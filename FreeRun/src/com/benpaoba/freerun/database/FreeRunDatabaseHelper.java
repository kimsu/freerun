package com.benpaoba.freerun.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FreeRunDatabaseHelper extends SQLiteOpenHelper{
	private final String TAG = "FreeRun";
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "run_records.db";
    
    
    public FreeRunDatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	Log.d(TAG, "FreeRunDatabaseHelper, onCreate()");
        RunRecordTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	Log.d(TAG, "FreeRunDatebaseHelper: onUpdate()");
    	RunRecordTable.onUpgrade(db, oldVersion, newVersion);
    }

}
