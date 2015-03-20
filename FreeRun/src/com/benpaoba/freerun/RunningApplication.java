package com.benpaoba.freerun;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

public class RunningApplication extends Application {

    public static int mRunningInfoId = -1;
    public static double mLongtitude = 0d;
    public static double mLatitude = 0d;
	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(this);
	}

}