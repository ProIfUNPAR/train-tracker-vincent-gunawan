package com.example.android.trainscheduler;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class LoadingActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private static LoadingActivity instance;

//    public MainPresenter dc;

    private ArrayList<Kereta> keretas;
    private HashMap<String, Stasiun> stasiuns;

//    private Context ctx;
//    private TextView tvSpeed;
//    private Thread t;

    public double speed = -1;
    private int TIME_OUT = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        this.keretas = new ArrayList<>();
        this.stasiuns = new HashMap<>();
        this.instance = this;

//        dc= new MainPresenter(-6.914430, -7.329102, 107.602447, 108.355991);
//        Log.d("rStasiun", dc.count() + "");

        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
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
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, ll);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, ll);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(LoadingActivity.this, MenuActivity.class);
                startActivity(i);
                finish();
            }
        }, TIME_OUT);
//        this.getAll();

        Parser parser = new Parser();
        keretas = parser.parseXML();
        Collections.sort(keretas, new Comparator<Kereta>() {
            @Override
            public int compare(Kereta kereta1, Kereta kereta2) {
                return kereta1.getNamaKereta().compareToIgnoreCase(kereta2.getNamaKereta());
            }
        });
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
}
