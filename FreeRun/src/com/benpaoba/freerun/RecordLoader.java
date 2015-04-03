package com.benpaoba.freerun;

import com.benpaoba.freerun.database.FreeRunContentProvider;
import com.benpaoba.freerun.database.RunRecordTable;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class RecordLoader extends AsyncTaskLoader<RecordResult>{
	private RecordResult mResult;
	private int mode;
	public RecordLoader(Context context,int sortmode) {
		super(context);
		mode = sortmode;
	}

	@Override
    protected void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
        }
        if (takeContentChanged() || mResult == null) {
            forceLoad();
        }
    }
	
	@Override
	public RecordResult loadInBackground() {
		// TODO Auto-generated method stub
		Log.d("yxf","RecordLoader, loadInBackground(), mode = "+ mode);
		RecordResult result = new RecordResult();
	    ContentResolver resolver = getContext().getContentResolver();
	    Cursor cursor = null;
	    cursor = resolver.query(FreeRunContentProvider.CONTENT_URI, null, null, null, getQuerySortOrder(mode));
	     
		result.cursor = cursor;
		return result;
	}
	
	@Override
    public void deliverResult(RecordResult result) {
        if (isReset()) {
            return;
        }
        RecordResult oldResult = mResult;
        mResult = result;

        if (isStarted()) {
            super.deliverResult(result);
        }
        oldResult = null;
    }
	
	@Override
	protected void onStopLoading() {
	    cancelLoad();
	}
	
	@Override
    protected void onReset() {
        super.onReset();
        // Ensure the loader is stopped
        onStopLoading();

        mResult = null;
    }
	
	public static String getQuerySortOrder(int sortOrder) {
        switch (sortOrder) {
            case RunHistoryRecord.SORT_BY_USEDTIME:
                return RunRecordTable.COLUMN_USEDTIME + " DESC";
            case RunHistoryRecord.SORT_BY_DISTANCE:
                return RunRecordTable.COLUMN_DISTANCE + " DESC";
            default:
                return RunRecordTable.COLUMN_DATE + " DESC";
        }
    }
}

class RecordResult{
    Cursor cursor;
    Exception exception;
}
