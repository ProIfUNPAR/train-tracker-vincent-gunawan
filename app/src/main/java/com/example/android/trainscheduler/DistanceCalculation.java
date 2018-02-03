package com.example.android.trainscheduler;


public class DistanceCalculation {
    public static final double R= 6371;//in KM
    private double lat1,lat2,dlat;
    private double long1,long2,dlong;
    private double a,c,d;

    public DistanceCalculation(double lat1,double lat2,double long1,double long2){
        this.lat1 = lat1;
        this.lat1 = Math.toRadians(this.lat1);
        this.lat2 = lat2;
        this.lat2 = Math.toRadians(this.lat2);
        this.long1 = long1;
        this.long1 = Math.toRadians(this.long1);
        this.long2 = long2;
        this.long2 = Math.toRadians(this.long2);
        this.dlat = Math.toRadians(lat2-lat1);
        this.dlong = Math.toRadians(long2-long1);
    }

    public double count(){
        this.a = Math.sin(this.dlat/2)*Math.sin(this.dlat/2) + (Math.cos(this.lat1)*Math.cos(this.lat2))*(Math.sin(this.dlong/2)*Math.sin(this.dlong/2));
        this.c = 2 * (Math.atan2(Math.sqrt(this.a),Math.sqrt(1-this.a)));
        this.d = this.R * this.c;
        return this.d;
    }

}
