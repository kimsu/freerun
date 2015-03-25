package com.benpaoba.freerun.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class RunRecordTable {
		private static final String TAG = "FreeRun";
	
	    public static final String TABLE_RUN_RECORD = "run_records";
	    public static final String COLUMN_ID = "_id";
	    public static final String COLUMN_DATA= "date";
	    public static final String COLUMN_ST = "start_time";
	    public static final String COLUMN_DISTANCE = "distance";
	    public static final String COLUMN_USEDTIME = "usedTime";
	    public static final String COLUMN_COSTENERGY ="cost_energy";

	    
	    private static final String DATABASE_CREATE = "create table if not exists " + TABLE_RUN_RECORD
	    		+ "(" + COLUMN_ID + " integer primary key autoincrement, " 
	    		+ COLUMN_DATA + " text not null," 
	    		+ COLUMN_ST + " text not null," 
	    		+ COLUMN_DISTANCE + " text not null," 
	    		+ COLUMN_USEDTIME + " text not null);";
	    

	    
	    public static void onCreate(SQLiteDatabase db) {
	    	Log.d(TAG, "RunRecordTable: onCreate()");
	        db.execSQL(DATABASE_CREATE);
	    }

	  
	    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    	Log.d(TAG, RunRecordTable.class.getSimpleName()+": " + 
					"Upgrading database from version " + oldVersion + "to " + newVersion
					+", which will destroy all old data");
	        db.execSQL("drop table " + TABLE_RUN_RECORD);
	        onCreate(db);
	    }

	

}
