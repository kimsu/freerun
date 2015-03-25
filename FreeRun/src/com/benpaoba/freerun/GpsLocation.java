package com.benpaoba.freerun;

class GpsLocation {
	public double lat;
	public double lng;
	
	public GpsLocation() {
		this.lat = 0.0f;
		this.lng = 0.0f;
	}
	public GpsLocation(double latitude, double longtitude) {
		this.lat = latitude;
		this.lng = longtitude;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "(lat : " + lat + ", lng: " + lng + ")";
	}
	
}
