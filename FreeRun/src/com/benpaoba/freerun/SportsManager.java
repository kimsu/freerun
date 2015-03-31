package com.benpaoba.freerun;

import java.io.File;
import java.io.IOException;

import android.os.Environment;

public class SportsManager {
	public static final String STATUS_ACTION = "com.phicomm.runningmap.sportscommand";
	
	public final static String POINTS_DIR = Environment.getExternalStorageDirectory().
			 getAbsolutePath() + "/sportsData/";
	
	public final static String POINTS_FILE = "sports";
	public final static String SUFFIX = ".dat";
	public final static int STATUS_INITIAL = 1 << 0 ;
	public final static int STATUS_READY = 1 << 1;
	public final static int STATUS_RUNNING = 1 << 2;
	public final static int STATUS_PAUSED = 1 << 3;
	public final static int STATUS_CANCELD = 1 << 4;
	//public final static int STATUS_FINISHED = 1 << 5;
	
	public static final String CMD_START = "start";
    public static final String CMD_FINISH = "finish";
    public static final String CMD_PAUSE = "pause";
    public static final String CMD_CONTINUE = "continue";
    
    public static void removePointsFile() {
    	File file = new File(POINTS_DIR, POINTS_FILE);
    	if(file != null && file.exists()) {
    		file.delete();
    	}
    }
    
    public static void createNewPointsFile() {
    	File dir = new File(POINTS_DIR);
    	if(!dir.exists()) {
    		dir.mkdirs();
    	}
    	File pointsFile = new File(dir, POINTS_FILE);
    	if(pointsFile != null && pointsFile.exists()) {
    		pointsFile.delete();
    	}
    	try {
			pointsFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
