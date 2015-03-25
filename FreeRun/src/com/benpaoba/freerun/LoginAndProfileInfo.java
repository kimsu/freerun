package com.benpaoba.freerun;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LoginAndProfileInfo extends Activity {
	private final String TAG = "FreeRun";
	
	private boolean logState = false;
	
	private ImageView  userIcon;
	private LinearLayout login;
	private LinearLayout logout;
	
	private	TextView fastestSpeedMatch;
	private	TextView longestDistance;
	private	TextView longestTime;
	private	TextView shortestTimeFive;
	private TextView shortestTimeTen;
	private TextView shortestTimeHalfMarathon;
	private TextView shortestTimeFullMarathon;
	
	private final String SPEEDMATCH = "speed_match";
	private final String LONGESTDISTANCE = "longestDistance";
	private final String LONGESTTIME = "longestTime";
	private final String SHORTESTTIME_5 = "shortestTimefive";
	private final String SHORTESTTIME_10 = "shortestTimeTen";
	private final String SHORTESTTIME_HM = "shortestTimeHalfMarathon";
	private final String SHORTESTTIME_FM = "shortestTimeFullMarathon";
	
	public final String PROFILE_INFO_PREFERENCES = "user_profile_info_preference"; 
	
	private SharedPreferences bestHistoryRecordPreference;
	
	
	protected void onCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_profile_info);
		bestHistoryRecordPreference = getSharedPreferences(PROFILE_INFO_PREFERENCES, Context.MODE_PRIVATE);
		logState = bestHistoryRecordPreference.getBoolean("LOGSTATE", false);
		
	}
	
	protected void onStart() {
		super.onStart();
		{ 
			//handle user profile info
			userIcon = (ImageView) findViewById(R.id.img_user_avatar);
			login = (LinearLayout) findViewById(R.id.login);
			logout = (LinearLayout) findViewById(R.id.logout);
			
			//handle the bestest history record. Start.
			fastestSpeedMatch = (TextView) findViewById(R.id.fastest_speed_match);
			longestDistance = (TextView) findViewById(R.id.longest_distance);
			longestTime = (TextView) findViewById(R.id.longest_time);
			shortestTimeFive = (TextView) findViewById(R.id.shortest_time_five);
			shortestTimeTen = (TextView) findViewById(R.id.shortest_time_ten);
			shortestTimeHalfMarathon = (TextView) findViewById(R.id.shortest_time_half_marathon);
			shortestTimeFullMarathon = (TextView) findViewById(R.id.shortest_time_full_marathon);
			
			if(logState) {
				//userIcon.setImageResource(R.id.user_icon);
				login.setVisibility(View.VISIBLE);
				logout.setVisibility(View.INVISIBLE);
				
				fastestSpeedMatch.setText(
						bestHistoryRecordPreference.getString(SPEEDMATCH, "nothing"));
				longestDistance.setText(
						bestHistoryRecordPreference.getString(LONGESTDISTANCE, "nothing"));
				longestTime.setText(
						bestHistoryRecordPreference.getString(LONGESTTIME, "nothing"));
				shortestTimeFive.setText(
						bestHistoryRecordPreference.getString(SHORTESTTIME_5, "nothing"));
				shortestTimeTen.setText(
						bestHistoryRecordPreference.getString(SHORTESTTIME_10, "nothing"));
				shortestTimeHalfMarathon.setText(
						bestHistoryRecordPreference.getString(SHORTESTTIME_HM, "nothing"));
				shortestTimeFullMarathon.setText(
						bestHistoryRecordPreference.getString(SHORTESTTIME_FM, "nothing"));
			} else {
				logout.setVisibility(View.VISIBLE);
				login.setVisibility(View.INVISIBLE);
			}
			
		}
	}
}
