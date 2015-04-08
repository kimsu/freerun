package com.benpaoba.freerun;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.tauth.Tencent;
import com.umeng.update.UmengUpdateAgent;

public class MoreSetupChoice extends Activity {
	private final String TAG = "FreeRun";
	
	private RelativeLayout offline_map;
	private RelativeLayout ver_update;
	private RelativeLayout upload_log;
	private RelativeLayout feedback;
	private RelativeLayout about;
	private Button exit_login;
	
	private TextView downloadState;
	private TextView verState;
	private TextView verNumber;
	
	private SharedPreferences logStatePreference;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_choice);
		
		initView();
		onListeningItemClicked();
		logStatePreference = this.getSharedPreferences(
					FreeRunConstants.PROFILE_INFO_PREFERENCES,
					Context.MODE_PRIVATE);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.theme_background_green));
		
		
	}
	private void initView() {
		offline_map = (RelativeLayout) findViewById(R.id.offlinemap_download);
		ver_update = (RelativeLayout) findViewById(R.id.ver_update);
		upload_log = (RelativeLayout) findViewById(R.id.upload_log);
		feedback = (RelativeLayout) findViewById(R.id.feedback);
		about = (RelativeLayout) findViewById(R.id.about);
		exit_login = (Button) findViewById(R.id.exit_login);
		
		downloadState = (TextView) findViewById(R.id.download_state);
		verState = (TextView) findViewById(R.id.ver_state);
		verNumber = (TextView) findViewById(R.id.ver_number);
		verNumber.setText(Utils.getAppVersion(this));
	}
	private void onListeningItemClicked() {
		offline_map.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		ver_update.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				UmengUpdateAgent.setDefault();
				UmengUpdateAgent.forceUpdate(MoreSetupChoice.this);
			}
		});
		
		upload_log.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		feedback.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
	
		about.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		exit_login.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "\n======  LOGOUT END  ============");
				Tencent mTencent = Tencent.createInstance(FreeRunConstants.APP_ID, getApplicationContext());
				mTencent.logout(getApplicationContext());
				logStatePreference
            	.edit()
            		.putBoolean(LoginAndProfileInfo.LOGSTATE, false)
            			.commit();
				
				Intent intent = new Intent(MoreSetupChoice.this, LoginAndProfileInfo.class);
				startActivity(intent);
				}
		
		});		
		
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
}
