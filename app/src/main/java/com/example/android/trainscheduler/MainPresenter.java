package com.example.android.trainscheduler;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MainPresenter {
    private ArrayList<Boolean> resultPolyline;
    private ArrayList<Double> routeLength;

    public MainPresenter() {
        resultPolyline = new ArrayList<>();
        routeLength = new ArrayList<>();
    }

    public int[] hitungWaktu(double jarak, double kecepatan) {
        int[] array = new int[3];
        if (kecepatan == 0) {
            kecepatan = MenuActivity.KECEPATAN_DEFAULT;
        }
        double temp = (jarak / kecepatan);
        int jam = (int) Math.floor(temp);
        int menit = (int) ((temp % 1) * 60);
        int detik = (int) ((((temp % 1) * 60) % 1) * 60);

        array[0] = jam;
        array[1] = menit;
        array[2] = detik;
        return array;
    }

    public String formatWaktu(int jam, int menit, int detik) {
        String sJam = (jam < 10) ? "0" + jam : jam + "";
        String sMenit = (menit < 10) ? "0" + menit : ("" + menit).substring(0, 2);
        String sDetik = (detik < 10) ? "0" + detik : ("" + detik).substring(0, 2);
        return sJam + ":" + sMenit + ":" + sDetik;
    }

    public boolean checkNearbyPolyline(ArrayList<PolylineOptions> polyline, Location currentLocation) {
        for (PolylineOptions po : polyline) {
            for (LatLng llPo : po.getPoints()) {
                float[] result = new float[1];
                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                        llPo.latitude, llPo.longitude, result);

                if (result[0] < 1000) {
                    // If distance is less than 100 meters, this is your polyline
                    resultPolyline.add(true);
                    return true;
                }
            }
        }
        resultPolyline.add(false);
        return false;
    }

    public int getNearbyIndex() {
        for(int i=0;i<resultPolyline.size();i++){
            if(resultPolyline.get(i)){
                return i;
            }
        }
        return -1;
    }
    public void resetResult(){
        resultPolyline = new ArrayList<>();
        routeLength = new ArrayList<>();
    }

    public void addRouteLength(double length){
        routeLength.add(length);
    }
    public void printRoute(){
        String temp = "";
        for(double d : routeLength){
            temp += d+"\n";
        }
        Log.d("ROUTELENGTH",temp);
    }
}
