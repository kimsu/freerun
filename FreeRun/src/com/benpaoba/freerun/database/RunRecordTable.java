package com.benpaoba.freerun.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class RunRecordTable {
		private static final String TAG = "FreeRun";
	
	    public static final String TABLE_RUN_RECORD = "run_records";
	    public static final String COLUMN_ID = "_id";
	    public static final String COLUMN_DATE= "date";
	    public static final String COLUMN_ST = "start_time";
	    public static final String COLUMN_DISTANCE = "distance";
	    public static final String COLUMN_USEDTIME = "usedTime";
	    public static final String COLUMN_FIVE ="fiveKmTime";
	    public static final String COLUMN_TEN = "tenKmTime";
	    public static final String COLUMN_HALF_MAR = "halfMarathonTime";
	    public static final String COLUMN_FULL_MAR = "fullMarathonTime";
	    public static final String COLUMN_COSTENERGY ="cost_energy";

	    
	    private static final String DATABASE_CREATE = "create table if not exists " + TABLE_RUN_RECORD
	    		+ "  ( " + COLUMN_ID + " INTEGER primary key autoincrement, " 
	    		+ COLUMN_DATE + " LONG, " 
	    		+ COLUMN_ST + " LONG, " 
	    		+ COLUMN_DISTANCE + " DOUBLE, " 
	    		+ COLUMN_USEDTIME + " LONG, " 
	    		+ COLUMN_FIVE + " INTEGER, " 
	    		+ COLUMN_TEN + " LONG, " 
	    		+ COLUMN_HALF_MAR + " LONG, " 
	    		+ COLUMN_FULL_MAR + " LONG );";
	    

	    
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