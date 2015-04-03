package com.benpaoba.freerun;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.util.DisplayMetrics;
import android.content.Intent;

public class HistoryDetailsActivity extends Activity implements
		OnGetGeoCoderResultListener {
	private static final String TAG = "HistoryDetails";
	private static final int DEFAULT_LEVEL = 18;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private List<LatLng> mPointLists;
	private LatLng mStartPoint;
	private LatLng mEndPoint;
	private GeoCoder mSearch;
	private File mDataFile;
	private DataInputStream mInput;

	private long mStartTime;
	private long mTotalTime;
	private double mTotalDistance;
	private int mId;
	
	private TextView mTimeTextView;
	private TextView mTotalTimeTextView;
	private TextView mTotalDistanceTextView;
	private TextView mAverageSpeedTextView;
	private TextView mPaceSpeedTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_sportdetais);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
        	mId = bundle.getInt("_id");
            mTotalTime = bundle.getLong("total_time");
            mTotalDistance = bundle.getDouble("total_distance");
            mStartTime = bundle.getLong("start_time");
        }
        mTimeTextView = (TextView)findViewById(R.id.tv_start_time); 
        mTotalTimeTextView = (TextView)findViewById(R.id.tv_total_time);
        mTotalDistanceTextView = (TextView)findViewById(R.id.tv_total_distance);
        mAverageSpeedTextView = (TextView)findViewById(R.id.tv_run_speed);
        mPaceSpeedTextView = (TextView)findViewById(R.id.tv_run_pace);
        		
        mTimeTextView.setText(getResources().getString(R.string.app_name) + " " +  
        		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(mStartTime));
        mTotalTimeTextView.setText(TimeFormatHelper.formatTime(mTotalTime));
        BigDecimal b = new BigDecimal(mTotalDistance / 1000); 
    	double formatDistance = b.setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
        mTotalDistanceTextView.setText(Utils.formatDoubleValue(formatDistance));
        updateDetails(mTotalDistance, mTotalTime);
		// 初始化地图
		mMapView = (MapView) findViewById(R.id.map_view);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.clear();
		mSearch = GeoCoder.newInstance();
		mDataFile = new File(SportsManager.POINTS_DIR,
				SportsManager.POINTS_FILE + mId + SportsManager.SUFFIX);
		mPointLists = (ArrayList<LatLng>) readPointsFromFile();
		double mMaxWidthDistance = 0;
		double mMaxHeightDistance = 0;
		DistanceComputeInterface distanceComputeInterface = DistanceComputeImpl.getInstance();
       
		if(mPointLists != null && mPointLists.size() > 1 ) {
	        double maxLat = mPointLists.get(0).latitude;
	        double maxLng = mPointLists.get(0).longitude;
	        double minLat = mPointLists.get(0).latitude;
	        double minLng = mPointLists.get(0).longitude;
			for(LatLng point : mPointLists) {
				if(maxLat < point.latitude) {
					maxLat = point.latitude;
				}
				
				if(maxLng < point.longitude) {
					maxLng = point.longitude;
				}
				
				if(minLat > point.latitude) {
					minLat = point.latitude;
				}
				
				if(minLng > point.longitude) {
					minLng = point.longitude;
				}
			}
			
			mMaxWidthDistance = distanceComputeInterface.getShortDistance(
	        			minLat,
	        			minLng,
	        			minLat,
	        			maxLng);
		    mMaxHeightDistance = distanceComputeInterface.getShortDistance(
		    		minLat, 
		    		minLng, 
		    		maxLat, 
		    		minLng);
		    
		    mStartPoint = mPointLists.get(0);
		    mEndPoint = mPointLists.get(mPointLists.size() - 1);
		    mSearch.reverseGeoCode(new ReverseGeoCodeOption().
				    location(new LatLng((minLat + maxLat) / 2,(minLng + maxLng) / 2)));
		    mSearch.setOnGetGeoCodeResultListener(this);
		    addSportDetails();
		    
		}
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
	
		Projection projection = mBaiduMap.getProjection();
		float meterToPixelsInWidth = 0;
		float meterToPixelsInHeight = 0;
		Log.d(TAG,"sceenH = " + screenHeight + ", w = " + screenWidth + ", maxW = " + mMaxWidthDistance + 
				", maxH = " + mMaxHeightDistance + ", meter : " + projection);
		
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.sports_details, menu);
		return true;
	}


	private void updateDetails(final double distance, final long usedTime) {
    	if(distance == 0 || usedTime == 0) {
    		return;
    	}
    	double averageSpeed = (60 * 60 * distance) / 1000 / usedTime;   //km/h
    	double paceSpeed = (1000 * usedTime ) / distance; //seconds
    	BigDecimal b = new BigDecimal(averageSpeed); 
    	double formatSpeed = b.setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    	
    	mAverageSpeedTextView.setText(Utils.formatDoubleValue(formatSpeed));
    	mPaceSpeedTextView.setText(TimeFormatHelper.formatTime((int)paceSpeed));
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final int id = item.getItemId();
		if(id == android.R.id.home) {
			finish();
			return true;
		}else if(id == R.id.action_share) {
			Intent intent=new Intent(Intent.ACTION_SEND); 
			intent.setType("text/plain"); //"image/*"
			intent.putExtra(Intent.EXTRA_SUBJECT,"共享软件"); 
			intent.putExtra(Intent.EXTRA_TEXT, "最专业的跑步软件 【奔跑吧】，亲们赶快使用吧，很多惊喜哦~");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			startActivity(Intent.createChooser(intent, "选择分享类型"));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private List<LatLng> readPointsFromFile() {
		boolean flag = true;
		List<LatLng> dataList = new ArrayList<LatLng>();
		double latitude;
		double longitude;
		try {
			if (mDataFile == null || !mDataFile.exists()) {
				return null;
			}
			mInput = new DataInputStream(new FileInputStream(mDataFile));
			while (flag) {
				latitude = mInput.readDouble();
				mInput.readChar();
				longitude = mInput.readDouble();
				mInput.readChar();
				dataList.add(new LatLng(latitude, longitude));
			}
		} catch (EOFException e) {
			flag = false;
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			mInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataList;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mMapView.onResume();
		super.onResume();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mMapView.onPause();
		super.onPause();
		Log.d(TAG,"Histroy, onPause()");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mMapView.onDestroy();
		super.onDestroy();
		Log.d(TAG,"Histroy, onDestroy()");
	}

	private void addSportDetails() {
		OverlayOptions ooPolyline = new PolylineOptions().width(8)
				.color(0xAAFF0000).points(mPointLists);
		mBaiduMap.addOverlay(ooPolyline);
		mBaiduMap.addOverlay(new MarkerOptions().position(
				mStartPoint)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.icon_st)));
		mBaiduMap.addOverlay(new MarkerOptions().position(
				mEndPoint)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.icon_en)));
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(HistoryDetailsActivity.this, "抱歉，未能找到结果",
					Toast.LENGTH_LONG).show();
			return;
		}
		 
		LatLng latLng = result.getLocation();
		if(mBaiduMap != null) {
		    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(
			    result.getLocation(), DEFAULT_LEVEL));
		}
		
	}
}

