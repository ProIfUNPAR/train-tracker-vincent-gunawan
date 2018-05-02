package com.example.android.trainscheduler;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MainPresenter {
    private ArrayList<Boolean> resultPolyline;
    private PolylineOptions[] pos;

    public MainPresenter() {
        resultPolyline = new ArrayList<>();
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

    public double getJarak(double latAwal, double latAkhir, double longAwal, double longAkhir) {
        double dlat = Math.toRadians(latAkhir - latAwal);
        double dlong = Math.toRadians(longAkhir - longAwal);

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) + (Math.cos(latAwal) * Math.cos(latAkhir)) * (Math.sin(dlong / 2) * Math.sin(dlong / 2));
        return 6371 * 2 * (Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    public void isPolylineNearby(Location currentLocation) {
        if(currentLocation == null){
            MenuActivity.getInstance().getBestLocation();
            currentLocation = MenuActivity.getInstance().getLoc();
        }
        for (PolylineOptions po : pos) {
            int flag = 0;
            if (po != null) {
                for (LatLng llPo : po.getPoints()) {
                    float[] result = new float[1];
                    Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                            llPo.latitude, llPo.longitude, result);

                    if (result[0] < 1000) {
                        // If distance is less than 100 meters, this is your polyline
                        resultPolyline.add(true);
                        break;
                    }
                }
                resultPolyline.add(false);
            } else {
                break;
            }
        }
//        return false;
    }

    public int getNearestStasiun() {
        for (int i = 0; i < resultPolyline.size(); i++) {
            if (resultPolyline.get(i)) {
                return i;
            }
        }
        return -1;
    }

    public int getNearestStasiun(ArrayList<Stasiun> stasiun, Location current) {
        int idx = -1;
        double jarak = Integer.MAX_VALUE;
        for (int i = 0; i < stasiun.size(); i++) {
            double temp = getJarak(current.getLatitude(), stasiun.get(i).getLatitude(), current.getLongitude(), stasiun.get(i).getLongtitude());
            if (temp < jarak) {
                jarak = temp;
                idx = i;
            }
        }
        return idx;
    }

    public void resetResult(int banyakStasiun) {
        resultPolyline = new ArrayList<>();
        this.pos = new PolylineOptions[1000];
    }

    public void setPanjangRute(int indeks, PolylineOptions po) {
        pos[indeks] = po;
    }
}
