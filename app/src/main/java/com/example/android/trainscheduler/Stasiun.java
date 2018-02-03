package com.example.android.trainscheduler;

/**
 * Created by toshiba pc on 2/3/2018.
 */

public class Stasiun {
    private String namaStasiun;
    private double latitude;
    private double longtitude;

    public Stasiun(String namaStasiun, double latitude, double longtitude) {
        this.namaStasiun = namaStasiun;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public String getNamaStasiun() {
        return namaStasiun;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setNamaStasiun(String namaStasiun) {
        this.namaStasiun = namaStasiun;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }
}
