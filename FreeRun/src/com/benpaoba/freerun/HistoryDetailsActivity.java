package com.benpaoba.freerun;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
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
import android.widget.TextView;
import android.widget.Toast;
import android.util.DisplayMetrics;

import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.GooglePlusShareContent;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.TencentWbShareContent;
import com.umeng.socialize.media.TwitterShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.media.UMusic;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

public class HistoryDetailsActivity extends Activity implements
		OnGetGeoCoderResultListener {
	private static final String TAG = "HistoryDetails";
	private float mDisplayLevel;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private Projection mProjection;
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
	
	private final UMSocialService mController = UMServiceFactory
            .getUMSocialService(Constants.DESCRIPTOR);
    private SHARE_MEDIA mPlatform = SHARE_MEDIA.SINA;
    
    private Point mMostLeftAndBottomPoint;
    private Point mMostRightAndTopPoint;
    private double mMaxLat;
    private double mMaxLng;
    private double mMinLat;
    private double mMinLng;
    
    private int mScreenWidthInPixels;
    private int mScreenHeightInPixels;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenWidthInPixels = dm.widthPixels;
		mScreenHeightInPixels = dm.heightPixels;
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
		mDisplayLevel = mBaiduMap.getMaxZoomLevel();
		mBaiduMap.clear();
		mBaiduMap.setMyLocationData(new MyLocationData.Builder().latitude(0.0f)
				.longitude(0.0f).build());
		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(
				new LatLng(0.0f,0.0f), mDisplayLevel));
		mBaiduMap.setOnMapLoadedCallback(mMapLoadedCallback);
		mBaiduMap.setOnMapStatusChangeListener(mMapStatusChangeListener);
		mSearch = GeoCoder.newInstance();
		mDataFile = new File(SportsManager.POINTS_DIR,
				SportsManager.POINTS_FILE + mId + SportsManager.SUFFIX);
		mPointLists = (ArrayList<LatLng>) readPointsFromFile();
		DistanceComputeInterface distanceComputeInterface = DistanceComputeImpl.getInstance();
        Log.d(TAG,"mPointList : " + mPointLists + ", size:" + ((mPointLists==null) ? 0 : mPointLists.size()));
		if(mPointLists != null && mPointLists.size() > 1 ) {
	        mMaxLat = mPointLists.get(0).latitude;
	        mMaxLng = mPointLists.get(0).longitude;
	        mMinLat = mPointLists.get(0).latitude;
	        mMinLng = mPointLists.get(0).longitude;
			for(LatLng point : mPointLists) {
				if(mMaxLat < point.latitude) {
					mMaxLat = point.latitude;
				}
				
				if(mMaxLng < point.longitude) {
					mMaxLng = point.longitude;
				}
				
				if(mMinLat > point.latitude) {
					mMinLat = point.latitude;
				}
				
				if(mMinLng > point.longitude) {
					mMinLng = point.longitude;
				}
			}
			
		    mStartPoint = mPointLists.get(0);
		    mEndPoint = mPointLists.get(mPointLists.size() - 1);
		    mSearch.reverseGeoCode(new ReverseGeoCodeOption().
				    location(new LatLng((mMinLat + mMaxLat) / 2,(mMinLng + mMaxLng) / 2)));
		    mSearch.setOnGetGeoCodeResultListener(this);
		    addSportDetails();
		}
	}
	
	private BaiduMap.OnMapLoadedCallback mMapLoadedCallback = new BaiduMap.OnMapLoadedCallback(){

		@Override
		public void onMapLoaded() {
			// TODO Auto-generated method stub
			Log.d(TAG,"HistoryDetailsActivity, onMapLoaded()");
			mProjection = mBaiduMap.getProjection();
			if(mProjection != null) {
			    mMostLeftAndBottomPoint = mProjection.toScreenLocation(new LatLng(mMinLat,mMinLng));
			    mMostRightAndTopPoint = mProjection.toScreenLocation(new LatLng(mMaxLat, mMaxLng));
			    
			    Log.d(TAG," id = " + mId + ",onMapLoaded, Point Info: " +
					    "left = " + mMostLeftAndBottomPoint.x + 
					    ", right = " + mMostRightAndTopPoint.x +
					    ", bottom = " + mMostLeftAndBottomPoint.y + 
					    ", top = " +  mMostRightAndTopPoint.y);
			    
			    DisplayMetrics dm = new DisplayMetrics();
			    getWindowManager().getDefaultDisplay().getMetrics(dm);
			    int level = (int)mDisplayLevel;
			    if ((Math.abs(mMostRightAndTopPoint.x - mMostLeftAndBottomPoint.x) >= mScreenWidthInPixels)
					|| (Math.abs(mMostRightAndTopPoint.y - mMostLeftAndBottomPoint.y) >= mScreenHeightInPixels)) {
			        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(--level);
				if (u != null) {
			            mBaiduMap.animateMapStatus(u);
				}
			    }
			}
		}
	};
	
	private OnMapStatusChangeListener mMapStatusChangeListener = new OnMapStatusChangeListener() {

		@Override
		public void onMapStatusChange(MapStatus status) {
			// TODO Auto-generated method stub
		} 

		@Override
		public void onMapStatusChangeFinish(MapStatus status) {
			// TODO Auto-generated method stub
		    Log.d(TAG,"onMapStatusFinish, status = " + status);
		    mProjection = mBaiduMap.getProjection();
		    if(mProjection == null) {
		        return;
		    }
		    int level = (int)status.zoom;
		    mMostLeftAndBottomPoint = mProjection.toScreenLocation(new LatLng(mMinLat,mMinLng));
		    mMostRightAndTopPoint = mProjection.toScreenLocation(new LatLng(mMaxLat, mMaxLng));
	            Log.d(TAG,"After MapStatus Finish(), " +  
	    	            ", left = " + mMostLeftAndBottomPoint.x + 
			    ", right = " + mMostRightAndTopPoint.x +
			    ", bottom = " + mMostLeftAndBottomPoint.y + 
			    ", top = " +  mMostRightAndTopPoint.y);
	    	
	    	if ((Math.abs(mMostRightAndTopPoint.x - mMostLeftAndBottomPoint.x) >= mScreenWidthInPixels)
					|| (Math.abs(mMostRightAndTopPoint.y - mMostLeftAndBottomPoint.y) >= mScreenHeightInPixels)) {
		        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(--level);
			if (u != null) {
		    	    Log.d(TAG,"onMapStatusChangeFinish, animateMapStatus, new level =  " + level);
		            mBaiduMap.animateMapStatus(u);
			}
		    }
		}

		@Override
		public void onMapStatusChangeStart(MapStatus status) {
			// TODO Auto-generated method stub
		}
		
	};
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
			/*
			Intent intent=new Intent(Intent.ACTION_SEND); 
			intent.setType("text/plain"); //"image/*"
			intent.putExtra(Intent.EXTRA_SUBJECT,"共享软件"); 
			intent.putExtra(Intent.EXTRA_TEXT, "最专业的跑步软件 【奔跑吧】，亲们赶快使用吧，很多惊喜哦~");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			startActivity(Intent.createChooser(intent, "选择分享类型"));
			*/
			mController.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,
                    SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE,SHARE_MEDIA.SINA,SHARE_MEDIA.TENCENT, SHARE_MEDIA.DOUBAN,
                    SHARE_MEDIA.RENREN);
            mController.openShare(this, false);
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
		
		// 添加QQ、QZone平台
        addQQAndQZonePlatform();

        // 添加微信、微信朋友圈平台
        addWXPlatform();
        
        setShareContent();
	}

	private void addQQAndQZonePlatform() {
        String appId = "1104450496";
        String appKey = "6f0fd8q9lCGsRHBv";
        // 添加QQ支持, 并且设置QQ分享内容的target url
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(this,
                appId, appKey);
        qqSsoHandler.setTargetUrl("http://www.umeng.com/social");
        qqSsoHandler.addToSocialSDK();

        // 添加QZone平台
        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(this, appId, appKey);
        qZoneSsoHandler.addToSocialSDK();
    }
	
	/**
     * @功能描述 : 添加微信平台分享
     * @return
     */
    private void addWXPlatform() {
        // 注意：在微信授权的时候，必须传递appSecret
        // wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
        String appId = "wx5a20f7622f9ab2f2";
        String appSecret = "ac0f2103ab8789fd3d3aaf4af8e4b0bc";
        // 添加微信平台
        UMWXHandler wxHandler = new UMWXHandler(this, appId, appSecret);
        wxHandler.addToSocialSDK();

        // 支持微信朋友圈
        UMWXHandler wxCircleHandler = new UMWXHandler(this, appId, appSecret);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
    }
    
    private void setShareContent() {
    	final String shareContent = getResources().getString(R.string.share_content, 
    			Utils.formatDoubleValue(mTotalDistance / 1000), mTotalTime / 60, mTotalTime % 60);
        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(this,
                "1104450496", "6f0fd8q9lCGsRHBv");
        qZoneSsoHandler.addToSocialSDK();
        mController.setShareContent(shareContent + "--QQ空间");

        UMImage localImage = new UMImage(this, R.drawable.device);
        UMImage urlImage = new UMImage(this,
                "http://www.umeng.com/images/pic/social/integrated_3.png");
        // UMImage resImage = new UMImage(getActivity(), R.drawable.icon);

        // 视频分享
        UMVideo video = new UMVideo(
                "http://v.youku.com/v_show/id_XNTc0ODM4OTM2.html");
        // vedio.setThumb("http://www.umeng.com/images/pic/home/social/img-1.png");
        video.setTitle(getResources().getString(R.string.share_title));
        video.setThumb(urlImage);

        UMusic uMusic = new UMusic(
                "http://music.huoxing.com/upload/20130330/1364651263157_1085.mp3");
        uMusic.setAuthor(getResources().getString(R.string.app_name));
        uMusic.setTitle(getResources().getString(R.string.share_title));
        uMusic.setThumb(urlImage);
        uMusic.setThumb("http://www.umeng.com/images/pic/social/chart_1.png");
        
        WeiXinShareContent weixinContent = new WeiXinShareContent();
        weixinContent.setShareContent(shareContent + "--微信");
        weixinContent.setTitle(getResources().getString(R.string.share_title));
        weixinContent.setTargetUrl("http://www.umeng.com/social");
        weixinContent.setShareMedia(urlImage);
        mController.setShareMedia(weixinContent);

        // 设置朋友圈分享的内容
        CircleShareContent circleMedia = new CircleShareContent();
        circleMedia.setShareContent(shareContent + "--微信朋友圈");
        circleMedia.setTitle(getResources().getString(R.string.share_title));
        circleMedia.setShareMedia(urlImage);
        // circleMedia.setShareMedia(uMusic);
        // circleMedia.setShareMedia(video);
        circleMedia.setTargetUrl("http://www.umeng.com/social");
        mController.setShareMedia(circleMedia);


        UMImage qzoneImage = new UMImage(this,
                "http://www.umeng.com/images/pic/social/integrated_3.png");
        qzoneImage
                .setTargetUrl("http://www.umeng.com/images/pic/social/integrated_3.png");

        // 设置QQ空间分享内容
        QZoneShareContent qzone = new QZoneShareContent();
        qzone.setShareContent(shareContent + "--QQ空间");
        qzone.setTargetUrl("http://www.umeng.com");
        qzone.setTitle(getResources().getString(R.string.share_title));
        qzone.setShareMedia(urlImage);
        //qzone.setShareMedia(uMusic);
        mController.setShareMedia(qzone);

        video.setThumb(new UMImage(this, BitmapFactory.decodeResource(
                getResources(), R.drawable.device)));

        QQShareContent qqShareContent = new QQShareContent();
        qqShareContent.setShareContent(shareContent + "--QQ分享");
        qqShareContent.setTitle(getResources().getString(R.string.share_title));
        qqShareContent.setShareMedia(uMusic);
        qqShareContent.setTargetUrl("http://www.umeng.com/social");
        mController.setShareMedia(qqShareContent);

        // 视频分享
        UMVideo umVideo = new UMVideo(
                "http://v.youku.com/v_show/id_XNTc0ODM4OTM2.html");
        umVideo.setThumb("http://www.umeng.com/images/pic/home/social/img-1.png");
        umVideo.setTitle(getResources().getString(R.string.share_title));

        TencentWbShareContent tencent = new TencentWbShareContent();
        tencent.setShareContent(shareContent + "--腾讯微博");
        // 设置tencent分享内容
        mController.setShareMedia(tencent);
        
        SinaShareContent sinaContent = new SinaShareContent();
        sinaContent.setShareContent(shareContent + "--新浪微博");
        mController.setShareMedia(sinaContent);

        TwitterShareContent twitterShareContent = new TwitterShareContent();
        twitterShareContent
                .setShareContent(shareContent + "--Twitter");
        twitterShareContent.setShareMedia(new UMImage(this, new File("/storage/sdcard0/emoji.gif")));
        mController.setShareMedia(twitterShareContent);

        GooglePlusShareContent googlePlusShareContent = new GooglePlusShareContent();
        googlePlusShareContent
                .setShareContent(shareContent + "--Google+");
        googlePlusShareContent.setShareMedia(localImage);
        mController.setShareMedia(googlePlusShareContent);
        
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
		
		LatLng location = result.getLocation();
		Log.d(TAG,"onGetReverseGeoCode, zoomLevel = " + mDisplayLevel);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(
                        location, mDisplayLevel);
		if (u != null) {
		    mBaiduMap.animateMapStatus(u);
		}
	}
}

