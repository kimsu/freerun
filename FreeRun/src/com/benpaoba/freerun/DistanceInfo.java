package com.benpaoba.freerun;

public class DistanceInfo {
	private int id;
    private float distance;
    private double longitude;
    private double latitude;
    private int timeInSeconds;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public double getLongitude() {

        return longitude;
    }

    public void setLongitude(double longitude) {

        this.longitude = longitude;
    }

    public double getLatitude() {

        return latitude;
    }

    public void setLatitude(double latitude) {

        this.latitude = latitude;
    }

    public void setTime(int seconds) {
    	this.timeInSeconds = seconds;
    }
    
    public int getTime() {
    	return timeInSeconds;
    }
    @Override
    public String toString() {

        return "DistanceInfo [id=" + id + ", distance=" + distance
                + ", longitude=" + longitude + ", latitude=" + latitude +
                ", usedTime=" + timeInSeconds + "]";
    }

}
