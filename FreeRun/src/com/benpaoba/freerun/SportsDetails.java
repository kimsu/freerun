package com.benpaoba.freerun;


public class SportsDetails {
	
    private String mUserId;
    private long mSportDate;
    private long mStartTime;
    private long mUsedTime;
    private double mDistance;
    private String mDataFilePath;
    
    public static class Builder {
    	private String userId = null;
        private long sportDate = 0;
        private long startTime = 0;
        private long usedTime = 0;
        private double distance = 0;
        private String dataFilePath = null;
        
        public Builder(String userId) {
        	this.userId = userId;
        }
        
        public Builder setSportsDate(long sportDate) {
        	this.sportDate = sportDate;
        	return this;
        }
        
        public Builder setStartTime(long startTime) {
        	this.startTime = startTime;
        	return this;
        }
        
        public Builder setUsedTime(long usedTime) {
        	this.usedTime = usedTime;
        	return this;
        }
        
        public Builder setDistance(double distance) {
        	this.distance = distance;
        	return this;
        }
        
        public Builder setDataFilePath(String filePath) {
        	this.dataFilePath = filePath;
        	return this;
        }
        
        public SportsDetails build() {
        	return new SportsDetails(this);
        }
    }
     
    private SportsDetails(Builder builder) {
    	mUserId = builder.userId;
    	mSportDate = builder.sportDate;
    	mStartTime = builder.startTime;
    	mUsedTime = builder.usedTime;
    	mDistance = builder.distance;
    	mDataFilePath = builder.dataFilePath;
    }
    
    public String getUserId() {
    	return mUserId;
    }
    
    public long getDate() {
    	return mSportDate;
    }
    
    public long getStartTime() {
    	return mStartTime;
    }
    
    public long getUsedTime() {
    	return mUsedTime;
    }
    
    public double getDistance() {
    	return mDistance;
    }
    
    public String getDataFilePath() {
    	return mDataFilePath;
    }
}
