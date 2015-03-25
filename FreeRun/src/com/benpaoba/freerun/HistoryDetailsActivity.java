package com.benpaoba.freerun;

import android.app.Activity;
import android.os.Bundle;
import java.util.List;
import java.util.ArrayList;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import android.util.Log;
import android.widget.Toast;

public class HistoryDetailsActivity extends Activity implements
        OnGetGeoCoderResultListener {
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private List<LatLng> mPointLists;
	private LatLng mCenterPoint;
	private GeoCoder mSearch;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sportdetais);
		
		// 初始化地图
	    mMapView = (MapView) findViewById(R.id.map_view);
		mBaiduMap = mMapView.getMap();
		mSearch = GeoCoder.newInstance();
		mPointLists = (ArrayList)DataSaveUtils.readDataFromFile();
		mCenterPoint = mPointLists.get((mPointLists.size() - 1) / 2);
		mSearch.reverseGeoCode(new ReverseGeoCodeOption()
		        .location(mCenterPoint));
		mSearch.setOnGetGeoCodeResultListener(this);
		addSportDetails();
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
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mMapView.onDestroy();
		super.onDestroy();
	}
	
	private void addSportDetails() {
		OverlayOptions ooPolyline = new PolylineOptions().width(10)
				.color(0xAAFF0000).points(mPointLists);
		mBaiduMap.addOverlay(ooPolyline);
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult arg0) {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(HistoryDetailsActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
					.show();
			return;
		}
		mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.icon_marka)));
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
				.getLocation()));
		Toast.makeText(HistoryDetailsActivity.this, result.getAddress(),
				Toast.LENGTH_LONG).show();
	}
	
}
