package com.benpaoba.freerun;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ComponentName;
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
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

@SuppressLint("HandlerLeak")
public class LoginAndProfileInfo extends Activity {
	private final String TAG = "FreeRun";
	
	private boolean logState = false;
	
	private AlertDialog alert;
	public  static Tencent mTencent;
	
	//the simple User Info
	private LinearLayout userSummaryInfo;
	private ImageView  userIcon;
	private LinearLayout login;
	private LinearLayout logout;
	private TextView userNickname;
	private TextView editInfo;
	private TextView totalDistance;
	private TextView totalTime;
	private TextView totalCalories;
	
	// total info item for preference
	 static final String LOGSTATE = "log_state";
	 static final String NICKNAME = "nickname";
	 static final String TOTALDISTANCE = "total_distance";
	 static final String TOTALTIME = "total_time";
	 static final String TOTALCALORIES = "total_calories";
	 
	 
	
	//The best Record
	private	TextView fastestSpeedMatch;
	private	TextView longestDistance;
	private	TextView longestTime;
	private	TextView shortestTimeFive;
	private TextView shortestTimeTen;
	private TextView shortestTimeHalfMarathon;
	private TextView shortestTimeFullMarathon;
	
	//the Run History Record
	private RelativeLayout checkHistoryRecord;
	private TextView historyTimes;
	
	static final String HISTORYTIMES = "run_history_times";

	protected static final String ICON = "user_icon";
	//More setup Choice
	private RelativeLayout moreSetup;
	
	//The best Record Item name, Used in Preference
	private final String SPEEDMATCH = "speed_match";
	private final String LONGESTDISTANCE = "longestDistance";
	private final String LONGESTTIME = "longestTime";
	private final String SHORTESTTIME_5 = "shortestTimefive";
	private final String SHORTESTTIME_10 = "shortestTimeTen";
	private final String SHORTESTTIME_HM = "shortestTimeHalfMarathon";
	private final String SHORTESTTIME_FM = "shortestTimeFullMarathon";
	
	 
	
	private SharedPreferences loginDataPreference;
	
	private UserInfo mInfo;

	private ImageView userIconDefault;
	private static String path;
	private AlertDialog.Builder builder;
	
	private Cursor mCursor;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_profile_info);
		loginDataPreference = getSharedPreferences(FreeRunConstants.PROFILE_INFO_PREFERENCES,
				Context.MODE_PRIVATE);
		mTencent = Tencent.createInstance(FreeRunConstants.APP_ID, this.getApplicationContext());
		path = this.getCacheDir().getPath();
		logState = loginDataPreference.getBoolean(LOGSTATE, false);
		// 初始化视图
		
		initViews();
		onListenMyItemClick();
		
		getLoaderManager().initLoader(22, null, new MyLoaderCallBacks());
		
		String[]  projectionId = {RunRecordTable.COLUMN_USEDTIME, 
				RunRecordTable.COLUMN_DISTANCE};
		mCursor = getContentResolver().query(FreeRunContentProvider.CONTENT_URI, 
						projectionId, 
						null,
						null, 
						RunRecordTable.COLUMN_ID + " DESC");
		if(null != mCursor && mCursor.getCount() >= 1) {
			Log.d(TAG, "Cursor Position: " + mCursor.getPosition());
			mCursor.moveToNext();
			int usedTime = mCursor.getInt(
					mCursor.getColumnIndex(
							RunRecordTable.COLUMN_USEDTIME));
			int distance = (int)mCursor.getLong(
					mCursor.getColumnIndex(
							RunRecordTable.COLUMN_DISTANCE));
			float bestVel = loginDataPreference.getFloat(SPEEDMATCH, 0);
			if(distance != 0 && bestVel < usedTime/distance) {
				fastestSpeedMatch.setText(Math.floor(usedTime)/distance + "'" 
						+ usedTime%distance + "\"");
				loginDataPreference.edit().putFloat(SPEEDMATCH, usedTime/distance).commit();
					
			}
			
		}
		
		
	}
	@Override
	protected void onStart() {
		super.onStart();
		//handle user profile info

	}
	
	
	private void initViews() {
		
		userSummaryInfo = (LinearLayout) findViewById(R.id.user_summary_info);
		userIcon = (ImageView)findViewById(R.id.img_user_avatar);
		userIconDefault = (ImageView) findViewById(R.id.img_user_avatar_default);
		login = (LinearLayout)findViewById(R.id.login);
		logout = (LinearLayout)findViewById(R.id.logout);
		editInfo = (TextView)findViewById(R.id.info_edit);
		userNickname = (TextView)findViewById(R.id.pro_nickname);
		totalDistance = (TextView)findViewById(R.id.total_distance);
		totalTime = (TextView)findViewById(R.id.total_time);
		totalCalories = (TextView)findViewById(R.id.total_calories);

		//handle the bestest history record. Start.
		fastestSpeedMatch = (TextView)findViewById(R.id.fastest_speed_match);
		longestDistance = (TextView)findViewById(R.id.longest_distance);
		longestTime = (TextView)findViewById(R.id.longest_time);
		shortestTimeFive = (TextView)findViewById(R.id.shortest_time_five);
		shortestTimeTen = (TextView)findViewById(R.id.shortest_time_ten);
		shortestTimeHalfMarathon = (TextView)findViewById(R.id.shortest_time_half_marathon);
		shortestTimeFullMarathon = (TextView)findViewById(R.id.shortest_time_full_marathon);
		
		//run history record
		historyTimes = (TextView)findViewById(R.id.record_history_times);
		checkHistoryRecord = (RelativeLayout) findViewById(R.id.run_history);
		
		//More setup
		moreSetup = (RelativeLayout) findViewById(R.id.more_setUp);
		handleLogin();			

	}

private void  onListenMyItemClick() {
		
		// show Login dialog
		userSummaryInfo.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
				// TODO Auto-generated method stub
			//Login Dialog 
			builder = new AlertDialog.Builder(LoginAndProfileInfo.this);
			if(false == logState) {
				builder.setMessage("Do you want to login ?")
				   .setCancelable(false)
				   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int id) {
				        	Log.d(TAG, "\n======  LONIN START  ============");
					            	logState = true;
					    			loginDataPreference
					            	.edit()
					            		.putBoolean(LOGSTATE, true)
					            			.commit();
					    			login();
					           }
					     })
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
							
							}
						});
			}
//			else {
//				builder.setMessage("Do you want to Logout ?")
//				   .setCancelable(false)
//				   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//				        public void onClick(DialogInterface dialog, int id) {
//				        	Log.d(TAG, "\n======  LOGOUT END  ============");
//							logout();
//							logState = false;
//							loginDataPreference
//			            	.edit()
//			            		.putBoolean(LOGSTATE, false)
//			            			.commit();
//							handleLogin();
//							dialog.cancel();
//					      }
//					 })
//					.setNegativeButton("No", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int id) {
//								dialog.dismiss();
//						}
//					});
//			}
			
			builder.create()
					.show();
				 //alert = builder.create();
				 //alert.show();
		}
		});
		
		//Enter Edit user Info Activity
		editInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction(FreeRunConstants.ACTION_EDIT_USER_INFO);
				startActivity(intent);
			}
		});
		
		//Enter check history record Activity
		checkHistoryRecord.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction(FreeRunConstants.ACTION_CHECK_RECORD);
				startActivity(intent);
			}
		});
		
		//Enter More setup choice Activity
		moreSetup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction(FreeRunConstants.ACTION_SETUP);
				startActivity(intent);
			}
		});
	}
	private void handleLogin()
	{ 
		
		if(logState) {
			Log.d(TAG, "Login .......");
			//userIcon.setImageResource(R.id.user_icon);
			logout.setVisibility(View.GONE);
			login.setVisibility(View.VISIBLE);
			
			//user simple Info
			{
				userIconDefault.setVisibility(View.GONE);
				userIcon.setVisibility(View.VISIBLE);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 1;
				Bitmap bm = BitmapFactory.decodeFile(path + "/" + ICON, options);
				userIcon.setImageBitmap(bm);
			}
			userNickname.setText(
					loginDataPreference.getString(NICKNAME, "Vistor"));
			totalDistance.setText(
					loginDataPreference.getString(TOTALDISTANCE, "none"));
			totalTime.setText(
					loginDataPreference.getString(TOTALTIME, "none"));
			totalCalories.setText(
					loginDataPreference.getString(TOTALCALORIES, "none"));
			
			//the best Record
			fastestSpeedMatch.setText(
					loginDataPreference.getString(SPEEDMATCH, "nothing"));
			longestDistance.setText(
					loginDataPreference.getString(LONGESTDISTANCE, "nothing"));
			longestTime.setText(
					loginDataPreference.getString(LONGESTTIME, "nothing"));
			shortestTimeFive.setText(
					loginDataPreference.getString(SHORTESTTIME_5, "nothing"));
			shortestTimeTen.setText(
					loginDataPreference.getString(SHORTESTTIME_10, "nothing"));
			shortestTimeHalfMarathon.setText(
					loginDataPreference.getString(SHORTESTTIME_HM, "nothing"));
			shortestTimeFullMarathon.setText(
					loginDataPreference.getString(SHORTESTTIME_FM, "nothing"));
		
			//the history run times
			historyTimes.setText(
					loginDataPreference.getInt(HISTORYTIMES, 0) + "次");
		} else {
			Log.d(TAG, "Logout......");
			login.setVisibility(View.GONE);
			logout.setVisibility(View.VISIBLE);
			userIcon.setVisibility(View.GONE);
			userIconDefault.setVisibility(View.VISIBLE);
			
		}
		
	}
	
	/**
	 *  调用QQ登录接口
	 * **/
	
	public void login()
	{
		mTencent = Tencent.createInstance(FreeRunConstants.APP_ID, this);
		Log.d(TAG, "Login(): mTencent.isSessionVaild = " + mTencent.isSessionValid() + 
				   "\n isReady = " + mTencent.isReady());
		if (!mTencent.isSessionValid())
		{
			mTencent.login(this, "all", loginListener);
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
							Log.d(TAG, "====> run()");
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
			mInfo = new UserInfo(this, mTencent.getQQToken());
			mInfo.getUserInfo(listener);
			//show the user personal login info.
        	handleLogin();

		} else {
			Log.d(TAG, "isSessionVaild =false, Don't Login." );
			userNickname.setText("");
			userNickname.setVisibility(android.view.View.GONE);
			userIcon.setVisibility(android.view.View.GONE);
		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				JSONObject response = (JSONObject) msg.obj;
				if (response.has("nickname")) {
					try {
						userNickname.setText(response.getString("nickname"));
						loginDataPreference
						.edit()
						.putString(NICKNAME, response.getString("nickname"))
						.commit();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}else if(msg.what == 1){
				Bitmap bitmap = (Bitmap)msg.obj;
				userIcon.setImageBitmap(bitmap);
				if(bitmap != null ) {
					Log.d(TAG, bitmap.toString());
				}else {
					Log.e(TAG, "bitmap is null");
				}
				
				
				try {
					saveBitmapToFile(bitmap, path + "/" + ICON);
				}catch(IOException e) {
					Log.e(TAG, "==>Error: " + e.getMessage());
					e.printStackTrace();
				}
				
			}
		}

	};
	
	protected void onResume() {
		super.onResume();
		//user simple Info
		{
			userIconDefault.setVisibility(View.GONE);
			userIcon.setVisibility(View.VISIBLE);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			Bitmap bm = BitmapFactory.decodeFile(path + "/" + ICON, options);
			userIcon.setImageBitmap(bm);
		}
		
	}
	
	
	
	
	
	/**
	 * 应用调用Andriod_SDK接口时，如果要成功接收到回调，
	 * 需要在调用接口的Activity的onActivityResult方法中增加如下代码：
	 * 
	 * **/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
	            Utils.showResultDialog(LoginAndProfileInfo.this, data.getStringExtra(Constants.LOGIN_INFO), "登录成功");
	    		
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
                Utils.showResultDialog(LoginAndProfileInfo.this, "返回为空", "登录失败");
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                Utils.showResultDialog(LoginAndProfileInfo.this, "返回为空", "登录失败");
                return;
            }
			//Utils.showResultDialog(LoginAndProfileInfo.this, response.toString(), "登录成功");
            // 有奖分享处理
            // handlePrizeShare();
            MyLoginPromptDialog dialongPrompt = new MyLoginPromptDialog();
    		dialongPrompt.show(getFragmentManager(), "MyLoginPromptDialog");
			doComplete((JSONObject)response);
		}

		protected void doComplete(JSONObject values) {

		}

		@Override
		public void onError(UiError e) {
			Utils.toastMessage(LoginAndProfileInfo.this, "onError: " + e.errorDetail);
			//Utils.dismissDialog();
		}

		@Override
		public void onCancel() {
			Utils.toastMessage(LoginAndProfileInfo.this, "onCancel: ");
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
			CursorLoader cursorLoader = new CursorLoader(LoginAndProfileInfo.this,
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
    		Button editInfo = (Button) view.findViewById(R.id.editInfo);
    		editInfo.setOnClickListener(new OnClickListener() {
				
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
					getDialog().dismiss();
				}
			});
    		
    		super.onActivityCreated(savedInstanceState);
    	}
    }
	
	
}
