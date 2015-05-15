package com.benpaoba.freerun;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WlecomActivity extends Activity {

	private static final String TAG = "FreeRun";
	private Button mWelcomeButton;
	private Button mQQLoginButton;
	public static Tencent mTencent;
	private Button mLoginButton;
	private Button mRegisterButton;
	private EditText mUserEmail;
	private EditText mUserPassword;
	
	private ProgressDialog mDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wlecom);

		mQQLoginButton = (Button)findViewById(R.id.btn_qq_login);
		mWelcomeButton = (Button)findViewById(R.id.btn_welcome_trial);
		mLoginButton = (Button)findViewById(R.id.btn_login);
		mRegisterButton = (Button)findViewById(R.id.btn_register);
		mUserEmail = (EditText)findViewById(R.id.edt_login_username);
		mUserPassword = (EditText)findViewById(R.id.edt_login_pwd);
		mDialog = new ProgressDialog(this);
		mLoginButton.setOnClickListener(new LoginListener());
		mRegisterButton.setOnClickListener(new ButtonRegister());
		mWelcomeButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(WlecomActivity.this, RunningTabActivity.class);
				startActivity(intent);
			}
			
		});
		mQQLoginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(WlecomActivity.this,RunningTabActivity.class);
				startActivity(intent);
			}
			
		});
	}
	private class LoginListener implements OnClickListener{
		 String myUserName = mUserEmail.getText().toString();
	     String passwd = mUserPassword.getText().toString();

			@Override
			public void onClick(View v) {
				 mDialog.setTitle("提示信息");
	             mDialog.setMessage("正在登陆...");
	             mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	             mDialog.setCancelable(true);
	             new MyAsyncTask().execute();
			}
			
	 }

	public class MyAsyncTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            mDialog.show();
        }
        
        @Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
        	mDialog.dismiss();
		}
        
		@Override
        protected Void doInBackground(Void... params)
        {
        	HttpClient client = new DefaultHttpClient();
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			NameValuePair pair = new BasicNameValuePair("index", "0");
			list.add(pair);
			NameValuePair pair1 = new BasicNameValuePair("useremail", mUserEmail.getText().toString());
			NameValuePair pair2 = new BasicNameValuePair("password",  mUserPassword.getText().toString());
			
			list.add(pair1);
			list.add(pair2);
			try {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,"UTF-8");
				HttpPost post = new HttpPost("http://172.18.65.185:8080/MyFirstWeb/TestServlet");
				Log.d(TAG,"post: "+  post.getRequestLine());
				post.setEntity(entity);
				HttpResponse response = client.execute(post);
				Log.d(TAG,"response: " + response.getStatusLine());
				if (response.getStatusLine().getStatusCode() == 200) {
					InputStream in = response.getEntity().getContent();
					mHandler.sendEmptyMessage(in.read());//将服务器返回给客户端的标记直接传给handler
					in.close();
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
        }
    }
	
	/**
	 * 设置register监听
	 */
	private class ButtonRegister implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent loginAgreement = new Intent(WlecomActivity.this, Agreement.class);
			startActivity(loginAgreement);
			finish();
		}
	}

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == 1) {//当服务器返回给客户端的标记为1时，说明登陆成功
				SharedPreferences loginDataPreference = getSharedPreferences(
						FreeRunConstants.PROFILE_INFO_PREFERENCES,
						Context.MODE_PRIVATE);
				loginDataPreference.edit().putBoolean("log_state", true).commit();
				Intent loginMain = new Intent(WlecomActivity.this, RunningTabActivity.class);
				loginMain.putExtra("useremail", mUserEmail.getText().toString());
				startActivity(loginMain);
				finish();
			}else {
				Toast.makeText(WlecomActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT)
				.show();
			}
		}
	};
		
}
