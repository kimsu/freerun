package com.benpaoba.freerun.more;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.benpaoba.freerun.FreeRunConstants;
import com.benpaoba.freerun.LoginAndProfileInfo;
import com.benpaoba.freerun.R;
import com.benpaoba.freerun.Utils;
import com.tencent.tauth.Tencent;
import com.umeng.update.UmengUpdateAgent;

public class MoreSetupChoice extends Fragment {
	private final String TAG = "FreeRun";
	
	private RelativeLayout mOffline_Map;
	private RelativeLayout mVer_Update;
	private RelativeLayout mUpload_Log;
	private RelativeLayout mFeedback;
	private RelativeLayout mAbout;
	private Button mExit_Login;
	
	private TextView mDownloadState;
	private TextView mVerState;
	private Button mVersionCheck;
	
	private SharedPreferences logStatePreference;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.more_choice, container, false);
		initView(view);
		return view;
		
	}
	
	public  void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.more_choice);
		getActivity().setTitle(R.string.more);
		onListeningItemClicked();
		logStatePreference = getActivity().getSharedPreferences(
					FreeRunConstants.PROFILE_INFO_PREFERENCES,
					Context.MODE_PRIVATE);
		
		
	}
	private void initView(View v) {
		mOffline_Map = (RelativeLayout) v.findViewById(R.id.offlinemap_download);
		mVer_Update = (RelativeLayout) v.findViewById(R.id.ver_update);
		mUpload_Log = (RelativeLayout) v.findViewById(R.id.upload_log);
		mFeedback = (RelativeLayout) v.findViewById(R.id.feedback);
		mAbout = (RelativeLayout) v.findViewById(R.id.about);
		mExit_Login = (Button) v.findViewById(R.id.exit_login);
		
		mDownloadState = (TextView) v.findViewById(R.id.download_state);
		mVerState = (TextView) v.findViewById(R.id.ver_state);
		mVersionCheck = (Button) v.findViewById(R.id.ver_check);
		mVersionCheck.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				UmengUpdateAgent.setDefault();
				UmengUpdateAgent.forceUpdate(getActivity());	
			}
		});
		mVerState.setText(Utils.getAppVersion(getActivity()));
		
	}
	private void onListeningItemClicked() {
		mOffline_Map.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(getActivity(), OfflineMapExpandableList.class);
				startActivity(intent);
			}
		});
		
		mVer_Update.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});
		
		mUpload_Log.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mFeedback.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
	
		mAbout.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mExit_Login.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "\n======  LOGOUT END  ============");
				Tencent mTencent = Tencent.createInstance(FreeRunConstants.APP_ID, getActivity().getApplicationContext());
				mTencent.logout(getActivity().getApplicationContext());
				logStatePreference
            	.edit()
            		.putBoolean(LoginAndProfileInfo.LOGSTATE, false)
            			.commit();
				Fragment loginFragment = new LoginAndProfileInfo();  
				//loginFragment.setArguments(bundle);  
				getActivity().getFragmentManager().beginTransaction().replace(R.id.tab_content, loginFragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)  
				.commit();  
				
				
				}
		
		});		
		
	}


}
