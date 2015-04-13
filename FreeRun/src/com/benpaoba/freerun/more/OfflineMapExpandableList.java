package com.benpaoba.freerun.more;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.benpaoba.freerun.R;

public class OfflineMapExpandableList extends ExpandableListActivity {
	private final String TAG = "OfflineMapExpandableList";

	private List<String> mCityList;   //Group List
	private List<List<MKOLSearchRecord>> mChildrenCityList;  //Child List
	private OfflineCityMapAdapter mOfflineCityMapAdapter;
	private MKOfflineMap mOfflineMap;
	
	private ArrayList<MKOLUpdateElement> localMapList = null;
	private LocalMapAdapter lAdapter;
	private ListView localMapListView;
	private LinearLayout lm;
	
	private ProgressDialog progressDialog;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_offline_map);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.theme_background_green));
		
		mOfflineMap = new MKOfflineMap();
		mOfflineMap.init(new MKOfflineMapListener() {
			@Override
			public void onGetOfflineMapState(int type, int state) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onGetOfflineMapState()");
				switch (type) {
				case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
					MKOLUpdateElement update = mOfflineMap.getUpdateInfo(state);
					// 处理下载进度更新提示
					if (update != null) {
						Toast.makeText(getApplicationContext(), "Downloading ... ", Toast.LENGTH_LONG).show();
						Log.d(TAG, String.format("%s : %d%%", update.cityName,
								update.ratio));

						progressDialog.setProgress(update.ratio);

					}
				}
					break;
				case MKOfflineMap.TYPE_NEW_OFFLINE:
					// 有新离线地图安装
					Log.d(TAG, String.format("add offlinemap num:%d", state));
					break;
				case MKOfflineMap.TYPE_VER_UPDATE:
					// 版本更新提示
					// MKOLUpdateElement e = mOffline.getUpdateInfo(state);

					break;
				}
			}
		});
		
		// the all offline map cities display.
		initializeData();
		getExpandableListView().setAdapter(new OfflineCityMapAdapter());
		getExpandableListView().setDividerHeight(10);

		
		
		// the download offline map cities display
		lm = (LinearLayout) findViewById(R.id.localmap_layout);
		localMapList = mOfflineMap.getAllUpdateInfo();// 获取已下过的离线地图信息
		if (localMapList == null) {
			localMapList = new ArrayList<MKOLUpdateElement>();
			lm.setVisibility(View.GONE);
		} 
		localMapListView = (ListView) findViewById(R.id.localmaplist);
		lAdapter = new LocalMapAdapter();
		localMapListView.setAdapter(lAdapter);
		localMapListView.setDividerHeight(10);

		
	}
	
	private void initializeData() {
		// TODO Auto-generated method stub
		mCityList = new ArrayList<String>();
		mChildrenCityList = new ArrayList<List<MKOLSearchRecord>>();
		//List<String> mChildrenCityItem = new ArrayList<String>();
		// 获取所有支持离线地图的城市
		ArrayList<MKOLSearchRecord> offlineCityList = mOfflineMap.getOfflineCityList();
		if (offlineCityList != null) {
			for (MKOLSearchRecord r : offlineCityList) {
				mCityList.add(r.cityName + "(" + r.cityID + ")" + "   --"
						+ this.formatDataSize(r.size));
				if(r.childCities != null) {
					
					mChildrenCityList.add(r.childCities);
					
				}else {
					List<MKOLSearchRecord> mChildrenCityItem = new ArrayList<MKOLSearchRecord>();
					mChildrenCityItem.add(r);
					mChildrenCityList.add(mChildrenCityItem);
				}
				//mChildrenCityItem.clear();
				
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		this.getExpandableListView().setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				MKOLSearchRecord record = mChildrenCityList.get(groupPosition).get(childPosition);
				Log.d(TAG, "Downloading ....");
				mOfflineMap.start(record.cityID);
				
				progressDialog = new ProgressDialog(OfflineMapExpandableList.this);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setMessage("Loading...");
//				progressDialog.setCancelable(false);
				progressDialog.show();
				
				localMapList = mOfflineMap.getAllUpdateInfo();
				if (localMapList == null) {
					localMapList = new ArrayList<MKOLUpdateElement>();
				}
				lAdapter.notifyDataSetChanged();
				lm.setVisibility(View.VISIBLE);
				return true;
			}
		});
	}
	
	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			int total = msg.arg1;
            progressDialog.setProgress(total);
            if (total >= 100){
                progressDialog.cancel();
            }
		}
	};

	class OfflineCityMapAdapter extends BaseExpandableListAdapter {

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return mChildrenCityList.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			MKOLSearchRecord record = mChildrenCityList.get(groupPosition).get(childPosition);
			View view = View.inflate(OfflineMapExpandableList.this, R.layout.offline_map_children_item, null);
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 80);
			view.setLayoutParams(lp);
			TextView cityName = (TextView)view.findViewById(R.id.cityName);
			TextView dataSize = (TextView)view.findViewById(R.id.dataSize);
			cityName.setText(record.cityName);
			dataSize.setText(
					OfflineMapExpandableList.formatDataSize(record.size));
			return view;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return mChildrenCityList.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return mCityList.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return mCityList.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			String string = mCityList.get(groupPosition);
			return getGericView(string);
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}
		
		public TextView getGericView(String s) {
			//Layout parameters for the ExpandableListView
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, 80);
			TextView text = new TextView(OfflineMapExpandableList.this);
			text.setLayoutParams(lp);
			//Center the text Vertically
			text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			text.setPadding(70, 0, 0, 0);
			
			text.setText(s);
			return text;
		}
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public static String formatDataSize(int size) {
		String ret = "";
		if (size < (1024 * 1024)) {
			ret = String.format("%dK", size / 1024);
		} else {
			ret = String.format("%.1fM", size / (1024 * 1024.0));
		}
		return ret;
	}
	

	/**
	 * 离线地图管理列表适配器
	 */
	public class LocalMapAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return localMapList.size();
		}

		@Override
		public Object getItem(int index) {
			return localMapList.get(index);
		}

		@Override
		public long getItemId(int index) {
			return index;
		}

		@Override
		public View getView(int index, View view, ViewGroup arg2) {
			MKOLUpdateElement e = (MKOLUpdateElement) getItem(index);
			view = View.inflate(OfflineMapExpandableList.this,
					R.layout.offline_localmap_list, null);
			initViewItem(view, e);
			return view;
		}

		void initViewItem(View view, final MKOLUpdateElement e) {
			LinearLayout root = (LinearLayout) view.findViewById(R.id.itemEntireRoot);
			//Button display = (Button) view.findViewById(R.id.display);
			Button remove = (Button) view.findViewById(R.id.remove);
			TextView title = (TextView) view.findViewById(R.id.title);
			TextView update = (TextView) view.findViewById(R.id.update);
			TextView ratio = (TextView) view.findViewById(R.id.ratio);
			ratio.setText(e.ratio + "%");
			title.setText(e.cityName);
			if (e.update) {
				update.setText("可更新");
			} else {
				update.setText("最新");
			}
//			if (e.ratio != 100) {
//				display.setEnabled(false);
//			} else {
//				display.setEnabled(true);
//			}
			remove.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mOfflineMap.remove(e.cityID);
					//updateView();
					localMapList = mOfflineMap.getAllUpdateInfo();
					if (localMapList == null) {
						localMapList = new ArrayList<MKOLUpdateElement>();
						lm.setVisibility(View.GONE);
					}
					lAdapter.notifyDataSetChanged();
					
				}
			});
//			display.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					Intent intent = new Intent();
//					intent.putExtra("x", e.geoPt.longitude);
//					intent.putExtra("y", e.geoPt.latitude);
//					intent.setClass(OfflineMap.this, RunningMainActivity.class);
//					startActivity(intent);
//				}
//			});
			root.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					Toast.makeText(getApplicationContext(), "rootView", Toast.LENGTH_LONG).show();
					Log.d(TAG, "onItemLongClick()");
					return false;
				}
			});
		}

	}
	
	public void onPause() {
		super.onPause();
		Log.d(TAG, "MoreSetupChoice, onPause()");
	}
	
	
}
