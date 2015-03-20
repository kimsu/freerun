package com.benpaoba.freerun;

import java.security.InvalidParameterException;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

public class DistanceComputeImpl implements DistanceComputeInterface{

    private final static double DEF_PI = 3.14159265359; // PI
    private final static double DEF_2PI= 6.28318530712; // 2*PI
    private final static double DEF_PI180= 0.01745329252; // PI/180.0
    private final static double DEF_R =6370693.5; // radius of earth

    private static DistanceComputeImpl instance = null;

    public synchronized static DistanceComputeImpl getInstance(){
        if (instance == null) {
            instance = new DistanceComputeImpl();
        }
        return instance;
    }

    private DistanceComputeImpl(){}

    @Override
    public double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double distance = DistanceUtil.getDistance(new LatLng(lat1, lng1),new LatLng(lat2, lng2));
        return distance/1000;
    }

    @Override
    public double getShortDistance(double lat1, double lon1, double lat2,double lon2) {
        double ew1, ns1, ew2, ns2;
        double dx, dy, dew;
        double distance;
        // 角度转换为弧度
        ew1 = lon1 * DEF_PI180;
        ns1 = lat1 * DEF_PI180;
        ew2 = lon2 * DEF_PI180;
        ns2 = lat2 * DEF_PI180;
        // 经度差
        dew = ew1 - ew2;
        // 若跨东经和西经180 度，进行调整
        if (dew > DEF_PI)
            dew = DEF_2PI - dew;
        else if (dew < -DEF_PI)
            dew = DEF_2PI + dew;
        dx = DEF_R * Math.cos(ns1) * dew; // 东西方向长度(在纬度圈上的投影长度)
        dy = DEF_R * (ns1 - ns2); // 南北方向长度(在经度圈上的投影长度)
        // 勾股定理求斜边长
        distance = Math.sqrt(dx * dx + dy * dy);
        return distance/1000;
    }

    @Override
    public double getLongDistance(double lat1, double lon1, double lat2,double lon2) {
        double ew1, ns1, ew2, ns2;
        double distance;
        // 角度转换为弧度
        ew1 = lon1 * DEF_PI180;
        ns1 = lat1 * DEF_PI180;
        ew2 = lon2 * DEF_PI180;
        ns2 = lat2 * DEF_PI180;
        // 求大圆劣弧与球心所夹的角(弧度)
        distance = Math.sin(ns1) * Math.sin(ns2) + Math.cos(ns1) * Math.cos(ns2) * Math.cos(ew1 - ew2);
        // 调整到[-1..1]范围内，避免溢出
        if (distance > 1.0){
            distance = 1.0;
        } else if (distance < -1.0){
            distance = -1.0;
        }
        // 求大圆劣弧长度
        distance = DEF_R * Math.acos(distance);
        return distance/1000;
    }

    @Override
    public double getDistanceBySpeed(double speed, double timeSpace) {
        if (speed  < 0 || timeSpace <= 0) {
            throw new InvalidParameterException();
        }
        return speed * timeSpace;
    }

    @Override
    public double getAccurancyDistance(double lat_a, double lng_a,double lat_b, double lng_b) {
        double pk = (double) (180 / 3.14169);
        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt / 1000;
    }
}
