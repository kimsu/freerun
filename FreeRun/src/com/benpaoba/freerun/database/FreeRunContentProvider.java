package com.benpaoba.freerun.database;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class FreeRunContentProvider extends ContentProvider {
	private final String TAG = "RunningMap";
	//database
	private FreeRunDatabaseHelper database;
	
	//used for the UriMather
	private static final int RECORDS = 10;
	private static final int RECORDS_ID = 20;
	
	private static final String AUTHORITY = 
			"com.benpaoba.freerun.contentprovider";
	
	private static final String BASE_PATH = "records";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
	
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE 
			+ "/records";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/item";
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, RECORDS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", RECORDS_ID);
	}
	
	@Override
	public boolean onCreate() {
		Log.i(TAG, "FreeRunContentProvider: onCreate(), get a database");
		database = new FreeRunDatabaseHelper(getContext());
		return false;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, 
			String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "MyTodoContentProvider: query() " + "Uri: " + uri.toString());
		//Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		//check if the caller has requested a column which does not exist
		checkColumns(projection);
		
		//set the table
		queryBuilder.setTables(RunRecordTable.TABLE_RUN_RECORD);
		
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case RECORDS:
			break;
		case RECORDS_ID:
			//adding the ID to the original query
			queryBuilder.appendWhere(RunRecordTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		Log.d(TAG, "MyTodoContentProvider: QueryBuilder query()");
		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		
		//make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}
	
	@Override
	public String getType(Uri uri) {
		return null;
	}
	
	@Override 
	public Uri insert(Uri uri, ContentValues values) {
		
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getReadableDatabase();
		int rowsDeleted = 0;
		int id = 0;
		switch (uriType) {
		case RECORDS:
			id = (int)sqlDB.insert(RunRecordTable.TABLE_RUN_RECORD, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI" + uri);
		}
		Log.d(TAG, "MyTodoContentProvider: insert()" + " Uri: " + uri.toString() + ", id = " + id);
		getContext().getContentResolver().notifyChange(uri, null);
		return ContentUris.withAppendedId(CONTENT_URI, id);
		
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(TAG, "MyTodoContentProvider: delete()" + " Uri: " + uri.toString());
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case RECORDS:
			rowsDeleted = sqlDB.delete(RunRecordTable.TABLE_RUN_RECORD, selection, selectionArgs);
			break;
		case RECORDS_ID:
			String id = uri.getLastPathSegment();
			if(TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(RunRecordTable.TABLE_RUN_RECORD, 
						RunRecordTable.COLUMN_ID + "=" + id,
						null);
			} else {
				rowsDeleted = sqlDB.delete(RunRecordTable.TABLE_RUN_RECORD,
						RunRecordTable.COLUMN_ID + "=" + id + " and " + selection,
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}
	@Override
	public int update(Uri uri, ContentValues values, String selection, 
			String[] selectionArgs) {
		Log.d(TAG, "MyTodoContentProvider: update()" + " Uri: " + uri.toString());
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case RECORDS:
			rowsUpdated = sqlDB.update(RunRecordTable.TABLE_RUN_RECORD,
					values, 
					selection,
					selectionArgs);
			break;
		case RECORDS_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(RunRecordTable.TABLE_RUN_RECORD,
						values, 
						RunRecordTable.COLUMN_ID,
						null);
			} else {
				rowsUpdated = sqlDB.update(RunRecordTable.TABLE_RUN_RECORD,
						values, 
						RunRecordTable.COLUMN_ID + "=" + id + " and " + selection, 
						selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknow URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
	private void checkColumns(String [] projection) {
		String[] available = {RunRecordTable.COLUMN_ID,
				RunRecordTable.COLUMN_DATE,
				RunRecordTable.COLUMN_DISTANCE,
				RunRecordTable.COLUMN_ST,
				RunRecordTable.COLUMN_USEDTIME,
				RunRecordTable.COLUMN_FIVE,
	            RunRecordTable.COLUMN_TEN,
	            RunRecordTable.COLUMN_HALF_MAR,
	            RunRecordTable.COLUMN_FULL_MAR,
	            RunRecordTable.COLUMN_USEDTIME,
	            RunRecordTable.COLUMN_COSTENERGY};
		
		if(projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			
			//check if all columns which are requested are available
			if(!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknow columns in projection");
			}
		}
	}
}
