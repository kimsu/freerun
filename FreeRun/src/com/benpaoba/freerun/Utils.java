package com.benpaoba.freerun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class Utils {
	private final static String TAG = "FreeRun";
	private static Toast mToast;
    //保证该类不能被实例化
    private Utils(){

    }
    /**
     * 判断是否有存储卡，有返回TRUE，否则FALSE
     *
     * @return
     */
    public static boolean isSDcardExist() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static long getSDFreeSize(){
         //取得SD卡文件路径
         File path = Environment.getExternalStorageDirectory();
         StatFs sf = new StatFs(path.getPath());
         //获取单个数据块的大小(Byte)
         long blockSize = sf.getBlockSize();
         //空闲的数据块的数量
         long freeBlocks = sf.getAvailableBlocks();
         //返回SD卡空闲大小
         //Formatter.formatFileSize(this, (freeBlocks * blockSize));		//系统自动装换的格式
         //return freeBlocks * blockSize;  									//单位Byte
         //return (freeBlocks * blockSize)/1024;   							//单位KB
         return (freeBlocks * blockSize)/1024 /1024; 						//单位MB
    }

    /**
     * 检查某个应用是否安装
     * @param context
     * @param packageName
     * @return
     */
    @SuppressWarnings("unused")
    public static boolean checkAPP(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(packageName,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    /**
     *  获取当前应用程序的版本号
     * @param context
     * @return
     */
    public static String getAppVersion(Context context) {
        // 获取手机的包管理者
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            // 不可能发生.
            // can't reach
            return null;
        }
    }

    /**
     * 安装一个新的apk
     * @param file
     */
    public static void installApk(File file, Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
//		intent.setType("application/vnd.android.package-archive");
//		intent.setData(Uri.fromFile(file));
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 判断字符串是不是手机号码
     * @param paramString
     * @return
     */
    public static boolean isMobileNO(String paramString){
        return Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-1,5-9]))\\d{8}$").matcher(paramString).matches();
    }

    /**
     * 获取本机号码
     * @return
     */
    public static String getPhoneNumber(Context context){
        TelephonyManager mTelephonyMgr = (TelephonyManager)  context.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

    /**
     * MD5加密算法
     * @param plainText
     * @return
     */
    public static String Md5(String plainText ) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if(i<0) i+= 256;
                if(i<16)
                buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            return buf.toString();//32位的加密
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
    }

    /**
     * 打电话
     * @param context
     * @param phone_num
     */
    public static void makePhoneCall(Context context, String phone_num) {

        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        switch (manager.getSimState()) {
        case TelephonyManager.SIM_STATE_READY:
            Intent phoneIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone_num));
            context.startActivity(phoneIntent);
            break;
        case TelephonyManager.SIM_STATE_ABSENT:
            Toast.makeText(context, "无SIM卡", Toast.LENGTH_LONG).show();
            break;
        default:
            Toast.makeText(context, "SIM卡被锁定或未知状态", Toast.LENGTH_LONG).show();
            break;
        }
    }

    /**
     * 判断邮箱是否合法
     * @param email
     * @return
     */
    public static boolean isEmail(String email){
        if (null==email || "".equals(email)) return false;
//      Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 判断应用否是处于运行状态.
     * @param context
     * @param serviceName
     * @return
     */
    public static boolean isAppRunning(Context context , String myPackageName){
        if (null == myPackageName) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> infos = am.getRunningTasks(100);
//		String myPackageName = "com.agchauffeur";
        for(RunningTaskInfo info : infos){
            if (myPackageName.equals(info.topActivity.getPackageName()) || myPackageName.equals(info.baseActivity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 取double数据后面两位小数的上界
     */
    public static String getValueWith2Suffix(double dbl){
         BigDecimal bg = new BigDecimal(dbl / 1000);
         double formatValue = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
         return String.format("%.2f", formatValue);
    }

    /**
     * 发送短信
     *
     * @param mobile
     * @param content
     * @param context
     */
    public static void sendMessage(String mobile, String content, Context context) {
        SmsManager smsManager = SmsManager.getDefault();
        PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent("SENT_SMS_ACTION"), 0);
        if (content.length() >= 70) { // 短信字数大于70，自动分条
            List<String> ms = smsManager.divideMessage(content);
            for (String str : ms) {
                smsManager.sendTextMessage(mobile, null, str, sentIntent, null); // 短信发送
            }
        } else {
            smsManager.sendTextMessage(mobile, null, content, sentIntent, null);
        }
        // register the Broadcast Receivers
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "短信发送成功", Toast.LENGTH_SHORT).show();
                break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                break;
                }
            }
        }, new IntentFilter("SENT_SMS_ACTION"));
    }

    /**
     * Umeng集成测试
     * @param context
     * @return
     */
    public static String getDeviceInfo(Context context) {
        try{
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String device_id = tm.getDeviceId();
            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);

            if( TextUtils.isEmpty(device_id) ){
                device_id = mac;
            }

            if( TextUtils.isEmpty(device_id) ){
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
            }
            json.put("device_id", device_id);

            return json.toString();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Dialog
     * @param context
     * @param iconId
     * @param title
     * @param message
     * @param positiveBtnName
     * @param negativeBtnName
     * @param positiveBtnListener
     * @param negativeBtnListener
     * @return
     */
    public static Dialog createConfirmDialog(Context context,int iconId, String title, String message,
            String positiveBtnName,String negativeBtnName,
            android.content.DialogInterface.OnClickListener positiveBtnListener,
            android.content.DialogInterface.OnClickListener negativeBtnListener){
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(iconId);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveBtnName, positiveBtnListener);
        builder.setNegativeButton(negativeBtnName, negativeBtnListener);
        dialog = builder.create();
        return dialog;
    }
    
    
	public static final void showResultDialog(Context context, String msg,
			String title) {
		if(msg == null) return;
		String rmsg = msg.replace(",", "\n");
		new AlertDialog.Builder(context)
			.setTitle(title)
			.setMessage(rmsg)
			.setNegativeButton("Sure", null)
			.create()
			.show();
	}
	
	/**
	 * 
	 * @param activity
	 * @param message
	 * @param logLevel
	 */
	public static final void toastMessage(final Activity activity,
			final String message, String logLevel) {
		if ("w".equals(logLevel)) {
			Log.w("sdkDemo", message);
		} else if ("e".equals(logLevel)) {
			Log.e("sdkDemo", message);
		} else {
			Log.d("sdkDemo", message);
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mToast != null) {
					mToast.cancel();
					mToast = null;
				}
				mToast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
				mToast.show();
			}
		});
	}
	
	/**
	 * @param activity
	 * @param message
	 * @param logLevel
	 *         
	 */
	public static final void toastMessage(final Activity activity,
			final String message) {
		toastMessage(activity, message, null);
	}
	
	/**
	 * 
	 * @param imageUri
	 * @return
	 * @throws MalformedURLException
	 */
	public static Bitmap getbitmap(String imageUri) {
		Log.v(TAG, "getbitmap:" + imageUri);
		// 鏄剧ず缃戠粶涓婄殑鍥剧墖
		Bitmap bitmap = null;
		try {
			URL myFileUrl = new URL(imageUri);
			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();

			Log.v(TAG, "image download finished." + imageUri);
		} catch (IOException e) {
			e.printStackTrace();
			Log.v(TAG, "getbitmap bmp fail---");
			return null;
		}
		return bitmap;
	}
	
	public static String formatDoubleValue(double value) {
		BigDecimal b = new BigDecimal(value); 
		double formatDistance = b.setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
		return String.format("%.2f",formatDistance);
	}
	
	public static File getDownloadFile(String fileUri) {
		Log.d(TAG,"getDownloadFile, fileUri = " + fileUri);
		File file;
		URL myFileUrl = null;
		try {
			myFileUrl = new URL(fileUri);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			
			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setDoInput(true);
			conn.connect();
			file = new File(SportsManager.POINTS_DIR,Uri.parse(fileUri).getLastPathSegment());
			Log.d(TAG,"getDownloadFile, file.toString() = " + file.getAbsolutePath());
			if(!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			file.createNewFile();
			OutputStream out = new FileOutputStream(file);
			InputStream is = conn.getInputStream();
			//OutputStream out = new FileOutputStream(file);
			int length = 0;
			byte[] buffer = new byte[1024];
			while((length = is.read(buffer,0,1024)) != -1) {
				out.write(buffer,0,length);
			}
			return file;
			
		}catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	
}
