package com.benpaoba.freerun;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tencent.tauth.Tencent;

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
	
	private RequestParams mRequestParams;
	private AsyncHttpClient mAsyncHttpClient;
	private String iconPath = null; 
	private SharedPreferences userInfoPreference;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		userInfoPreference = getSharedPreferences(FreeRunConstants.PROFILE_INFO_PREFERENCES,
				Context.MODE_PRIVATE);
		boolean status = userInfoPreference.getBoolean(FreeRunConstants.LOGIN_STATUS, false);
		if(status) {
			Intent loginMain = new Intent(WlecomActivity.this, RunningTabActivity.class);
			startActivity(loginMain);
			finish();
		}
		
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
		mRequestParams = new RequestParams();
		mAsyncHttpClient = new AsyncHttpClient();
		iconPath = this.getCacheDir().getAbsolutePath();
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

			@Override
			public void onClick(View v) {
				 mDialog.setTitle("提示信息");
	             mDialog.setMessage("正在登陆...");
	             mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	             mDialog.setCancelable(true);
	          // new MyAsyncTask().execute();
	             handleRequestLogin();
			}
			
	 }

	private void handleRequestLogin() {
		String myUserName = mUserEmail.getText().toString();  
	    String passwd = mUserPassword.getText().toString();
	    Log.d(TAG, "User Name: " + myUserName
	    		+ "\nPassword: " + passwd);
       if ((myUserName.length() != 0) && (passwd.length() != 0)) {
    	   mRequestParams.put("useremail", myUserName);
           mRequestParams.put("password", passwd);
           mRequestParams.setContentEncoding(HTTP.UTF_8);
           mAsyncHttpClient.post("http://192.168.1.137:8080/FirstServlet/servlet/JsonServlet",
          		 mRequestParams, 
          		 new JsonHttpResponseHandler(){
        	   		@Override
          			public void onSuccess(int statusCode,
          					Header[] headers, JSONObject response) {
          				// TODO Auto-generated method stub
          				super.onSuccess(statusCode, headers, response);
          				try {
          					boolean authStatus = false;
	   						authStatus = response.getBoolean("Authentication");
	          				if(authStatus == true) {
	          					Log.d(TAG, "onSuccess() in JsonHttpResponseHandler" 
	          							+ "\nStatus Code: " + statusCode 
	          							+ "\nJSONObject: " + response.toString());
	          					SharedPreferences.Editor editor = userInfoPreference.edit();
	   							editor.putString("nickname_text", response.getString("nickname_text"))
	   								.putString("location_text", response.getString("location_text"))
	   								.putString("height_text", response.getString("height_text"))
	   								.putString("weight_text", response.getString("weight_text"));
	   							if(response.getBoolean("sex_text")) {
	   								editor.putString("sex_text", "男");
	   							}else {
	   								editor.putString("sex_text", "女");
	   							}
	   							editor.commit();
	   							File iconFile = new File(iconPath + "/user_icon.png");
	     						 //Get user icon image;
	     						 mAsyncHttpClient.get("http://192.168.1.137:8080/FirstServlet/icon/777.png", new FileAsyncHttpResponseHandler(iconFile) {
	     							 @Override
	     							 public void onSuccess(int statusCode, Header[] header, File file) {
	     								 // TODO Auto-generated method stub
	     								Toast.makeText(WlecomActivity.this, "complete to download your info ", Toast.LENGTH_LONG).show();
	     								mHandler.sendEmptyMessage(1); 
	     							 }
	   							
	     							 @Override
	     							 public void onFailure(int arg0, Header[] arg1, Throwable arg2, File arg3) {
	     								 // TODO Auto-generated method stub
	   								
	     							 }
	     						 });
	     						 
	   						} else {
	   							mHandler.sendEmptyMessage(2);
	   						}
          				}catch (JSONException e) {
   							// TODO Auto-generated catch block
   							e.printStackTrace();
   						}
        	   		}
      	 		@Override
      			public void onFailure(int statusCode,
      					Header[] headers, String responseString,
      					Throwable throwable) {
      				// TODO Auto-generated method stub
      				super.onFailure(statusCode, headers, responseString, throwable);
      				Log.d(TAG, "onFailure() in JsonHttpResponseHandler" 
      						+ "\nStatu Code: " + statusCode
      						+ "\n Response String: " + responseString);
      			}
           });
       }else {
    	   Log.d(TAG, "show alert dialog !");
        	AlertDialog.Builder builder = new AlertDialog.Builder(WlecomActivity.this);
        	builder.setMessage("Account and Password can not be empty!")
        	.create().show();
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
				userInfoPreference.edit()
				.putBoolean(FreeRunConstants.LOGIN_STATUS, true)
				.commit();
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

	/*
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
				//http://172.18.65.185:8080/MyFirstWeb/TestServlet
				HttpPost post = new HttpPost("http://192.168.1.137:8080/FirstServlet/servlet/JsonServlet");
				Log.d(TAG,"post: "+  post.getRequestLine());
				post.setEntity(entity);
				HttpResponse response = client.execute(post);
				
				HttpEntity respEntity = response.getEntity();
				Log.d(TAG, "ContentType: " + respEntity.getContentType()
						+"\nContentEncoding: " + respEntity.getContentEncoding());
				InputStream respContent = respEntity.getContent();
				InputStreamReader isReader = new InputStreamReader(respContent);
				BufferedReader reader = new BufferedReader(isReader);
				Log.d(TAG, "Response Content: " + reader.readLine());
				
				
				HttpGet get = new HttpGet("http://192.168.1.137:8080/FirstServlet/icon/777.png");
				
				HttpResponse getResponse = client.execute(get);
				String path = null;
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					 path = Environment.getExternalStorageDirectory().getAbsolutePath();
					 if(getResponse != null) {
						 BufferedInputStream iconStream = new BufferedInputStream(
								 getResponse.getEntity().getContent());
							File iconFile = new File(path + "/DCIM/user_icon.png");
							BufferedOutputStream iconOS= new BufferedOutputStream(new FileOutputStream(iconFile));
							int length = 0;
							byte[] buffer = new byte[1024];
							while((length = iconStream.read(buffer)) != -1) {
								iconOS.write(buffer, 0, length);
							}
							iconOS.flush();
							iconOS.close();
					 }

				}else {
					Log.d(TAG, "Get Icon Response is null");
				}
				Log.d(TAG, "Cache Directory: " + iconPath
						+ "\nExternal Storage Directory: " + path);
				
				
				
				
				Log.d(TAG,"response: " + response.getStatusLine());
//				if (response.getStatusLine().getStatusCode() == 200) {
//					InputStream in = response.getEntity().getContent();
//					mHandler.sendEmptyMessage(in.read());//将服务器返回给客户端的标记直接传给handler
//					in.close();
//				}
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
	*/
}




