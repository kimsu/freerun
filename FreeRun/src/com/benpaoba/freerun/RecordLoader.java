package com.benpaoba.freerun;

import com.benpaoba.freerun.database.FreeRunContentProvider;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

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
		RecordResult result = new RecordResult();
	    ContentResolver resolver = getContext().getContentResolver();
	    Cursor cursor = null;
	    if(mode == 1) {
	    	cursor = resolver.query(FreeRunContentProvider.CONTENT_URI, null, null, null, null);
	    }else{
	        cursor = resolver.query(FreeRunContentProvider.CONTENT_URI, null, null, null, getQuerySortOrder(mode));
	    }
	     
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
            case 1:
                return ContactsContract.Contacts.DISPLAY_NAME + " ASC";
            case 2:
                return ContactsContract.Contacts.DISPLAY_NAME + " DESC";
            default:
                return null;
        }
    }
}

class RecordResult{
    Cursor cursor;
    Exception exception;
}
