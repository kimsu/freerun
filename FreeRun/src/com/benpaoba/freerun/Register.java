package com.benpaoba.freerun;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Register extends Activity {
	public final static int TELEPHONE = 0;
	public final static int EMAIL = 1;
	public final static int QQ = 2;
	public final static int WECHAT = 3;
	public final static int OTHERS = 4;
	protected static final String TAG = "Register";

	private EditText mUserEmail = null;
	private EditText mPassword = null;
	private EditText mRePassword = null;
	private RadioGroup mSex = null;
	private RadioButton mMale = null;
	private RadioButton mFemale = null;
	private Button mRegister = null;
	private Button mGoback = null;
	private User mUser = null;
	private boolean mUserEmailCursor = true;// 判读用户名输入框是失去光标还是获得光标
	private boolean mRepasswordCursor = true;// 判读重复密码输入框是失去光标还是获得光标
	private String mMySex = null;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if (msg.what == 1) {//等于1是说明接收到了注册成功的信息，当注册成功时，服务器会返回1给客户端
					Toast.makeText(Register.this, "注册成功", Toast.LENGTH_SHORT)
					.show();
					Intent register_login = new Intent(Register.this, WlecomActivity.class);
					startActivity(register_login);
					finish();
			} else {
				if (mUserEmail.getText().toString() == null) {
					Toast.makeText(Register.this, "用户名不能为空", Toast.LENGTH_SHORT)
					.show();
					mUserEmail.requestFocus();
				} else{
					Toast.makeText(Register.this, "注册失败", Toast.LENGTH_SHORT)
					.show();
				}			
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		init();
		initListener();
	}

	/**
	 * 页面元素初始化
	 */
	private void init() {

		this.mUserEmail = (EditText) this.findViewById(R.id.regi_userName);
		this.mPassword = (EditText) this.findViewById(R.id.regi_password);
		this.mRePassword = (EditText) this.findViewById(R.id.regi_repassword);
		this.mSex = (RadioGroup) this.findViewById(R.id.regi_sex);
		this.mMale = (RadioButton) this.findViewById(R.id.regi_male);
		this.mFemale = (RadioButton) this.findViewById(R.id.regi_female);
		this.mRegister = (Button) this.findViewById(R.id.regi_register);
		this.mGoback = (Button) this.findViewById(R.id.regi_goback);
	}

	private void initListener() {
		this.mUserEmail.setOnFocusChangeListener(new CheckmUserEmailListener());
		this.mRePassword.setOnFocusChangeListener(new RePasswordListener());
		this.mSex.setOnCheckedChangeListener(new RadioGroupSex());
		this.mRegister.setOnClickListener(new RegisterListener());
		this.mGoback.setOnClickListener(new ExitListener());
	}

	private class CheckmUserEmailListener implements OnFocusChangeListener {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			String myUserName = mUserEmail.getText().toString();
			if (!mUserEmailCursor) {
				if (isUsernameExisted(myUserName)) {
					Toast.makeText(Register.this, "该用户名已经存在，请更改用户名",
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		/**
		 * 用于检测用户输入的用户名是否已经注册
		 * 将用户输入的用户名传动到服务器，在用户数据库中查找该用户名，若能够查找到则返回true，否则返回false
		 * @param username
		 *            用户输入的用户名
		 * @return 标记该用户名是否已经存在，存在为true，不存在为false
		 */
		private boolean isUsernameExisted(String username) {
			boolean flag = false;
			return flag;
		}
	}

	/**
	 * RadioGroupSex
	 * @author renzhongfeng 
	 * 性别复选框的监听类，并将获得的性别赋给成员变量mySex 2014/07/27
	 */
	private class RadioGroupSex implements RadioGroup.OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if (checkedId == mMale.getId()) {
				mMySex = "男";
			} else {
				mMySex = "女";
			}
		}
	}

	/**
	 * RePasswordListener
	 * @author renzhongfeng 
	 * 重复输入密码失去光标的监听类 2014/07/27
	 */
	private class RePasswordListener implements OnFocusChangeListener {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if (mRepasswordCursor=!mRepasswordCursor) {
				if (!checkPassword(mPassword.getText().toString(), mRePassword
						.getText().toString())) {
					mRePassword.setText("");
					//rePassword.requestFocus();
					Toast.makeText(Register.this, "两次密码不一样，请重新输入",
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		
	}
	/**
	 * 比较两次输入的密码是否一致
	 * rePassword输入完成后，光标改变监听，和password进行比较，如果不一致，会有提示，并且两次密码密码清空
	 * 
	 * @param psw1
	 *            密码框中输入的密码
	 * @param psw2
	 *            重复密码框中输入的密码
	 * @return 两次密码一致返回true，否则返回false
	 */
	private boolean checkPassword(String psw1, String psw2) {
		if (psw1.equals(psw2))
			return true;
		else
			return false;
	}

	/**
	 * SpinnerListener
	 * @author renzhongfeng 
	 * 联系方式的spinner组件commnunication监听，获得Item的内�?
	 */
	private class SpinnerListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub

		}

	}
	/**
	 * 执行注册的方法
	 */
	public void excuteRegister(){
		new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				Log.d(TAG,"executeRegister()");
				HttpClient client = new DefaultHttpClient();
				Log.d(TAG,"11");
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				NameValuePair pair = new BasicNameValuePair("index", "2");
				Log.d(TAG,"22");
				list.add(pair);
				Log.d(TAG,"33");
				NameValuePair pair1 = new BasicNameValuePair("useremail", mUserEmail.getText().toString());
				NameValuePair pair2 = new BasicNameValuePair("password", mPassword.getText().toString());
				NameValuePair pair3 = new BasicNameValuePair("sex", mMySex);
				Log.d(TAG,"44");
				list.add(pair1);
				list.add(pair2);
				list.add(pair3);
				try {
					Log.d(TAG, "Before HttpPost");
					HttpEntity entity = new UrlEncodedFormEntity(list,"UTF-8");
					
					HttpPost post = new HttpPost("http://172.18.65.185:8080/MyFirstWeb/TestServlet");
					Log.d(TAG, "Before HttpPost, 222");
					post.setEntity(entity);
					HttpResponse response = client.execute(post);
					Log.d(TAG, "After HttpPost, 333");
					if (response.getStatusLine().getStatusCode() == 200) {
						InputStream in = response.getEntity().getContent();
						mHandler.sendEmptyMessage(in.read());//将服务器端返回给客户端的标记直接传输给handler
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
			
			}
		}.start();
	}
	/**
	 * RegisterListener
	 * @author renzhongfeng 
	 * 点击注册按钮后，向服务器端发送注册信息，等到服务器返回确认信息后，提示注册成功，并自动返回登陆页�?
	 * 2014/07/28
	 */
	private class RegisterListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (mMySex == null) {
				String title = "提示：";
				String message = "您的信息不完整，填写完整信息有助于我们提供更好的服务";
				new AlertDialog.Builder(Register.this).setTitle(title)
						.setMessage(message)
						.setPositiveButton("继续注册", new MakeSureListener())
						.setNegativeButton("返回修改", null).show();
			} else if (checkPassword(mPassword.getText().toString(), mRePassword
						.getText().toString())) {			
				excuteRegister();
			}else{
				mRePassword.setText("");
				//rePassword.requestFocus();
				Toast.makeText(Register.this, "两次密码不一样，请重新输入",
						Toast.LENGTH_SHORT).show();
			}
			
		}
		/**
		 * 获取用户的注册信息获取用户在页面上填写的信息，并将这些信息封装到User类中
		 * @return User 包含有用户完整注册信息的User包装�?
		 */
		private User getUser() {
			User user = new User();
			user.setPassword(mPassword.getText().toString());
			user.setUsername(mUserEmail.getText().toString());
			user.setSex(mMySex);
			return user;
		}
	}

	/**
	 * ExitListener
	 * @author renzhongfeng 
	 * 设置“返回按钮的点击监听，点击后回到登陆界面2014/07/27
	 */
	private class ExitListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent registerLlogin = new Intent(Register.this, WlecomActivity.class);
			startActivity(registerLlogin);
			finish();
		}
	}
	
	/**
	 * MakeSureListener
	 * @author renzhongfeng
	 * 确定提示框的确定按钮监听
	 */
	private class MakeSureListener implements
			android.content.DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			if (checkPassword(mPassword.getText().toString(), mRePassword
					.getText().toString())) {			
			excuteRegister();
		}else{
			mRePassword.setText("");
			//rePassword.requestFocus();
			Toast.makeText(Register.this, "两次密码不一样，请重新输入",
					Toast.LENGTH_SHORT).show();
		}
		}
	}
}

class User {
	/**
	 * username 用户名
	 * password 密码
	 * sex 用户性别
	 * communication 用户联系方式
	 */
	private String username = null;
	private String password = null;
	private String sex = null;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
}