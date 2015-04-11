package com.benpaoba.freerun;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TabHost;

public class RunningTabActivity extends Activity {

	private static final String TAG = "FreeRun";
	private TabHost mTabHost;
	private String[] mTabSpecTags = {
            "aboutme", "start","more"
    };
	
	private RadioButton mRadioMe;
	private RadioButton mRadioStart;
	private RadioButton mRadioMore;
	private ViewGroup mMainContent;
	private ViewGroup mIndoorOutdoorView;
	private ViewGroup mOutdoorLayout;
	private ImageView mCancelView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_running_tab);
		
		mMainContent = (ViewGroup) findViewById(R.id.main_content);
		mRadioMe = (RadioButton)findViewById(R.id.radio_me);
		mRadioStart = (RadioButton)findViewById(R.id.radio_start);
		mRadioMore = (RadioButton)findViewById(R.id.radio_more);
		
		mIndoorOutdoorView = (RelativeLayout)findViewById(R.id.indoor_outdoor);
		mCancelView = (ImageView)findViewById(R.id.iv_run_mode_cancel);
		mOutdoorLayout = (LinearLayout)findViewById(R.id.layout_run_mode_outdoor);
		mOutdoorLayout.setOnClickListener(mClickListener);
		mIndoorOutdoorView.setOnClickListener(mClickListener);
		mCancelView.setOnClickListener(mClickListener);
		mRadioMe.setOnClickListener(mClickListener);
		mRadioStart.setOnClickListener(mClickListener);
		mRadioMore.setOnClickListener(mClickListener);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Fragment loginFragment = new LoginAndProfileInfo();  
		getFragmentManager().beginTransaction().replace(R.id.tab_content, loginFragment)
		.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)  
		.addToBackStack(null)  
		.commit();  
	}

	private View.OnClickListener mClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			switch(view.getId()){
			case R.id.radio_me:
				Fragment loginFragment = new LoginAndProfileInfo();  
				//loginFragment.setArguments(bundle);  
				getFragmentManager().beginTransaction().replace(R.id.tab_content, loginFragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)  
				.addToBackStack(null)  
				.commit();  
			    break;
			case R.id.radio_start:
				showIndoorOutdoorMainView(true);
				break;
			case R.id.radio_more:
				Intent moreIntent = new Intent(RunningTabActivity.this,MoreSetupChoice.class);
				startActivity(moreIntent);
				break;
			case R.id.iv_run_mode_cancel:
			case R.id.indoor_outdoor:
				showIndoorOutdoorMainView(false);
				break;
			case R.id.layout_run_mode_outdoor:
				Intent outdoorIntent = new Intent(RunningTabActivity.this,RunningMainActivity.class);
				startActivity(outdoorIntent);
				break;
			default:
				break;
			}
		}
	};
	
	private void showIndoorOutdoorMainView(boolean value) {
		mIndoorOutdoorView.setVisibility(value ? View.VISIBLE : View.GONE);
	}
	
	
}
