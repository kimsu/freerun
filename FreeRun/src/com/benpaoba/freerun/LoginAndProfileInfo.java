package com.benpaoba.freerun;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.benpaoba.freerun.database.FreeRunContentProvider;
import com.benpaoba.freerun.database.RunRecordTable;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;


public class LoginAndProfileInfo extends Fragment {
	private final String TAG = "FreeRun";
	
	private boolean mLogState = false;
	
	private AlertDialog mAlert;
	public  static Tencent mTencent;
	
	//the simple User Info
	private LinearLayout mUserSummaryInfo;
	private ImageView  mUserIcon;
	private LinearLayout mLogin;
	private LinearLayout mLogout;
	private TextView mUserNickname;
	private TextView mEditInfo;
	private TextView mTotalDistance;
	private TextView mTotalTime;
	private TextView mTotalCalories;
	
	private long mTotalDistanceVal = 0;
	private long mTotalTimeVal = 0;
	private long mTotalCaloriesVal = 0;
	
	// total info item for preference
	 static final String NICKNAME = "nickname";
	 static final String TOTALDISTANCE = "total_distance";
	 static final String TOTALTIME = "total_time";
	 static final String TOTALCALORIES = "total_calories";
	 static final String WELCOMED = "welcomed";
	 
	//The best Record
	private	TextView mFastestSpeedMatch;
	private	TextView mLogestDistance;
	private	TextView mLongestTime;
	private	TextView mShortestTimeFive;
	private TextView mShortestTimeTen;
	private TextView mShortestTimeHalfMarathon;
	private TextView mShortestTimeFullMarathon;
	
	//the Run History Record
	private RelativeLayout mCheckHistoryRecord;
	private TextView mFrequencies;
	
	static final String HISTORYTIMES = "run_history_times";

	protected static final String ICON = "user_icon.png";
	//More setup Choice
//	private RelativeLayout mMoreSetup;
	
	//The best Record Item name, Used in Preference
	private final String SPEEDMATCH = "speed_match";
	private final String LONGESTDISTANCE = "longestDistance";
	private final String LONGESTTIME = "longestTime";
	private final String SHORTESTTIME_5 = "shortestTimefive";
	private final String SHORTESTTIME_10 = "mShortestTimeTen";
	private final String SHORTESTTIME_HM = "mShortestTimeHalfMarathon";
	private final String SHORTESTTIME_FM = "mShortestTimeFullMarathon";
	
	 
	
	private static SharedPreferences mLoginDataPreference;
	
	private UserInfo mInfo;

	private ImageView mUserIconDefault;
	private static String mPath;
	private AlertDialog.Builder mBuilder;
	
	private Cursor mCursor;
	
	private Context mContext;
	private boolean mIsLatestState = false;
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		mContext = getActivity();
		getLoaderManager().initLoader(22, null, new MyLoaderCallBacks());
		//mTencent = Tencent.createInstance(FreeRunConstants.APP_ID, getActivity().getApplicationContext());
		mLoginDataPreference = mContext.getSharedPreferences(FreeRunConstants.PROFILE_INFO_PREFERENCES,
				Context.MODE_PRIVATE);
		mPath = mContext.getCacheDir().getPath();
		mLogState = mLoginDataPreference.getBoolean(FreeRunConstants.LOGIN_STATUS, false);
		handleLogin();
//		updateHistoryRecordFromServer();
		onListenMyItemClick();
		
	}
	
//	private void updateHistoryRecordFromServer() {
//		AsyncHttpClient client = new AsyncHttpClient();
//    	RequestParams requestParams = new RequestParams();
//    	requestParams.put("index", SportsManager.QUERY_FROM_SERVER);
//    	requestParams.put("user_id", 1234);
//        mIsLatestState = mLoginDataPreference.getBoolean("is_latest", false);
//        Log.d(TAG,"updateHistoryRecoedFromServer, mIsLatestState = " + mIsLatestState);
//    	if(true) {
//    		client.post(SportsManager.SERVER_PATH, requestParams,
//    				new AsyncHttpResponseHandler() {
//    			        ProgressDialog dialog = new ProgressDialog(getActivity());
//    					@Override
//    					public void onStart() {
//    						// TODO Auto-generated method stub
//    						dialog.setTitle("提示信息");
//    		                dialog.setMessage("正在同步服务器的数据至本地。。。");
//    		                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//    		                dialog.setCancelable(true);
//    						dialog.show();
//    					}
//
//    					@Override
//    					public void onFailure(int statusCode,
//    							Header[] headers, byte[] responseBody,Throwable error) {
//    						// TODO Auto-generated method stub
//    						Log.d(TAG, "onFailure ==========" + statusCode + ", headers = " + headers);
//    						
//    						Message msg = new Message();
//    						msg.obj = responseBody;
//    						msg.what = 3;
//    						mHandler.sendMessage(msg);
//    						dialog.dismiss();
//    					}
//
//    					@Override
//    					public void onSuccess(int statusCode,
//    							Header[] headers, byte[] responseBody) {
//    						// TODO Auto-generated method stub
//    						Log.d(TAG, "onSuccess ==========" + statusCode + ", headers = " + headers);
//    						Message msg = new Message();
//    						msg.obj = responseBody;
//    						msg.what = 2;
//    						mHandler.sendMessage(msg);
//    						mLoginDataPreference.edit().putBoolean("is_latest", true).commit();
//    						dialog.dismiss();
//    					}
//        	});
//        }
//	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final View view = inflater.inflate(R.layout.user_profile_info, container, false);
		// 初始化视图
	    initViews(view);
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		//handle user profile info
	}
	
	
	private void initViews(View view) {
		
		mUserSummaryInfo = (LinearLayout) view.findViewById(R.id.user_summary_info);
		mUserIcon = (ImageView)view.findViewById(R.id.img_user_avatar);
		mUserIconDefault = (ImageView) view.findViewById(R.id.img_user_avatar_default);
		mLogin = (LinearLayout)view.findViewById(R.id.login);
		mLogout = (LinearLayout)view.findViewById(R.id.logout);
		mEditInfo = (TextView)view.findViewById(R.id.info_edit);
		mUserNickname = (TextView)view.findViewById(R.id.pro_nickname);
		mTotalDistance = (TextView)view.findViewById(R.id.total_distance);
		mTotalTime = (TextView)view.findViewById(R.id.total_time);
		mTotalCalories = (TextView)view.findViewById(R.id.total_calories);

		//handle the bestest history record. Start.
		mFastestSpeedMatch = (TextView)view.findViewById(R.id.fastest_speed_match);
		mLogestDistance = (TextView)view.findViewById(R.id.longest_distance);
		mLongestTime = (TextView)view.findViewById(R.id.longest_time);
		mShortestTimeFive = (TextView)view.findViewById(R.id.shortest_time_five);
		mShortestTimeTen = (TextView)view.findViewById(R.id.shortest_time_ten);
		mShortestTimeHalfMarathon = (TextView)view.findViewById(R.id.shortest_time_half_marathon);
		//mShortestTimeFullMarathon = (TextView)view.findViewById(R.id.shortest_time_full_marathon);
		
		//run history record
		mFrequencies = (TextView)view.findViewById(R.id.record_history_times);
		mCheckHistoryRecord = (RelativeLayout) view.findViewById(R.id.run_history);
		
		//More setup
//		mMoreSetup = (RelativeLayout) view.findViewById(R.id.more_setUp);
		
				

	}
	


private void  onListenMyItemClick() {
		
		// show Login dialog
		mUserSummaryInfo.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
				// TODO Auto-generated method stub
			//Login Dialog 
			mBuilder = new AlertDialog.Builder(mContext);
			if(false == mLogState) {
				mBuilder.setMessage(getActivity().getResources().getString(R.string.ask_for_login))
				   .setCancelable(false)
				   .setPositiveButton(getActivity().getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int id) {
				        	Log.d(TAG, "\n======  LONIN START  ============");
//					            	mLogState = true;
//					    			mLoginDataPreference
//					            	.edit()
//					            		.putBoolean(LOGSTATE, true)
//					            			.commit();
					    			login();
					           }
					     })
					.setNegativeButton(getActivity().getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
							
							}
						}).show();
			}
			
		}
		});
		
		//Enter Edit user Info Activity
		mEditInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction(FreeRunConstants.ACTION_EDIT_USER_INFO);
				startActivity(intent);
			}
		});
		
		//Enter check history record Activity
		mCheckHistoryRecord.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG,"onClick(), ");
				Intent intent = new Intent();
				intent.setAction(FreeRunConstants.ACTION_CHECK_RECORD);
				startActivity(intent);
			}
		});
		
	}
	public  void handleLogin()
	{ 
		//Log.e(TAG, "handleLogin(), mLogState = " + mLogState, new Exception());
		
		if(mLogState) {
			Log.d(TAG, "Login .......");
			//mUserIcon.setImageResource(R.id.user_icon);
			mLogout.setVisibility(View.GONE);
			mLogin.setVisibility(View.VISIBLE);
			//user simple Info
			{
				mUserIconDefault.setVisibility(View.GONE);
				mUserIcon.setVisibility(View.VISIBLE);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 1;
				Bitmap bm = BitmapFactory.decodeFile(mPath + "/" + ICON, options);
				mUserIcon.setImageBitmap(bm);
			}
			mUserNickname.setText(
					mLoginDataPreference.getString(NICKNAME, "Vistor"));
			mTotalDistance.setText(
					mLoginDataPreference.getString(TOTALDISTANCE, "none"));
			mTotalTime.setText(
					mLoginDataPreference.getString(TOTALTIME, "none"));
			mTotalCalories.setText(
					mLoginDataPreference.getString(TOTALCALORIES, "none"));
			
			//the best Record
//			mFastestSpeedMatch.setText(
//					mLoginDataPreference.getString(SPEEDMATCH, "nothing"));
//			mLogestDistance.setText(
//					mLoginDataPreference.getString(LONGESTDISTANCE, "nothing"));
//			mLongestTime.setText(
//					mLoginDataPreference.getString(LONGESTTIME, "nothing"));
//			mShortestTimeFive.setText(
//					mLoginDataPreference.getString(SHORTESTTIME_5, "nothing"));
//			mShortestTimeTen.setText(
//					mLoginDataPreference.getString(SHORTESTTIME_10, "nothing"));
//			mShortestTimeHalfMarathon.setText(
//					mLoginDataPreference.getString(SHORTESTTIME_HM, "nothing"));
//			mShortestTimeFullMarathon.setText(
//					mLoginDataPreference.getString(SHORTESTTIME_FM, "nothing"));
//		
			//the history run times
			Log.d(TAG,"mFrencies : " + mLoginDataPreference.getInt(HISTORYTIMES, 0));
			mFrequencies.setText(
					mLoginDataPreference.getInt(HISTORYTIMES, 0) + "次");
			
			//the best record update
			updateTheBestRecord();
			
		} else {
			Log.d(TAG, "Logout......");
			mLogin.setVisibility(View.GONE);
			mLogout.setVisibility(View.VISIBLE);
			mUserIcon.setVisibility(View.GONE);
			mUserIconDefault.setVisibility(View.VISIBLE);
			
		}
		
	}
	
	public void updateTheBestRecord() {
		
		// update the best match velocity
		String[]  projectionId = {RunRecordTable.COLUMN_USEDTIME, 
				RunRecordTable.COLUMN_DISTANCE, RunRecordTable.COLUMN_COSTENERGY};
		mCursor = mContext.getContentResolver().query(FreeRunContentProvider.CONTENT_URI, 
						projectionId, 
						null,
						null, 
						RunRecordTable.COLUMN_ID + " DESC");
		if(null != mCursor && mCursor.getCount() >= 1) {
			long newUsedTime = 0;
			long newDistance = 0;
			int timeColumn = 0;
			int distanceColumn = 0;
//			int energyColumn = 0;

			Log.d(TAG, "Cursor Position: " + mCursor.getPosition() 
					+ "\n the fastest match velocity");
			if(mCursor.moveToFirst()) {
				timeColumn = mCursor.getColumnIndex(RunRecordTable.COLUMN_USEDTIME);
				newUsedTime = mCursor.getLong( timeColumn );
				distanceColumn = mCursor.getColumnIndex(RunRecordTable.COLUMN_DISTANCE);
				newDistance = mCursor.getLong(distanceColumn);
//				energyColumn = mCursor.getColumnIndex(RunRecordTable.COLUMN_COSTENERGY);
				
				//update the run frequencies
				Log.d(TAG,"mCursor.getCount= " + mCursor.getCount());
				mFrequencies.setText(mCursor.getCount() + "次");
					

				 
				do {
					mTotalTimeVal = mTotalTimeVal + mCursor.getLong( timeColumn );
					mTotalDistanceVal = mTotalDistanceVal +  mCursor.getLong(distanceColumn);
//					mTotalCaloriesVal = mTotalCaloriesVal + mCursor.getLong(energyColumn);
				}while(mCursor.moveToNext());
				mTotalTime.setText(TimeFormatHelper.formatTime(mTotalTimeVal));
				mTotalDistance.setText(mTotalDistanceVal/1000 + "KM");
//				mTotalCalories.setText( mTotalCaloriesVal + "J");
			}
			
			Log.d(TAG, "newUsedTime: " + newUsedTime + "; newDistance: " + newDistance + ";");
			float bestVel = mLoginDataPreference.getFloat(SPEEDMATCH, Float.MAX_VALUE);
			float newBestVel = (float)(newUsedTime*1000)/(newDistance*60);
			if(newDistance != 0 && bestVel > newBestVel) {
				Log.d(TAG, "new fastestSpeedmatch");
				mFastestSpeedMatch.setText((newUsedTime*1000)/(newDistance*60) + "'" 
						+ (newUsedTime*1000)%newDistance + "\"");
				mLoginDataPreference.edit().putFloat(SPEEDMATCH, newBestVel).commit();
			}else if( bestVel != Float.MAX_VALUE && bestVel != 0) {
				int min = (int)Math.floor(bestVel);
				int sec = (int)Math.floor((bestVel-min)*60);
				Log.d(TAG, ": " + (bestVel - min)*60 + " : " +sec);
				mFastestSpeedMatch.setText(min + "'" + sec + "\"" );
			}
		}
		
		
		
		//update the longest distance;
		String[] projectionDistance = {RunRecordTable.COLUMN_DISTANCE};
		mCursor = mContext.getContentResolver().query(FreeRunContentProvider.CONTENT_URI, 
				projectionDistance,
				null,
				null, 
				RunRecordTable.COLUMN_ID + " DESC");
		if(null != mCursor && mCursor.getCount() >= 1) {
			Log.d(TAG, "the longest distance !");
			mCursor.moveToNext();
			long newLongestDistanceVal = mCursor.getLong(
					mCursor.getColumnIndex(
							RunRecordTable.COLUMN_DISTANCE));
			float longestDistanceVal = mLoginDataPreference.getFloat(LONGESTDISTANCE, 0);
			if(longestDistanceVal < newLongestDistanceVal ) {
				mLogestDistance.setText((float)newLongestDistanceVal/1000 + "KM");
				mLoginDataPreference.edit().putFloat(LONGESTDISTANCE, newLongestDistanceVal).commit();
			} else if(longestDistanceVal != 0) {
				mLogestDistance.setText((float)longestDistanceVal/1000 + "KM");
			}
		}
		
		//update the longest time;
		String[] projectionTime = {RunRecordTable.COLUMN_USEDTIME};
		mCursor = mContext.getContentResolver().query(FreeRunContentProvider.CONTENT_URI, 
				projectionTime,
				null,
				null, 
				RunRecordTable.COLUMN_ID + " DESC");
		if(null != mCursor && mCursor.getCount() >= 1) {
			Log.d(TAG, "the longest used time !");
			mCursor.moveToNext();
			long newLongestUseTimeVal = mCursor.getLong(
					mCursor.getColumnIndex(
							RunRecordTable.COLUMN_USEDTIME));
			long longestUsedTimeVal= mLoginDataPreference.getLong(LONGESTTIME, 0);
			if(longestUsedTimeVal < newLongestUseTimeVal ) { 
				mLongestTime.setText(TimeFormatHelper.formatTime(newLongestUseTimeVal));
				mLoginDataPreference.edit().putLong(LONGESTTIME, newLongestUseTimeVal).commit();
			} else if (longestUsedTimeVal != 0) {
				mLongestTime.setText(TimeFormatHelper.formatTime(longestUsedTimeVal));
			}
		}
		
		//update the  5Km   best record 
		String[] projection5KmTime = {RunRecordTable.COLUMN_FIVE};
		mCursor = mContext.getContentResolver().query(FreeRunContentProvider.CONTENT_URI, 
				projection5KmTime,
				null,
				null, 
				RunRecordTable.COLUMN_ID + " DESC");
		if(null != mCursor && mCursor.getCount() >= 1) {
			mCursor.moveToNext();
			long newfiveKmShortestTimeVal = mCursor.getLong(
					mCursor.getColumnIndex(
							RunRecordTable.COLUMN_FIVE));
			long fiveKmShortestTimeVal= mLoginDataPreference.getLong(SHORTESTTIME_5, Long.MAX_VALUE);
			Log.d(TAG, "the 5KM shortest time !" 
			+ "\n newFiveKmShortestValue = " + newfiveKmShortestTimeVal 
			+ "; oldFiveKmShortestValue = " + fiveKmShortestTimeVal);
			if(fiveKmShortestTimeVal > newfiveKmShortestTimeVal) { 
				mShortestTimeFive.setText(TimeFormatHelper.formatTime(newfiveKmShortestTimeVal));
				mLoginDataPreference.edit().putLong(SHORTESTTIME_5, newfiveKmShortestTimeVal).commit();
			} else if(fiveKmShortestTimeVal != Long.MAX_VALUE && fiveKmShortestTimeVal != 0) {
				mShortestTimeFive.setText(TimeFormatHelper.formatTime(fiveKmShortestTimeVal));
			}
		}
		
		//update the 10Km best record
		String[] projection10KmTime = {RunRecordTable.COLUMN_TEN};
		mCursor = mContext.getContentResolver().query(FreeRunContentProvider.CONTENT_URI, 
				projection10KmTime,
				null,
				null, 
				RunRecordTable.COLUMN_ID + " DESC");
		if(null != mCursor && mCursor.getCount() >= 1) {
			Log.d(TAG, "the 10KM shortest time !");
			mCursor.moveToNext();
			long newTenKmShortestTimeVal = mCursor.getLong(
					mCursor.getColumnIndex(
							RunRecordTable.COLUMN_TEN));
			long tenKmShortestTimeVal= mLoginDataPreference.getLong(SHORTESTTIME_10, Long.MAX_VALUE);
			if(newTenKmShortestTimeVal != 0 && tenKmShortestTimeVal > newTenKmShortestTimeVal) { 
				mShortestTimeTen.setText(TimeFormatHelper.formatTime(newTenKmShortestTimeVal));
				mLoginDataPreference.edit().putLong(SHORTESTTIME_10, newTenKmShortestTimeVal).commit();
			}else if(tenKmShortestTimeVal != Long.MAX_VALUE && tenKmShortestTimeVal != 0) {
				mShortestTimeTen.setText(TimeFormatHelper.formatTime(tenKmShortestTimeVal));
			}
		}
		
		//update the half Marathon best record
		
		String[] projectionHalfMarTime = {RunRecordTable.COLUMN_HALF_MAR};
		mCursor = mContext.getContentResolver().query(FreeRunContentProvider.CONTENT_URI, 
				projectionHalfMarTime,
				null,
				null, 
				RunRecordTable.COLUMN_ID + " DESC");
		if(null != mCursor && mCursor.getCount() >= 1) {
			Log.d(TAG, "the half Marathon shortest time !");
			mCursor.moveToNext();
			long newHalfMarShortestTimeVal = mCursor.getLong(
					mCursor.getColumnIndex(
							RunRecordTable.COLUMN_HALF_MAR));
			long halfMarShortestTimeVal= mLoginDataPreference.getLong(SHORTESTTIME_HM, 0);
			if(newHalfMarShortestTimeVal != 0 && halfMarShortestTimeVal > newHalfMarShortestTimeVal) { 
				mShortestTimeHalfMarathon.setText(TimeFormatHelper.formatTime(newHalfMarShortestTimeVal));
				mLoginDataPreference.edit().putLong(SHORTESTTIME_HM, newHalfMarShortestTimeVal).commit();
			} else if(halfMarShortestTimeVal != 0 && halfMarShortestTimeVal != Long.MAX_VALUE) {
				mShortestTimeHalfMarathon.setText(TimeFormatHelper.formatTime(halfMarShortestTimeVal));
			}
		}
		
		//update the full Marathon best record
		String[] projectionfullMarTime = {RunRecordTable.COLUMN_FULL_MAR};
		mCursor = mContext.getContentResolver().query(FreeRunContentProvider.CONTENT_URI, 
				projectionfullMarTime,
				null,
				null, 
				RunRecordTable.COLUMN_ID + " DESC");
		if(null != mCursor && mCursor.getCount() >= 1) {
			Log.d(TAG, "the full Marathon shortest time !");
			mCursor.moveToNext();
			long newFullMarShortestTimeVal = mCursor.getLong(
					mCursor.getColumnIndex(
							RunRecordTable.COLUMN_FULL_MAR));
			long fullMarShortestTimeVal= mLoginDataPreference.getLong(SHORTESTTIME_HM, Long.MAX_VALUE);
			if(newFullMarShortestTimeVal != 0 && fullMarShortestTimeVal > newFullMarShortestTimeVal) { 
				mShortestTimeFullMarathon.setText(TimeFormatHelper.formatTime(newFullMarShortestTimeVal));
				mLoginDataPreference.edit().putLong(SHORTESTTIME_FM, newFullMarShortestTimeVal).commit();
			} else if (fullMarShortestTimeVal != Long.MAX_VALUE && fullMarShortestTimeVal != 0) {
				mShortestTimeFullMarathon.setText(TimeFormatHelper.formatTime(fullMarShortestTimeVal));
			}
		}
	}
	
	
	
	/**
	 *  调用QQ登录接口
	 * **/
	
	public void login()
	{
		mTencent = Tencent.createInstance(FreeRunConstants.APP_ID, mContext);
		Log.d(TAG, "Login(): mTencent.isSessionVaild = " + mTencent.isSessionValid() + 
				   "\n isReady = " + mTencent.isReady());
		if (!mTencent.isSessionValid())
		{
			mTencent.login(getActivity(), "all", loginListener);
		}
	} 
	
	/**
	 * 调用QQ注销接口
	 * */
//	public void logout()
//	{
//		mTencent.logout(this);
//	}
	
	private void updateUserInfo() {
		Log.d(TAG, "LoginAndProfileInfo: updateUserInfo() " + " isSessionValid = " + mTencent.isSessionValid());
		if (mTencent != null && mTencent.isSessionValid()) {
			IUiListener listener = new IUiListener() {
				@Override
				public void onError(UiError e) {

				}

				@Override
				public void onComplete(final Object response) {
					Log.d(TAG, "LoginAndProfileInfo: UpdateUserInfo: updateUserInfo: onComplete()");
					Message msg = new Message();
					msg.obj = response;
					msg.what = 0;
					mHandler.sendMessage(msg);
					new Thread(){
						@Override
						public void run() {
							JSONObject json = (JSONObject)response;
							if(json.has("figureurl")){
								Bitmap bitmap = null;
								try {
									bitmap = Utils.getbitmap(json.getString("figureurl_qq_2"));
								} catch (JSONException e) {

								}
								Message msg = new Message();
								msg.obj = bitmap;
								msg.what = 1;
								mHandler.sendMessage(msg);
							}
						}

					}.start();
				}

				@Override
				public void onCancel() {

				}
			};
			
			mInfo = new UserInfo(mContext, mTencent.getQQToken());
			Log.d(TAG,"begin to getListener");
			mInfo.getUserInfo(listener);
			Log.d(TAG,"After listener");
			//show the user personal mLogin info.

		} else {
			Log.d(TAG, "isSessionVaild =false, Don't Login." );
			mUserNickname.setText("");
			mUserNickname.setVisibility(android.view.View.GONE);
			mUserIcon.setVisibility(android.view.View.GONE);
		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				JSONObject response = (JSONObject) msg.obj;
				if (response.has("nickname")) {
					try {
						mUserNickname.setText(response.getString("nickname"));
						mLoginDataPreference
						.edit()
						.putString(NICKNAME, response.getString("nickname"))
						.commit();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}else if(msg.what == 1){
				Bitmap bitmap = (Bitmap)msg.obj;
				mUserIcon.setImageBitmap(bitmap);
				
				if(bitmap == null ) {
					Log.e(TAG, "bitmap is null");
				}
				try {
					saveBitmapToFile(bitmap, mPath + "/" + ICON);
				}catch(IOException e) {
					Log.e(TAG, "==>Error: " + e.getMessage());
					e.printStackTrace();
				}
				
				//Finish to download the user icon Image, then update the obtained info.
				mLogState = true;
				mLoginDataPreference
	        	.edit()
	        		.putBoolean(FreeRunConstants.LOGIN_STATUS, true)
	        			.commit();
	        	handleLogin();
			}else if(msg.what == 2) {
				byte[] data = (byte[]) msg.obj;
				String str = new String(data);
				try {
					JSONObject jsonObject = new JSONObject(str);
					JSONArray jsonArray = jsonObject.getJSONArray("detail_list");
					Log.d(TAG,"jsonArray length : " + jsonArray.length());
					mFrequencies.setText(jsonArray.length() + "次");
					saveJsonToFile(str);
					saveJsontoDatabase(jsonObject);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	};
	
	
	private void saveJsonToFile(String str) {
		File file = getActivity().getFilesDir();
		Log.d(TAG,"saveJsonToFile, file = " + file.getAbsolutePath());
		FileOutputStream out;
		try {
			File saveFile = new File(file, SportsManager.SPORTS_JSON_FILE);
			if(saveFile.exists()) {
				saveFile.delete();
			}
			saveFile.createNewFile();
			out = new FileOutputStream(saveFile);
			out.write(str.getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void saveJsontoDatabase(JSONObject jsonObject) {
		try {
			List<SportsDetails> details = new ArrayList<SportsDetails>();
			JSONArray jsonArray = jsonObject.getJSONArray("detail_list");
			for(int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				SportsDetails detail = new SportsDetails.Builder(object.getString("userId"))
				        .setSportsDate(object.getLong("date"))
				        .setUsedTime(object.getLong("usedTime"))
				        .setDistance(object.getDouble("distance"))
				        .setDataFilePath(object.getString("dataFilePath"))
				        .build();
				Log.d(TAG," i = " + i + ", " + detail.getUserId() + ", " + detail.getDate() + ", " + detail.getUsedTime() +
						", " + detail.getDataFilePath());
				details.add(detail);
			}
			Cursor cursor = getActivity().getContentResolver().query(FreeRunContentProvider.CONTENT_URI, null, null, null, null);
			
			Log.d(TAG,"cursor.getCount() = " + cursor.getCount());
			if(details.size() != cursor.getCount()) {
			    new DownloadTask(details).start();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	class DownloadTask extends Thread{
		private List<SportsDetails> details;
		
		public DownloadTask(List<SportsDetails> details) {
			this.details = details;
		}

		@Override
		public void run() {
			
			// TODO Auto-generated method stub
			for(SportsDetails detail : details) {
				
				File downloadFile = Utils.getDownloadFile(detail.getDataFilePath());
				Log.d(TAG,"DownloadTask, run(). details = " + details + "downloadFile.getPath = " + 
				downloadFile.getAbsolutePath());
			    ContentValues values = new ContentValues();
			    values.put(RunRecordTable.COLUMN_DATE,detail.getDate());
			    values.put(RunRecordTable.COLUMN_USEDTIME,detail.getUsedTime());
			    values.put(RunRecordTable.COLUMN_DISTANCE,detail.getDistance());
			    values.put(RunRecordTable.COLUMN_FILE_LOCATION, downloadFile.getAbsolutePath());
			    getActivity().getContentResolver().insert(FreeRunContentProvider.CONTENT_URI, values);
			    
			}
		}
		
	}
	public void onResume() {
		super.onResume();
		//user simple Info
		if(mLogState)
		{
			mUserIconDefault.setVisibility(View.GONE);
			mUserIcon.setVisibility(View.VISIBLE);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			Bitmap bm = BitmapFactory.decodeFile(mPath + "/" + ICON, options);
			mUserIcon.setImageBitmap(bm);
		}
		
	}
	
	
	
	
	
	/**
	 * 应用调用Andriod_SDK接口时，如果要成功接收到回调，
	 * 需要在调用接口的Activity的onActivityResult方法中增加如下代码：
	 * 
	 * **/
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    Log.d(TAG, "onActivityResult: requestCode= " + requestCode  + " resultCode=" + resultCode);
	    if(requestCode == Constants.REQUEST_API) {
	        if(resultCode == Constants.RESULT_LOGIN) {
	            Tencent.handleResultData(data, loginListener);
	            Log.d(TAG, "onActivityResult handle logindata");
	        }
	    } else if (requestCode == Constants.REQUEST_APPBAR) { //app内应用吧登录
	    	if (resultCode == Constants.RESULT_LOGIN) {
	    		updateUserInfo();
	            //updateLoginButton();
	            Utils.showResultDialog(mContext, data.getStringExtra(Constants.LOGIN_INFO), "登录成功");
	    		
	    	}
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}
	
	// 调用SDK已经封装好的接口时，例如：登录、快速支付登录、应用分享、应用邀请等接口，需传入该回调的实例。
	
	public static void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                    && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
            }
        } catch(Exception e) {
        }
    }	
	
	IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject values) {
        	Log.d(TAG, "BaseUiListener: doComplete(), SDKQQAgentPref, AuthorSwitch_SDK:" + SystemClock.elapsedRealtime());
            initOpenidAndToken(values);
            updateUserInfo();
            //updateLoginButton();
        }
    };

	private class BaseUiListener implements IUiListener {
		
		@Override
		public void onComplete(Object response) {
            Log.d(TAG, "BaseUiListener: onComplete()");
			if (null == response) {
                Utils.showResultDialog(mContext, "返回为空", "登录失败");
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                Utils.showResultDialog(mContext, "返回为空", "登录失败");
                return;
            }
			//Utils.showResultDialog(mContext, response.toString(), "登录成功");
            // 有奖分享处理
            // handlePrizeShare();
            if(!mLoginDataPreference.getBoolean(WELCOMED, false)) {
            	MyLoginPromptDialog dialongPrompt = new MyLoginPromptDialog();
        		dialongPrompt.show(getFragmentManager(), "MyLoginPromptDialog");
            }
            
			doComplete((JSONObject)response);
		}

		protected void doComplete(JSONObject values) {

		}

		@Override
		public void onError(UiError e) {
			Utils.toastMessage(getActivity(), "onError: " + e.errorDetail);
			//Utils.dismissDialog();
		}

		@Override
		public void onCancel() {
			Utils.toastMessage(getActivity(), "onCancel: ");
			//Utils.dismissDialog();
		}
	}
	
	/**
     * Save Bitmap to a file.保存图片到SD卡。
     * 
     * @param bitmap
     * @param file
     * @return error message if the saving is failed. null if the saving is
     *         successful.
     * @throws IOException
     */
    public  void saveBitmapToFile(Bitmap bitmap, String _file)
            throws IOException {
    	Log.d(TAG, "LoginAndProfileInfo: saveBitmapToFile()");
        BufferedOutputStream os = null;
        try {
            File file = new File(_file);
            // String _filePath_file.replace(File.separatorChar +
            // file.getName(), "");
            int end = _file.lastIndexOf(File.separator);
            String _filePath = _file.substring(0, end);
            File filePath = new File(_filePath);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            file.createNewFile();
            os = new BufferedOutputStream(new FileOutputStream(file));
            if(bitmap != null) {
            	bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            }else {
            	Log.e(TAG, "fail to obtain the user Icon!!!!");
            }
            
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }
    
    private class MyLoaderCallBacks implements LoaderManager.LoaderCallbacks<Cursor> {

    	//you attempt to access  a loaderfor example, through initLoader()), 
    	//it checks to see whether the loader specified by the ID exists.
    	//if it does't, it triggers the LoaderManager.LoaderCallbacks<Cursor> 
    	// onCreateLoader method
    	//initLoader invoke this method if the specified id loader doen't exist
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			// TODO Auto-generated method stub
			Log.d(TAG, "Create Loader ID: " + id);
			String loaderCreateProjection[] = {RunRecordTable.COLUMN_DISTANCE, 
					               RunRecordTable.COLUMN_FIVE,
					               RunRecordTable.COLUMN_TEN,
					               RunRecordTable.COLUMN_HALF_MAR,
					               RunRecordTable.COLUMN_FULL_MAR,
					               RunRecordTable.COLUMN_USEDTIME};
			CursorLoader cursorLoader = new CursorLoader(mContext,
						FreeRunContentProvider.CONTENT_URI, 
						loaderCreateProjection,
						null, 
						null,
						null);
			return cursorLoader;
		}

		// If at the point of this call the caller is in its started state,
		//	and the requested loader already exists and has generated its data,
		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			// TODO Auto-generated method stub
			Log.d(TAG, "LoaderManager: onLoadFinished()");
		}
		
		//This callback lets you find out when the data is about to be released 
		//so you can remove your reference to it.  
		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
    public static  class MyLoginPromptDialog extends DialogFragment {
    	private String TAG = "MyLoginPromptDialog";
    	private View view;
    	
    	@Override
    	public void onCreate(Bundle savedInstanceState) {
    		// TODO Auto-generated method stub
    		super.onCreate(savedInstanceState);
    		setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
    	}
    	
    	@Override
    	public View onCreateView(LayoutInflater inflater, ViewGroup container,
    			Bundle savedInstanceState) {
    		// TODO Auto-generated method stub
    		 view = 
    				inflater.inflate(R.layout.fragment_login_prompt, container);
    		return view;
    	}
    	@Override
    	public void onActivityCreated(Bundle savedInstanceState) {
    		// TODO Auto-generated method stub
    		getDialog().setTitle("开始使用吧！");
    		
    		
    		Button mEditInfo = (Button) view.findViewById(R.id.editInfo);
    		mEditInfo.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent();
					intent.setComponent(new ComponentName(
							getActivity(), 
							EditUserInfo.class));
					startActivity(intent);
					getDialog().dismiss();
					
				}
			});
    		
    		Button nothingDone = (Button) view.findViewById(R.id.thanks);
    		nothingDone.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.d(TAG, "MyLoginPromtDialog: dismiss()");
					mLoginDataPreference.edit().putBoolean(WELCOMED, true).commit();
					getDialog().dismiss();
				}
			});
    		
    		super.onActivityCreated(savedInstanceState);
    	}
    }
	
	
}
