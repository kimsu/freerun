package com.benpaoba.freerun;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EditUserInfo extends Activity {
	private final String TAG = "FreeRun";

	/** 请求码 */
	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int RESULT_REQUEST_CODE = 2;

	protected static final String ICON = "user_icon";
	private static final String IMAGE_FILE_NAME = "image.jpg";
	private static final String NICKNAME = "nickname_text";
	private static final String SEX = "sex_text";
	private static final String LOCATION = "location_text";
	private static final String HEIGHT = "height_text";
	private static final String WEIGHT = "weight_text";
	
	private ImageButton back_up;
	private RelativeLayout editIcon;
	private ImageView user_icon;
	private RelativeLayout nickname;
	private TextView nickname_text;
	private EditText mEtNickname;
	private RelativeLayout sex;
	private TextView sex_text;
	private RelativeLayout location;
	private TextView location_text;
	private RelativeLayout height;
	private TextView height_text;
	private RelativeLayout weight;
	private TextView weight_text;
	private Button exit;
	private static String path;
	
	
	private SharedPreferences userInfoPreference;
	private static String[] photo_select_item = new String[]{"选择本地图片", "拍照"};
	private static String[] sex_item = new String[]{"男", "女"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_user_info);
		//the path used to save the user Icon
		path = this.getCacheDir().getPath();
		back_up = (ImageButton) findViewById(R.id.back_up);
		user_icon = (ImageView) findViewById(R.id.user_icon_edit);
		nickname_text = (TextView) findViewById(R.id.nickname_text);
		sex_text = (TextView)findViewById(R.id.sex_text);
		location_text = (TextView) findViewById(R.id.location_text);
		height_text = (TextView) findViewById(R.id.height_text);
		weight_text = (TextView) findViewById(R.id.weight_text);
 		userInfoPreference = getSharedPreferences(FreeRunConstants.PROFILE_INFO_PREFERENCES, 
				Context.MODE_PRIVATE);
		back_up.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});
		
		//Edit User Icon. Way 1 from Gallery; Way 2. take a photography
		editIcon = (RelativeLayout) findViewById(R.id.editIcon);
		editIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(EditUserInfo.this)
				.setTitle("设置头像")
				.setItems(photo_select_item, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0 :
							Intent intentFromGallery =new Intent(
			                        Intent.ACTION_PICK,
			                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); 
							intentFromGallery.setType("image/*"); // 设置文件类型
							
//							Intent intentFromGallery = new Intent();
//							intentFromGallery.setType("image/*"); // 设置文件类型
//							intentFromGallery
//							.setAction(Intent.ACTION_OPEN_DOCUMENT);
							startActivityForResult(intentFromGallery,
								IMAGE_REQUEST_CODE);
							break;
						case 1 :
							Intent intentFromCapture = new Intent(
								android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
							// 判断存储卡是否可以用，可用进行存储
							String state = Environment
									.getExternalStorageState();
							if (state.equals(Environment.MEDIA_MOUNTED)) {
								Log.d(TAG, " ==>MEDIA_MOUNTED");
								File path = Environment
										.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
								File file = new File(path, IMAGE_FILE_NAME);
								Log.d(TAG, "File Uri: " + Uri.fromFile(file));
								intentFromCapture.putExtra(
										MediaStore.EXTRA_OUTPUT,
										Uri.fromFile(file));
							}
							startActivityForResult(intentFromCapture,
									CAMERA_REQUEST_CODE);
							break;
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();

			}
		});
		
		
		
		//Edit the nickname of user 
		nickname = (RelativeLayout) findViewById(R.id.nickname);
		nickname.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mEtNickname = new EditText(EditUserInfo.this);
				mEtNickname.setText("呢称");
	    		mEtNickname.setTextColor(getResources().getColor(R.color.gray));
				try {
					new AlertDialog.Builder(EditUserInfo.this).setTitle("请输入昵称")
							.setCancelable(false)
							.setView(mEtNickname)
							.setPositiveButton("yes", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
										//昵称
										String editTextContent = mEtNickname.getText().toString().trim();
										if (!TextUtils.isEmpty(editTextContent)) {
										    nickname_text.setText(editTextContent);
										    userInfoPreference.edit()
										    .putString(NICKNAME, editTextContent)
										    .commit();
											
										}
								}
							})
							.setNegativeButton("no", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							})
							.show();
				} catch (Exception e) {
				}
			}
		});
		
		
		//Edit the user sex
		sex = (RelativeLayout) findViewById(R.id.sex);
		sex.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(EditUserInfo.this)
				.setTitle("选择性别")
				.setCancelable(true)
				.setItems(sex_item, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.d(TAG, "SEX: " + sex_item[which]);
						sex_text.setText(sex_item[which]);
						userInfoPreference.edit()
						.putString(SEX, sex_item[which]).commit();
	
					}
				})
				.create().show();;
			}
		});
		
		//Edit the user location
		location = (RelativeLayout) findViewById(R.id.location);
		location.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(EditUserInfo.this)
				.setCancelable(true)
				.setTitle("选择你所在的省份")
				.setItems(FreeRunConstants.LOCATION, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.d(TAG, "LOCATION: " + FreeRunConstants.LOCATION[which]);
						location_text.setText(FreeRunConstants.LOCATION[which]);
						userInfoPreference.edit()
						.putString(LOCATION, FreeRunConstants.LOCATION[which]).commit();
						
					}
				})
				.create().show();
			}
		});
		
		//Edit the user Height
		height = (RelativeLayout) findViewById(R.id.height);
		height.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(EditUserInfo.this)
				.setCancelable(true)
				.setTitle("选择你的身高")
				.setItems(FreeRunConstants.HEIGHTS, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						height_text.setText(FreeRunConstants.HEIGHTS[which]);
						userInfoPreference.edit()
						.putString(HEIGHT, FreeRunConstants.HEIGHTS[which])
						.commit();
					}
				})
				.create().show();

				
			}
		});
		
		//Edit the user weight
		weight = (RelativeLayout) findViewById(R.id.weight);
		weight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(EditUserInfo.this)
				.setCancelable(true)
				.setTitle("选择你的体重")
				.setItems(FreeRunConstants.WEIGHT, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						weight_text.setText(FreeRunConstants.WEIGHT[which]);
						userInfoPreference.edit()
						.putString(WEIGHT, FreeRunConstants.WEIGHT[which])
						.commit();
					}
				})
				.create().show();
				
			}
		});
		
		//Exit
		Button exit = (Button) findViewById(R.id.exit);
		exit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});
		
	}
	
	
	protected void onResume() {
		super.onResume();
		
		//load the user icon
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			Bitmap bm = BitmapFactory.decodeFile(path + "/" + ICON, options);
			user_icon.setImageBitmap(bm);
			
		}
		//load the user nickname
		nickname_text.setText(
				userInfoPreference.getString(NICKNAME, "Jeff"));
		
		//load the user sex
		sex_text.setText(
				userInfoPreference.getString(SEX, "男"));
		
		//load the user location
		location_text.setText(
				userInfoPreference.getString(LOCATION, "上海"));
		
		//Load the user height
		height_text.setText(
				userInfoPreference.getString(HEIGHT, "172cm"));
		
		//load the user weight
		weight_text.setText(
				userInfoPreference.getString(WEIGHT, "60kg"));
		
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "requestCode = " + requestCode + "  resultCode = " + resultCode);
		//返回结果码不等于取消
		if(RESULT_CANCELED != resultCode) { 
			switch(requestCode) {
			case IMAGE_REQUEST_CODE :
				Uri selectedImage = data.getData();
//	            String[] filePathColumn = { MediaStore.Images.Media.DATA };
//	            Cursor cursor = getContentResolver().query(selectedImage,
//	                    filePathColumn, null, null, null);
//	            cursor.moveToFirst();
//	  
//	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//	            String picturePath = cursor.getString(columnIndex);
//	            cursor.close();
//	            user_icon.setImageBitmap(BitmapFactory.decodeFile(picturePath));
				startPhotoZoom(selectedImage);
				break;
			case CAMERA_REQUEST_CODE:
				//判断存储卡是否可用，用于进行存储
				String state = Environment.getExternalStorageState();
				if(state.equals(Environment.MEDIA_MOUNTED)) {
					File path = Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
				File tempFile = new File(path, IMAGE_FILE_NAME);
				startPhotoZoom(Uri.fromFile(tempFile));
				} else {
					Toast.makeText(getApplicationContext(), "未找到存储卡，无法存储照片",
							Toast.LENGTH_LONG).show();
				}
				break;
			case RESULT_REQUEST_CODE : //图片缩放完成后
				if(data != null) {
					getImageToView(data);
				}
				break;
				
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.acitivity_edit_user_info, menu);
		return true;
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.go_back:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	
	// 裁剪图片的方法
	private void startPhotoZoom(Uri uri) {
		Log.d(TAG, "startPhotoZoom" + "\nUri: " + uri);
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		//设置裁剪
		intent.putExtra("crop", "true");
		//aspectX, aspectY是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		//outputX, outputY 是裁剪图片宽高
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, RESULT_REQUEST_CODE);
	}
	
	//保存裁剪之后的图盘数据
	private void getImageToView(Intent data) {
		Bundle extras = data.getExtras();
		if(extras != null) {
			Bitmap photo = extras.getParcelable("data");
			Drawable drawable = new BitmapDrawable(this.getResources(), photo);
			user_icon.setImageDrawable(drawable);
			
			try {
				saveBitmapToFile(photo, this.getCacheDir().getPath() + "/" + ICON);
			}catch(IOException e) {
				Log.e(TAG, "==>Error: " + e.getMessage());
				e.printStackTrace();
			}
			
			
			
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

}
