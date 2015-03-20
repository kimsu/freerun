package com.benpaoba.freerun;

public class SportsManager {
	public static final String STATUS_ACTION = "com.phicomm.runningmap.sportscommand";
	
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
}
