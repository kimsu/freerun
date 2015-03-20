package com.benpaoba.freerun;

public interface DistanceComputeInterface {
    double getDistance(double lat1, double lng1, double lat2, double lng2);

    double getShortDistance(double lat1, double lon1, double lat2, double lon2);

    double getLongDistance(double lat1, double lon1, double lat2, double lon2);

    double getDistanceBySpeed(double speed, double timeSpace);

    double getAccurancyDistance(double lat_a, double lng_a, double lat_b, double lng_b);

}
