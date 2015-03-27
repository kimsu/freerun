package com.benpaoba.freerun;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class LocationService extends Service {

    public static final String FILE_NAME = "log.txt";
    private static final String TAG = "LocationService";
    public static final int PLAYBACKSERVICE_STATUS = 1;
    private volatile int mSportStatus;
    LocationClient mLocClient;
    private Object lock = new Object();
    private GpsLocation mPrevGpsLocation;       //定位数据
    private GpsLocation mCurrentGpsLocation;
    private double mOldDistance = 0;
    private MyLocationListenner mLocationListener;
    private volatile int discard = 1;
    
    private DistanceInfoDao mDistanceInfoDao;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private Notification mStatus;
    private RemoteViews mRemoteViews;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"LocationService, onCreate()");
        
        //Notice that when service is created, we have been running and  
        mSportStatus = SportsManager.STATUS_RUNNING;
        mDistanceInfoDao = new DistanceInfoDao(this);
        mLocationListener = new MyLocationListenner();
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(mLocationListener);
        
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.layout_notify_info);
        //定位参数设置
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll"); //返回的定位结果是百度经纬度，默认值gcj02
        option.setAddrType("all");    //返回的定位结果包含地址信息
        option.setScanSpan(1000);     //设置发起定位请求的间隔时间为5000ms
        option.setOpenGps(true);
        mLocClient.setLocOption(option);
        mLocClient.start();
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(SportsManager.STATUS_ACTION);
        registerReceiver(mIntentReceiver, commandFilter);
    }

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
    	mStatus = new Notification();
    	mStatus.contentView = mRemoteViews;
    	mStatus.flags |= Notification.FLAG_ONGOING_EVENT;
    	mStatus.icon = R.drawable.ic_launcher;
    	mStatus.contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this,RunningMainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
        startForeground(PLAYBACKSERVICE_STATUS, mStatus);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mLocClient) {
            mLocClient.stop();
        }
        stopForeground(false);
        unregisterReceiver(mIntentReceiver);
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            Log.d(TAG,"onReceive(), action = " + action + ", cmd = " + cmd);
            if(action.equals(SportsManager.STATUS_ACTION)) {
            	if(cmd.equals(SportsManager.CMD_START)) {
            		mSportStatus = SportsManager.STATUS_RUNNING;
            	} else if(cmd.equals(SportsManager.CMD_PAUSE)) {
            		mSportStatus = SportsManager.STATUS_PAUSED;
            	} else if(cmd.equals(SportsManager.CMD_CONTINUE)) {
            		mSportStatus = SportsManager.STATUS_RUNNING;
            	}else if(cmd.equals(SportsManager.CMD_FINISH)) {
            		mSportStatus = SportsManager.STATUS_READY;
            	}
            }
        }
    };
    
    private class Task implements Callable<String>{

        private BDLocation location;
        public Task(BDLocation location){
            this.location = location;
        }

        /**
         * 检测是否在原地不动
         *
         * @param distance
         * @return
         */
        private boolean noMove(float distance){
            if (distance < 0.01) {
                return true;
            }
            return false;
        }

        /**
         * 检测是否在正确的移动
         *
         * @param distance
         * @return
         */
        private boolean checkProperMove(float distance){
            if(distance <= 0.1 * discard){
                return true;
            }else{
                return false;
            }
        }

        /**
         * 检测获取的数据是否是正常的
         *
         * @param location
         * @return
         */
        private boolean checkProperLocation(BDLocation location){
            if (location != null && location.getLatitude() != 0 && location.getLongitude() != 0){
                return true;
            }
            return false;
        }

        @Override
        public String call() throws Exception {
            synchronized (lock) {
                if (!checkProperLocation(location)){
                    discard++;
                    return null;
                }
               Log.d(TAG,"LocationService, call(), RunningApplication.mRunningInfoId = " +
                        RunningApplication.mRunningInfoId + 
                        ", status = " + mSportStatus);
                if (RunningApplication.mRunningInfoId != -1 && mSportStatus == SportsManager.STATUS_RUNNING) {
                    //DistanceInfo mDistanceInfo = mDistanceInfoDao.getById(RunningApplication.mRunningInfoId);
                    //LogUtil.info("LocationService, mDistanceInfo = " + mDistanceInfo);
                    //if(mDistanceInfo != null) {
                    	mCurrentGpsLocation = new GpsLocation(location.getLatitude(), location.getLongitude());
                        double addedDistance = 0.0f;
                        DistanceComputeInterface distanceComputeInterface = DistanceComputeImpl.getInstance();
                        if(mPrevGpsLocation != null) {
                        	addedDistance = (float) distanceComputeInterface.getShortDistance(mPrevGpsLocation.lat,mPrevGpsLocation.lng,mCurrentGpsLocation.lat,mCurrentGpsLocation.lng);
                        }
						//LogUtil.info("LocationService(), distance = " + addedDistance + ", oldDistance = " + mDistanceInfo.getDistance());
						if (addedDistance > 0) {
							//float oldDistance = mDistanceInfo.getDistance();
							//mDistanceInfo
							//		.setDistance(addedDistance + oldDistance); //拿到数据库原始距离值，加上当前值
																				
							//mDistanceInfo.setLongitude(mCurrentGpsLocation.lng); //经度
							//mDistanceInfo.setLatitude(mCurrentGpsLocation.lat); //纬度

							//mDistanceInfoDao.updateDistance(mDistanceInfo);
							
							discard = 1;
						}
                        mPrevGpsLocation = mCurrentGpsLocation;
                    //}
                }
                return null;
            }
        }
    }

    /**
     * The BaiduMap SDK listener
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
        	LogUtil.info("MyLocationListener, onReceiveLocation, latitude :　" + location.getLatitude() + ", longitude : " + 
                    location.getLongitude());
                mExecutor.submit(new Task(location));

                RunningApplication.mLongtitude = location.getLongitude();
                RunningApplication.mLatitude = location.getLatitude();
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null){
                return ;
            }
        }
    }

}
