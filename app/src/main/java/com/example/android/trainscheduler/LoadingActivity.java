package com.example.android.trainscheduler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class LoadingActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private static LoadingActivity instance;

//    public DistanceCalculation dc;

    private ArrayList<Kereta> keretas;
    private HashMap<String, Stasiun> stasiuns;

    private Context ctx;
    private TextView tvSpeed;
    public double speed = -1;

    private Thread t;
    private int TIME_OUT = 2500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        this.keretas = new ArrayList<>();
        this.stasiuns = new HashMap<>();
        this.instance = this;

//        dc= new DistanceCalculation(-6.914430, -7.329102, 107.602447, 108.355991);
//        Log.d("rStasiun", dc.count() + "");

        locationManager = (LocationManager) this.getSystemService(ctx.LOCATION_SERVICE);
        LocationListener ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                location.getLatitude();
                LoadingActivity.getInstance().speed = location.getSpeed() * 3.6;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.INTERNET
                }, 10);
                return;
            }
        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, ll);
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, ll);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, ll);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(LoadingActivity.this, MenuActivity.class);
                startActivity(i);
                finish();
            }
        }, TIME_OUT);
        this.getAll();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.INTERNET
                        }, 10);
                        return;
                    }
                }
        }
    }

    public static LoadingActivity getInstance() {
        return instance;
    }

    public ArrayList<Stasiun> getStasiuns() {
        ArrayList<Stasiun> result = new ArrayList<>();
        result.addAll(stasiuns.values());
        return result;
    }

    public ArrayList<Kereta> getKereta() {
        return this.keretas;
    }

    private void getAll() {
        TypedArray typedArray = getResources().obtainTypedArray(R.array.jadwal);
        String[][] array = new String[typedArray.length()][];
        for (int i = 0; i < typedArray.length(); i++) {
            int id = typedArray.getResourceId(i, 0);
            array[i] = getResources().getStringArray(id);
            ArrayList<Jadwal> jadwals = new ArrayList<>();
            for (int j = 1; j < array[i].length; j++) {
                String[] splits = array[i][j].split("#");
                String namaStasiun = splits[0];
                namaStasiun = namaStasiun.toLowerCase();
//                Log.d("namaStasiun", array[i][0]);
                if (splits[1].length() == 10) {
                    StringBuilder sb = new StringBuilder(splits[1]);
                    sb = sb.deleteCharAt(splits[1].length() - 4);
                    splits[1] = sb.toString();

                    sb = new StringBuilder(splits[2]);
                    sb = sb.deleteCharAt(splits[2].length() - 4);
                    splits[2] = sb.toString();
                }
                double latitude = Double.parseDouble(splits[1]);
                double longtitude = Double.parseDouble(splits[2]);
                String jamDatang = splits[3];
                String jamPergi = splits[4];
//                Log.d("jamjam", "" + jamDatang + (jamDatang.equalsIgnoreCase("NULL")) + " " + jamPergi);
                if (!jamDatang.equalsIgnoreCase("X")) {
                    if (jamDatang.length() > 2) {
                        String[] jdSplit = jamDatang.split("\\.");
                        if (jdSplit[0].length() == 1) {
                            jdSplit[0] = "0" + jdSplit[0];
                        }
                        if (jdSplit[1].length() == 1) {
                            jdSplit[1] = jdSplit[1] + "0";
                        }
                        jamDatang = jdSplit[0] + ":" + jdSplit[1];
                    } else {
                        if (jamDatang.length() == 1) {
                            if (jamDatang.equalsIgnoreCase("-")) {
                                jamDatang = jamDatang;
                            } else {
                                jamDatang = "0" + jamDatang + ":00";
                            }
                        } else {
                            jamDatang = jamDatang + ":00";
                        }
                    }
                } else {
                    jamDatang = "X";
                }
                if (!jamPergi.equalsIgnoreCase("X")) {
                    if (jamPergi.length() > 2) {
                        String[] jpSplit = jamPergi.split("\\.");
                        if (jpSplit[0].length() == 1) {
                            jpSplit[0] = "0" + jpSplit[0];
                        }
                        if (jpSplit[1].length() == 1) {
                            jpSplit[1] = jpSplit[1] + "0";
                        }
                        jamPergi = jpSplit[0] + ":" + jpSplit[1];
                    } else {
                        if (jamPergi.length() == 1) {
                            if (jamPergi.equalsIgnoreCase("-")) {
                                jamPergi = jamPergi;
                            } else {
                                jamPergi = "0" + jamPergi + ":00";
                            }
                        } else {
                            jamPergi += ":00";
                        }
                    }
                } else {
                    jamPergi = "X";
                }
                if (!stasiuns.containsKey(namaStasiun)) {
                    stasiuns.put(namaStasiun, new Stasiun(namaStasiun, latitude, longtitude));
                }
                jadwals.add(new Jadwal(stasiuns.get(namaStasiun), jamDatang, jamPergi));

            }
            keretas.add(new Kereta(array[i][0], jadwals));
            Collections.sort(keretas, new Comparator<Kereta>() {
                @Override
                public int compare(Kereta kereta1, Kereta kereta2) {
                    return kereta1.getNamaKereta().compareToIgnoreCase(kereta2.getNamaKereta());
                }
            });
        }
//        for(int i=0;i<keretas.size();i++){
//            Log.d("namaKereta",""+keretas.get(i).getNamaKereta());
//        }
        typedArray.recycle();
    }
}
