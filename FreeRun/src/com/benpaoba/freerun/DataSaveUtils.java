package com.benpaoba.freerun;

import java.io.File;
import java.io.DataInputStream;  
import java.io.DataOutputStream;
import java.io.FileInputStream;  
import java.io.FileNotFoundException;
import java.io.FileOutputStream;  
import java.io.IOException;
import java.io.EOFException;
import java.util.List;
import java.util.ArrayList;
import android.util.Log;
import com.baidu.mapapi.model.LatLng;
import android.os.Environment;



public class DataSaveUtils {

	private final static File mDataFile;
	private static DataInputStream mIn;
	private static DataOutputStream mOut;
	
	static {
		if (Utils.isSDcardExist() && (Utils.getSDFreeSize() > 1)) {
             String path = Environment.getExternalStorageDirectory().
            		 getAbsolutePath() + "/sportsData/";
             File directory = new File(path);
             if (!directory.exists()) {
                 directory.mkdirs();
             }
             mDataFile = new File(new File(path), "sports1.txt");
             try {
                 mOut = new DataOutputStream(new FileOutputStream(mDataFile,true));
                 mIn = new DataInputStream(new FileInputStream(mDataFile));
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             }
		} else {
			mDataFile = null;
		}
	}
	public static void writeToFile(double latitude, double longitude) {
		Log.d("yxf","writeToFile, mOut = " + mOut + ", mDataFile = " + mDataFile);
		try {
		    if(mOut != null) {
				mOut.writeDouble(latitude);
				mOut.writeChar('\t');
				mOut.writeDouble(longitude);
				mOut.writeChar('\n');
		    } 
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		try{
			mOut.flush();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static List<LatLng> readDataFromFile() {
		List<LatLng> dataList = new ArrayList<LatLng>();
		double latitude;
		double longitude;
		char c;
		boolean flag = true;
		try {
			while(flag) {
				latitude = mIn.readDouble();
				mIn.readChar();
				longitude = mIn.readDouble();
				mIn.readChar();
				dataList.add(new LatLng(latitude, longitude));
			}
			
		}catch (EOFException eof) {
			eof.printStackTrace();
			flag = false;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return dataList;
	}
	
}
