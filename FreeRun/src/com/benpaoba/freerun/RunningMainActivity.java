package com.benpaoba.freerun;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.math.BigDecimal;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;

public class RunningMainActivity extends Activity {
	public static final String TAG = "RunningMap";
	
	private final static float DEFAULT_ZOOM_LEVEL = 18.0f;
	private final static float UPPER_ZOOM_LEVEL = 18.0f;
	private int mSportStatus;
	private DistanceInfoDao mDistanceInfoDao;
	// ��λ���
    LocationClient mLocClient;
	private CustomLocationListenner mLocationListener = new CustomLocationListenner();
	BitmapDescriptor mCurrentMarker;
    private ActivityManager mActivityManager;

	MapView mMapView;
	BaiduMap mBaiduMap;
	// UI���
	private TextView mGpsSignalTextView;
	private ViewGroup mTotalContentLayout;
	private ViewGroup mBaiduMapLayout;
	private ViewGroup mBottomLayout;
	private ViewGroup mRunningStateLayout;
	private ViewGroup mOtherDetailsLayout;
	private ViewGroup mUnLockedControllerLayout;
	private ViewGroup mLockedControllerLayout;
	private TextView mCountDownView;
	private Button mMiddleButton;
	private Button mLeftButton;
	private Button mRightButton;
	private ImageButton mLockImageButton;
	private ViewGroup mViewRunControllerLayout;
	
	private ImageView mFullMapImageView;
	private TextView mRunDistanceTextView;
	private TextView mRunTimeTextView;
	
	boolean mIsFirstLoc = true;

	private boolean mIsBaiduMapFullScreen = false;
	
	private LatLng mPointLast = null;
	private LatLng mPointBeforeLast = null;
	
	private int mUpdateInterval = 0;
	
    private SoundClips.Player mSoundPlayer;
    private Context mContext;
    private static final int SPEED = 30;
	private static final int SLEEP_TIME = 5;
	
	
	private boolean hasMeasured = false;
	private boolean isScrolling = false;
	
	private int window_width;
	private int max_width;
	
	private float mScrollX;
	
	private LinearLayout layout_left;
	private LinearLayout layout_right;
	
	private GestureDetector mGestureDetector;
	private ViewTreeObserver viewTreeObserver;
	private ImageButton userIcon;
    
	private LocationManager mLocationManager;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		Log.d(TAG,"onCreate, id : " + Thread.currentThread().getId());
		mDistanceInfoDao = new DistanceInfoDao(this);
		setContentView(R.layout.layout_main);
		mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		mLocationManager.addGpsStatusListener(mGpsStatusListener);
		
		mSportStatus = SportsManager.STATUS_READY;
		//��ȡ��Դ
		mGpsSignalTextView = (TextView) findViewById(R.id.tv_bmap_run_gps);
	    
		mTotalContentLayout = (RelativeLayout)findViewById(R.id.layout_all);
		mBaiduMapLayout = (RelativeLayout)findViewById(R.id.layout_bmap);
		mBottomLayout = (LinearLayout)findViewById(R.id.layout_bottom);
		mViewRunControllerLayout = (FrameLayout)findViewById(R.id.view_run_controller);
		mFullMapImageView = (ImageView)findViewById(R.id.img_bmap_full);
		mFullMapImageView.setOnClickListener(mOnClickListener);
		
		mRunningStateLayout = (LinearLayout)findViewById(R.id.layout_running_on);
		mOtherDetailsLayout = (LinearLayout)findViewById(R.id.other_details_layout);
		
		mUnLockedControllerLayout = (LinearLayout)findViewById(R.id.layout_controller);
		mLockedControllerLayout = (RelativeLayout)findViewById(R.id.layout_lock);
		mCountDownView = (TextView)findViewById(R.id.tv_action_number);
		mMiddleButton = (Button)findViewById(R.id.btn_running_middle);
	    mLeftButton = (Button)findViewById(R.id.btn_running_back);
	    mRightButton = (Button)findViewById(R.id.btn_running_right);
	    mLockImageButton = (ImageButton)findViewById(R.id.ibtn_running_lock);
		mRunningStateLayout.setVisibility(View.GONE);
	 	mLockedControllerLayout.setVisibility(View.GONE);
	 	mCountDownView.setVisibility(View.GONE);
	 	
	 	mRunDistanceTextView = (TextView) findViewById(R.id.tv_run_distance);
	 	mRunTimeTextView = (TextView)findViewById(R.id.tv_run_time);
		mLeftButton.setOnClickListener(mOnClickListener);
	 	mMiddleButton.setOnClickListener(mOnClickListener);
	 	mRightButton.setOnClickListener(mOnClickListener);
	 	mLockImageButton.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG, "ImageButton, onLongClick, arg0 = " + arg0);
				if (mActivityManager.isUserAMonkey()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							RunningMainActivity.this);
					builder.setMessage("�㲻�ܶԴ���Ļ���в�������Ϊ�㲻�ǹ���Ա");
					builder.setPositiveButton("I admit defeat", null);
					builder.show();
					return true;
				}
				return true;
			}
		});
	 	
	 	if(mSportStatus == SportsManager.STATUS_INITIAL || mSportStatus == SportsManager.STATUS_READY) {
	 	    mMiddleButton.setText(R.string.status_start);
	 	    //mLeftButton.setVisibility(View.GONE);
	 	    mRightButton.setVisibility(View.INVISIBLE);
	 	}
	 	
	    // ��ͼ��ʼ��
	 	mMapView = (MapView) findViewById(R.id.map_view);
	 	mBaiduMap = mMapView.getMap();
	 		
	 	// ������λͼ��
	 	mBaiduMap.setMyLocationEnabled(true);
	 	//startService(new Intent(this, LocationService.class));
	 	mLocationListener = new CustomLocationListenner();
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(mLocationListener);
        //��λ��������
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll"); //���صĶ�λ����ǰٶȾ�γ�ȣ�Ĭ��ֵgcj02
        option.setAddrType("all");    //���صĶ�λ���������ַ��Ϣ
        option.setScanSpan(1000);
        option.setOpenGps(true);
        mLocClient.setLocOption(option);
        mLocClient.start();
        mLocClient.requestLocation();
	}
	
	private int mUsedSatellitesCount = 0;
	private final GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) { // GPS״̬�仯ʱ�Ļص�����������
			GpsStatus status = mLocationManager.getGpsStatus(null); //ȡ��ǰ״̬
			if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
				int maxSatellites = status.getMaxSatellites();
				Iterator<GpsSatellite> it = status.getSatellites().iterator();
				while (it.hasNext() && mUsedSatellitesCount <= maxSatellites) {
					GpsSatellite s = it.next();
					if(s.usedInFix()) {
						mUsedSatellitesCount++;
					}
				}
			}
			int gpsState = GpsManager.getGpsStatus(mUsedSatellitesCount);
			updateGpsSignalTextView(gpsState);
		}
	};
	
	private void updateGpsSignalTextView(int gpsState) {
		String gpsDescription = null;
		switch(gpsState) {
		case GpsManager.GPS_BAD:
			gpsDescription = String.format(
					getResources().getString(R.string.gps_signal_text),
					getResources().getString(R.string.bad_gps));
			break;
		case GpsManager.GPS_NORMAL:
			gpsDescription = String.format(
					getResources().getString(R.string.gps_signal_text),
					getResources().getString(R.string.nomarl_gps));
			break;
		case GpsManager.GPS_GOOD:
			gpsDescription = String.format(
					getResources().getString(R.string.gps_signal_text),
					getResources().getString(R.string.good_gps));
			break;
		default:
			break;
		}
		mGpsSignalTextView.setText(gpsDescription);
	}
	
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
			
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			Log.d(TAG,"OnClick, view = " + view);
			switch(view.getId()) {
			case R.id.btn_running_back:
				finish();
				break;
				
			case R.id.btn_running_middle:		
				if(mSportStatus == SportsManager.STATUS_READY) {
					if(GpsManager.isGpsOpen(RunningMainActivity.this)) {
						startService(new Intent(RunningMainActivity.this, LocationService.class));
						Intent intent = new Intent(SportsManager.STATUS_ACTION);
						intent.putExtra("command",SportsManager.CMD_START);
						Log.d("LocationService","RunningMainActivity, sendBroadcast()");
						sendBroadcast(intent);
						mSoundPlayer.play(SoundClips.START_SPORT,0,0,0);
						mSportStatus = SportsManager.STATUS_RUNNING;
						mMiddleButton.setText(R.string.status_pause);
						mLeftButton.setVisibility(View.GONE);
						mRightButton.setText(R.string.status_finished);
						mRightButton.setVisibility(View.VISIBLE);
						mLockImageButton.setVisibility(View.VISIBLE);
						mRunningStateLayout.setVisibility(View.VISIBLE);
						
						DistanceInfo mDistanceInfo = new DistanceInfo();
						mDistanceInfo.setDistance(0f); // �����ʼֵ
						mDistanceInfo.setLongitude(RunningApplication.mLongtitude); // ���ȳ�ʼֵ
						mDistanceInfo.setLatitude(RunningApplication.mLatitude); // γ�ȳ�ʼֵ
						int id = mDistanceInfoDao.insertAndGet(mDistanceInfo);
						if (id != -1) {
							RunningApplication.mRunningInfoId = id;
							Toast.makeText(RunningMainActivity.this, "�Ѿ���ʼ�ܲ�...", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(RunningMainActivity.this, "id is -1,�޷�ִ�о����������", Toast.LENGTH_SHORT).show();
						}
						
						startTimer();
					} else {
						new AlertDialog.Builder(RunningMainActivity.this)
				        .setTitle(mContext.getString(R.string.open_gps_title))
				        .setMessage(mContext.getString(R.string.open_gps_message))
	                    .setPositiveButton(mContext.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								GpsManager.startIntentForGpsSetting(mContext);
							}
						})
						.setNegativeButton(mContext.getString(R.string.button_cancel), null).show();
					}
				}else if(mSportStatus == SportsManager.STATUS_RUNNING) {
					Intent intent = new Intent(SportsManager.STATUS_ACTION);
					intent.putExtra("command",SportsManager.CMD_PAUSE);
					sendBroadcast(intent);
					mSoundPlayer.play(SoundClips.PAUSE_SPORT,0,0,0);
					mSportStatus = SportsManager.STATUS_PAUSED;
					mMiddleButton.setText(R.string.status_continue);
					mLockImageButton.setVisibility(View.INVISIBLE);
					cancelTimer();
				} else if(mSportStatus == SportsManager.STATUS_PAUSED) {
					Intent intent = new Intent(SportsManager.STATUS_ACTION);
					intent.putExtra("command",SportsManager.CMD_CONTINUE);
					sendBroadcast(intent);
					mSoundPlayer.play(SoundClips.CONTINUE_SPORT,0,0,0);
					mSportStatus = SportsManager.STATUS_RUNNING;
					mMiddleButton.setText(R.string.status_pause);
					mLockImageButton.setVisibility(View.VISIBLE);
					startTimer();
				}
				break;
			case R.id.btn_running_right:
				Intent intent = new Intent(SportsManager.STATUS_ACTION);
				intent.putExtra("command",SportsManager.CMD_FINISH);
				sendBroadcast(intent);
				stopService(new Intent(RunningMainActivity.this, LocationService.class));
				mSoundPlayer.play(SoundClips.COMPLETE_SPORT,0,0,0);
				cancelTimer();
				
                DistanceInfo distanceInfo = mDistanceInfoDao.getById(RunningApplication.mRunningInfoId);
                if(distanceInfo != null) {
                	distanceInfo.setTime(mCurrentIndividualStatusSeconds);
                	mDistanceInfoDao.updateDistance(distanceInfo);
                }
                
				mSportStatus = SportsManager.STATUS_READY;
				mMiddleButton.setText(R.string.status_start);
				mRightButton.setVisibility(View.INVISIBLE);
				mRunningStateLayout.setVisibility(View.GONE);
			 	mLockedControllerLayout.setVisibility(View.GONE);
			 	mCountDownView.setVisibility(View.GONE);
			 	initialAndResetAllWidegts();
				break;
			case R.id.img_bmap_full:
				if(!mIsBaiduMapFullScreen) {
				    mViewRunControllerLayout.setVisibility(View.GONE);
				    mOtherDetailsLayout.setVisibility(View.GONE);
				    configOrientalOrHorizontalLayout(true);
				    mMapView.invalidate();
				    mIsBaiduMapFullScreen = true;
				}else {
					mViewRunControllerLayout.setVisibility(View.VISIBLE);
				    mOtherDetailsLayout.setVisibility(View.VISIBLE);
				    configOrientalOrHorizontalLayout(false);
				    mMapView.invalidate();
				    mIsBaiduMapFullScreen = false;
				}
			default:
				break;
			}
		}
	};

	private void initialAndResetAllWidegts() {
		mCurrentIndividualStatusSeconds = 0;
		mRunDistanceTextView.setText(Utils.getValueWith2Suffix(0.0f));
		mRunTimeTextView.setText(TimeFormatHelper.formatTime(0));
	}
	
	@SuppressLint("NewApi")
	private void configOrientalOrHorizontalLayout(boolean horizontal) {
		RelativeLayout.LayoutParams distanceParams = (RelativeLayout.LayoutParams) 
				mRunDistanceTextView.getLayoutParams();
		RelativeLayout.LayoutParams timeParams = (RelativeLayout.LayoutParams)
				mRunTimeTextView.getLayoutParams();
		if(horizontal) {
		    distanceParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
		    mRunDistanceTextView.setLayoutParams(distanceParams);
		    mRunDistanceTextView.setTextSize(30);
		    
		    timeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		    timeParams.addRule(RelativeLayout.ALIGN_BASELINE, R.id.tv_run_distance);
		    timeParams.addRule(RelativeLayout.BELOW,0);
		    mRunTimeTextView.setLayoutParams(timeParams);
		    mRunTimeTextView.setTextSize(30);
		} else {
			distanceParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
			mRunDistanceTextView.setLayoutParams(distanceParams);
		    mRunDistanceTextView.setTextSize(80);
		    
		    timeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0);
		    timeParams.removeRule(RelativeLayout.ALIGN_BASELINE);
		    timeParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		    timeParams.addRule(RelativeLayout.BELOW, R.id.tv_run_distance);
		    
		    mRunTimeTextView.setLayoutParams(timeParams);
		    mRunTimeTextView.setTextSize(30);
		}
	}
	
	private List<LatLng> mPointLists;
	/**
	 * ��λSDK��������
	 */
	public class CustomLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			Log.d(TAG,
					"onReceiveLocation(),getLatitude = "
							+ location.getLatitude() + ", getLongitude = "
							+ location.getLongitude()
							+ ", networkLocationType: "
							+ location.getNetworkLocationType()
							+ ", locationType:" + location.getLocType() + ", Thread :"+Thread.currentThread().getId());

			// map view ���ٺ��ڴ����½��յ�λ��
			if (location == null || mMapView == null) {
				return;
			}
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if(mIsFirstLoc) {
				mIsFirstLoc = false;
			    LatLng ll = new LatLng(location.getLatitude(),
				    	location.getLongitude());
			    MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll,
					    DEFAULT_ZOOM_LEVEL);
			    mBaiduMap.animateMapStatus(u);
			}

			// draw the sports path
			if (mSportStatus == SportsManager.STATUS_RUNNING && 
					(location.getLocType() == BDLocation.TypeGpsLocation || 
					location.getLocType() == BDLocation.TypeNetWorkLocation)) {
				mUpdateInterval++;
				if(mUpdateInterval == 3) {
					//DataSaveUtils.writeToFile(location.getLatitude(), location.getLongitude());
					//mPointLists.add(new )
					mUpdateInterval = 0;
				}
				LatLng currentPoint = new LatLng(location.getLatitude(),
						location.getLongitude());
				List<LatLng> pointLists = new ArrayList<LatLng>();
				Log.d(TAG, "currentPoint = " + currentPoint + ", mPointLast = "
						+ mPointLast + ", mPointBeforeLast = "
						+ mPointBeforeLast);
				if (mPointBeforeLast != null && mPointBeforeLast != null) {
					pointLists.add(mPointBeforeLast);
					pointLists.add(mPointLast);
					pointLists.add(currentPoint);
					OverlayOptions ooPolyline = new PolylineOptions().width(10)
							.color(0xAAFF0000).points(pointLists);
					mBaiduMap.addOverlay(ooPolyline);
				}

				mPointBeforeLast = mPointLast;
				mPointLast = currentPoint;
			}
		}
		
		public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null){
                return ;
            }
        }
	}

	private int mCurrentIndividualStatusSeconds = 0;
	private int mTimeBeforeOneMileSeconds = 0;
	private TimerTask mUpdateTimerValuesTask = null;
	private Timer mTimer;
	
	private final static int REFRESH_UI = 0x01;
	private final static int PLAY_SOUND = 0x02;
	private Handler mUpdateDisplayHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch(msg.what) {
        	case REFRESH_UI:
                updateDisplay();
                break;
        	case PLAY_SOUND:
        		double distance = (Double)msg.obj;
        		mSoundPlayer.play(SoundClips.TIMETICK_EACHMILE_SPORT, (int)distance, 
	    				mCurrentIndividualStatusSeconds, 
	    				(mCurrentIndividualStatusSeconds - mTimeBeforeOneMileSeconds));
	    		mTimeBeforeOneMileSeconds = mCurrentIndividualStatusSeconds;
        		break;
        	}
        }
    };
    
    //update the time and distance display
    protected void updateDisplay() {
    	mRunTimeTextView.setText(TimeFormatHelper.formatTime(mCurrentIndividualStatusSeconds));
    }
    
	/**
     * ����һ��Timer��ÿ��һ���Ӹ���һ�½���
     */
    private synchronized void startTimer() {
        mTimer = new Timer();
        /**
         * void schedule (TimerTask task, long delay, long period)
         * task--��Ҫִ�е�����
         * delay--�û�����schedule()������೤ʱ�俪ʼִ��run()�������Ժ���Ϊ��λ��
         * period--��һ�ε���֮���Ժ�ÿ���೤ʱ����һ��ִ��run()�������Ժ���Ϊ��λ��
         */
        if(mUpdateTimerValuesTask == null) {
        	mUpdateTimerValuesTask = new TimerTask() {
                @Override
                public void run() {
                	if(mSportStatus == SportsManager.STATUS_RUNNING) {
                	    Log.d(TAG,"updateTimerValuesTask, run(), Thread: " + Thread.currentThread().getId());
                        updateTimerValues();
                        new UpdateDistanceTask().execute();
                	}else {
                		this.cancel();
                	}
                }
            };
        }
        mTimer.schedule(mUpdateTimerValuesTask, 1000, 1000);
    }
    
    protected synchronized void updateTimerValues() {
    	mCurrentIndividualStatusSeconds++;
        Log.d(TAG,"updateTimerValues(), currentIndividualStatusSeconds = " + mCurrentIndividualStatusSeconds);
        mUpdateDisplayHandler.sendMessage(Message.obtain(mUpdateDisplayHandler, REFRESH_UI));
    }
    
    private synchronized void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if(mUpdateTimerValuesTask != null) {
        	mUpdateTimerValuesTask.cancel();
        	mUpdateTimerValuesTask = null;
        }
    }
	    
	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
		if (mSoundPlayer != null) {
            mSoundPlayer.release();
            mSoundPlayer = null;
        }
	}
	
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
		mSoundPlayer = SoundClips.getPlayer(this);
		String gpsSinalDescription = String.format(
				getResources().getString(R.string.gps_signal_text),
				GpsManager.isGpsOpen(this) ? getResources().getString(R.string.bad_gps) :
					getResources().getString(R.string.no_gps)
				);
		mGpsSignalTextView.setText(gpsSinalDescription);
		userIcon =(ImageButton)findViewById(R.id.user_icon);
		
		userIcon.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(FreeRunConstants.ACTION_CHECK_PROFILE_INFO);
				startActivity(intent);
				/*
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_left
						.getLayoutParams();
				TextView title = (TextView) findViewById(R.id.title);
				// ���ƶ�
				if (layoutParams.leftMargin >= 0) {
					new AsynMove().execute(-SPEED);
					title.setVisibility(View.VISIBLE);
				} else {
					// ���ƶ�
					new AsynMove().execute(SPEED);
					title.setVisibility(View.GONE);
				}
					*/
			}
		} );
	
	}

	 private class MyOnGestureListener implements OnGestureListener {
		  @Override
		  public boolean onDown(MotionEvent e) {
			  Log.d(TAG, "onDown: " + e.getAction());
	
		    return true;
		  }
		  /***
		   * e1 ����㣬e2���յ㣬���distanceX=e1.x-e2.x>0˵�����󻬶�����֮�����.
		   */
		  @Override
		  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
		      float distanceY) {
			  Log.d(TAG, "onScroll: " + e1.getAction() + " : " + e2.getAction() 
					  + " : " + distanceX + " : " + distanceY);
			    mScrollX += distanceX;// distanceX:����Ϊ������Ϊ��
			    RelativeLayout.LayoutParams layoutParams_left = (RelativeLayout.LayoutParams) layout_left
			        .getLayoutParams();
			    RelativeLayout.LayoutParams layoutParams_right = (RelativeLayout.LayoutParams) layout_right
				        .getLayoutParams();
			    
			    layoutParams_left.leftMargin -= mScrollX;
			    layoutParams_left.rightMargin += mScrollX;
			    layoutParams_right.leftMargin -=mScrollX;
			    //Log.d(TAG, "rightMargin = " + layoutParams.rightMargin + "\nleftMargin = " + layoutParams.leftMargin);
			    if (layoutParams_left.leftMargin >= 0) {
			      layoutParams_left.leftMargin = 0;
			      layoutParams_left.rightMargin = window_width-max_width;
			      layoutParams_right.leftMargin = max_width;

			    } else if (layoutParams_left.leftMargin <= -max_width) {
			         // �Ϲ�ͷ�˲���Ҫ��ִ��AsynMove��
			      layoutParams_left.leftMargin = -max_width;
			      layoutParams_left.rightMargin = max_width;
			      layoutParams_right.leftMargin = 0;
			    } else {
				      Log.d(TAG, "leftMargin = " + layoutParams_left.leftMargin);
				      // ����ȥ
				      if (layoutParams_left.leftMargin > max_width / 2) {
				        new AsynMove().execute(SPEED);
				      } else {
				        new AsynMove().execute(-SPEED);
				      }
				    
			    }
			    layout_left.setLayoutParams(layoutParams_left);
			    layout_right.setLayoutParams(layoutParams_right);
			  
		    return true;
		  }
		  @Override
		  public void onLongPress(MotionEvent e) {
			  Log.d(TAG, "onLongPress");
		  }
		  @Override
		  public void onShowPress(MotionEvent e) {
			  Log.d(TAG, "onShowPress");
		  }
		  @Override
		  public boolean onSingleTapUp(MotionEvent e) {
			  Log.d(TAG, "onSingleTapUp");

		    return false;
		  }
		  @Override
		  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
		      float velocityY) {
			  Log.d(TAG, "onFling");
		    return false;
		  }
	  }
	
	
	class AsynMove extends AsyncTask<Integer, Integer, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			int times = 0;
			if (max_width % Math.abs(params[0]) == 0)// ����
				times = max_width / Math.abs(params[0]);
			else
				times = max_width / Math.abs(params[0]) + 1;// ������

			for (int i = 0; i < times; i++) {
				publishProgress(params[0]);
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return null;
		}
		
		/**
		 * update UI
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			RelativeLayout.LayoutParams layoutParams_left = (RelativeLayout.LayoutParams) layout_left
					.getLayoutParams();
			RelativeLayout.LayoutParams layoutParams_right = (RelativeLayout.LayoutParams) layout_right
					.getLayoutParams();
			// ���ƶ�
			if (values[0 ] > 0) {
				layoutParams_right.leftMargin = Math.min(layoutParams_right.leftMargin
						+ values[0], max_width);
				layoutParams_left.leftMargin = Math.min(layoutParams_left.leftMargin
						+ values[0], 0);
				layoutParams_left.rightMargin =Math.max(layoutParams_left.rightMargin
						- values[0], window_width-max_width);
				
				Log.v(TAG, "\n**** Move to Right  *****\n" 
						+ "layout_right: " + layoutParams_right.leftMargin
						+ "\nlayout_left: " + layoutParams_left.leftMargin);
			} else {
				// ���ƶ�
				layoutParams_right.leftMargin = Math.max(layoutParams_right.leftMargin
						+ values[0], 0);
				layoutParams_left.leftMargin = Math.max(layoutParams_left.leftMargin
						+ values[0], -max_width);
				layoutParams_left.rightMargin = Math.min(layoutParams_left.rightMargin
						-values[0], max_width);
				Log.v(TAG, "\n**** Move to Left  *****\n" 
						+ "layout_right: " + layoutParams_right.leftMargin
						+ "\nlayout_left: " + layoutParams_left.leftMargin);
			}
			layout_right.setLayoutParams(layoutParams_right);
			layout_left.setLayoutParams(layoutParams_left);

		}

	}
	
	@Override
	protected void onDestroy() {
		// �˳�ʱ���ٶ�λ
		mLocClient.stop();
		// �رն�λͼ��
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		synchronized(this) {
            cancelTimer();
        }
		super.onDestroy();
	}
	private double distance = 0;
	private class UpdateDistanceTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... arg0) {
			Log.d(TAG,"UpdateDistanceTask,  doInBackground() ");
			// TODO Auto-generated method stub
			String result = null;
			DistanceInfo distanceInfo = mDistanceInfoDao
					.getById(RunningApplication.mRunningInfoId);
			if (distanceInfo != null && distanceInfo.getDistance() > 0) {
		            result = Utils.getValueWith2Suffix(distanceInfo.getDistance());
			    double distance = distanceInfo.getDistance() / 1000;
			    BigDecimal b = new BigDecimal(distance); 
		    	    double formatDistance = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		    	    Log.d(TAG,"updateDistance, formatDistance = " + formatDistance);
		    	    if((formatDistance > 0) && ((formatDistance * 100) % 100) == 0 && (!mSoundPlayer.isPlaying())) {
		    	        mUpdateDisplayHandler.sendMessage(Message.obtain(mUpdateDisplayHandler, PLAY_SOUND,formatDistance));
		    	    }
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			if(result != null) {
				onCurrentDistanceChanged(result);
			}
		}
	}
	
	private void onCurrentDistanceChanged(String result) {
		mRunDistanceTextView.setText(result);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this)
			        .setTitle("��ʾ")
			        .setMessage("ȷ��Ҫ�˳���?")
                    .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							if(mSportStatus == SportsManager.STATUS_PAUSED || mSportStatus == SportsManager.STATUS_RUNNING) {
								Toast.makeText(RunningMainActivity.this, "�����Ƚ����ܲ������˳���", Toast.LENGTH_SHORT).show();
								return;
							}else {
								RunningMainActivity.this.finish();
							}
						}
					})
					.setNegativeButton("��̨����", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							 ResolveInfo resolveInfo = getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).
									 addCategory(Intent.CATEGORY_HOME), 0);
							 ActivityInfo activityInfo = resolveInfo.activityInfo;
							 Intent intent = new Intent(Intent.ACTION_MAIN);
							 intent.addCategory(Intent.CATEGORY_LAUNCHER);
							 intent.setComponent(new ComponentName(activityInfo.packageName,activityInfo.name));
							 startActivitySafely(intent);
						}
					}).show();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
    private void startActivitySafely(Intent intent) {    
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    
	    try {    
	        startActivity(intent);    
	    } catch (ActivityNotFoundException e) {    
	        e.printStackTrace(); 
	    } catch (SecurityException e) {
		   e.printStackTrace();
	    }    
	}    
	
}
