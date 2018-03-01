package com.example.android.trainscheduler;


public class DistanceCalculation {
    public static final double R= 6371;//in KM
    private double latAwal, latAkhir,dlat;
    private double longAwal, longAkhir,dlong;
    private double a,c,d;

    public DistanceCalculation(double latAwal, double latAkhir, double longAwal, double longAkhir){
        this.latAwal = latAwal;
        this.latAwal = Math.toRadians(this.latAwal);
        this.latAkhir = latAkhir;
        this.latAkhir = Math.toRadians(this.latAkhir);
        this.longAwal = longAwal;
        this.longAwal = Math.toRadians(this.longAwal);
        this.longAkhir = longAkhir;
        this.longAkhir = Math.toRadians(this.longAkhir);
        this.dlat = Math.toRadians(latAkhir - latAwal);
        this.dlong = Math.toRadians(longAkhir - longAwal);
    }

    public double getJarak(){
        this.a = Math.sin(this.dlat/2)*Math.sin(this.dlat/2) + (Math.cos(this.latAwal)*Math.cos(this.latAkhir))*(Math.sin(this.dlong/2)*Math.sin(this.dlong/2));
        this.c = 2 * (Math.atan2(Math.sqrt(this.a),Math.sqrt(1-this.a)));
        this.d = this.R * this.c;
        return this.d;
    }

}
