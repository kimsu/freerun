package com.benpaoba.freerun;

import java.text.SimpleDateFormat;

import com.benpaoba.freerun.database.RunRecordTable;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class RunHistoryRecord extends Activity {
	
	public static final int SORT_BY_USEDTIME = 1;
	public static final int SORT_BY_DISTANCE = 2;
	public static final int SORT_BY_DEFAULT = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_record);
        FragmentManager fm = getFragmentManager();
        // Create the list fragment and add it as our sole content.
        RecordListFragment fragment = new RecordListFragment();
        fm.beginTransaction().replace(R.id.container_roots, fragment).commit();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
    	getMenuInflater().inflate(R.menu.activity_menu, menu);
    	return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final int id = item.getItemId();
		if(id == android.R.id.home) {
			finish();
			return true;
		}else if(id == R.id.menu_sort_by_usedtime){
			RecordListFragment appListFragment = (RecordListFragment)getFragmentManager().findFragmentById(R.id.container_roots);
			appListFragment.onSortModeChanged(SORT_BY_USEDTIME);
			return true;
		}else if(id == R.id.menu_sort_by_distance){
			RecordListFragment appListFragment = (RecordListFragment)getFragmentManager().findFragmentById(R.id.container_roots);
			appListFragment.onSortModeChanged(SORT_BY_DISTANCE);
			return true;
		}else if(id == R.id.menu_sort_by_default){
			RecordListFragment appListFragment = (RecordListFragment)getFragmentManager().findFragmentById(R.id.container_roots);
			appListFragment.onSortModeChanged(SORT_BY_DEFAULT);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public static class RecordListFragment extends Fragment {
		private TextView mEmptyView;
		private ListView mList;
		private Context mContext;

		//default sort mode
		private int sortMode = SORT_BY_DEFAULT;
		private LoaderCallbacks<RecordResult> mCallbacks;
		protected DocumentsAdapter mAdapter;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			
			final View view = inflater.inflate(R.layout.fragment_record,null);
			mEmptyView = (TextView) view.findViewById(R.id.empty);
			mList = (ListView) view.findViewById(R.id.list);
			return view;
		}

		public void onSortModeChanged(int mode) {
			Log.d("yxf", "onSortModeChanged(), mode = " + mode); 
			sortMode = mode;
			getLoaderManager().restartLoader(0, null, mCallbacks);
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onActivityCreated(savedInstanceState);
			mContext = getActivity();
			mAdapter = new DocumentsAdapter();
			mCallbacks = new LoaderCallbacks<RecordResult>() {
				@Override
				public Loader<RecordResult> onCreateLoader(int id,
						Bundle args) {
					Log.d("yxf","onCreateLoader()");
					return new RecordLoader(getActivity(),sortMode);
				}

				@Override
				public void onLoadFinished(Loader<RecordResult> loader,
						RecordResult result) {
					Log.d("yxf","onLoadFinished(), result = " + result);
					if (!isAdded())
						return;
					mAdapter.swapResult(result);
                    mList.setAdapter(mAdapter);
                    mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View view,
								int position, long id) {
							// TODO Auto-generated method stub
							Log.d(RunningMainActivity.TAG,"onItemClick, view = " + view + "id = " + id);
							Cursor cursor = mAdapter.getItem(position);
							final int cursorId = cursor.getInt(cursor.getColumnIndex(RunRecordTable.COLUMN_ID));
							final double distance = cursor.getDouble(cursor.getColumnIndex(RunRecordTable.COLUMN_DISTANCE));
			                final long timeMilliSeconds  = cursor.getLong(cursor.getColumnIndex(RunRecordTable.COLUMN_DATE));
			                final long usedTime = cursor.getLong(cursor.getColumnIndex(RunRecordTable.COLUMN_USEDTIME));
			                Intent intent = new Intent(mContext, HistoryDetailsActivity.class);
			                intent.putExtra("_id", cursorId);
							intent.putExtra("total_time", usedTime);
							intent.putExtra("start_time", timeMilliSeconds);
							intent.putExtra("total_distance",distance);
							startActivity(intent);
						}
					});
				}

				@Override
				public void onLoaderReset(Loader<RecordResult> loader) {
				    mAdapter.swapResult(null);
				}
			};
			getLoaderManager().restartLoader(0, null, mCallbacks);
		}

		
		class DocumentsAdapter extends BaseAdapter {
			private Cursor mCursor;
			protected int mCursorCount;
			int index = 0;

			public void swapResult(RecordResult result) {
				mCursor = result != null ? result.cursor : null;
				mCursorCount = mCursor != null ? mCursor.getCount() : 0;
				if (isEmpty()) {
					mEmptyView.setVisibility(View.VISIBLE);
					mEmptyView.setTextColor(getResources().getColor(
							android.R.color.holo_blue_bright));
				} else {
					mEmptyView.setVisibility(View.GONE);
				}

				notifyDataSetChanged();
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				Cursor cursor = getItem(position);
				View view = LayoutInflater.from(getActivity()).inflate(R.layout.list_item,null);
                TextView date = (TextView)view.findViewById(R.id.textViewDate);
                TextView time = (TextView)view.findViewById(R.id.textViewTime);
                TextView distanceView = (TextView)view.findViewById(R.id.textViewDistanceContent);
                ImageView arrowImage = (ImageView)view.findViewById(R.id.image_arrow);
                final int id = cursor.getInt(cursor.getColumnIndex(RunRecordTable.COLUMN_ID));
                final double distance = cursor.getDouble(cursor.getColumnIndex(RunRecordTable.COLUMN_DISTANCE));
                final long timeMilliSeconds  = cursor.getLong(cursor.getColumnIndex(RunRecordTable.COLUMN_DATE));
                final long usedTime = cursor.getLong(cursor.getColumnIndex(RunRecordTable.COLUMN_USEDTIME));
                String[] dateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timeMilliSeconds).split(" ");
                date.setText(dateAndTime[0]);
                time.setText(dateAndTime[1]);
                
                TextView usedTimeView = (TextView)view.findViewById(R.id.textViewTimeContent);
                usedTimeView.setText(TimeFormatHelper.formatTime(usedTime));
                Log.d("yxf","getView, positon = " + position + ", distance = " + distance);
                distanceView.setText((distance != 0) ? Utils.formatDoubleValue(distance/1000):"0.00");
                
                arrowImage.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View view) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(mContext, HistoryDetailsActivity.class);
						intent.putExtra("_id", id);
						intent.putExtra("total_time", usedTime);
						intent.putExtra("start_time", timeMilliSeconds);
						intent.putExtra("total_distance",distance);
						startActivity(intent);
					}
				});
				return view;
			}

			public void setIndex(int selected) {
				index = selected;
			}
            
			@Override
			public int getCount() {
				return mCursorCount;
			}

			@Override
			public Cursor getItem(int position) {
				mCursor.moveToPosition(position);
				return mCursor;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}
		}
	}
}
