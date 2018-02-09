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
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class Menu extends AppCompatActivity {
    private LocationManager locationManager;
    private static Menu instance;

//    public DistanceCalculation dc;

//    private ArrayList<Stasiun> stasiuns;
//    private ArrayList<Jadwal> jadwals;

    private ArrayList<Kereta> keretas;
    private HashMap<String,Stasiun> stasiuns;

    private Context ctx;

    private TextView tvSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        this.keretas = new ArrayList<>();
        this.stasiuns = new HashMap<>();
        this.tvSpeed = this.findViewById(R.id.speed_text);
        this.instance = this;

//        dc= new DistanceCalculation(-6.914430, -7.329102, 107.602447, 108.355991);
//        Log.d("rStasiun", dc.count() + "");

        locationManager = (LocationManager) this.getSystemService(ctx.LOCATION_SERVICE);
        LocationListener ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                location.getLatitude();
                tvSpeed.setText("Current Speed: " + (location.getSpeed()*3.6));
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, ll);

        changeActivity();
        this.getAll();


//        for(int i=0;i<keretas.size();i++){
//            Log.d("keretasTesting",""+keretas.get(i).getNamaKereta());
//        }
    }

    private void changeActivity() {
        Button mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Menu.this, MapMenu.class));
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

    public static Menu getInstance() {
        return instance;
    }

    public ArrayList<Stasiun> getStasiuns() {
        ArrayList<Stasiun> result = new ArrayList<>();
        result.addAll(stasiuns.values());
        return result;
    }
    public void getAll(){
        TypedArray test = getResources().obtainTypedArray(R.array.jadwal);
        String[][] array = new String[test.length()][];
        for(int i=0;i<test.length();i++){
            int id= test.getResourceId(i,0);
            array[i] = getResources().getStringArray(id);
            ArrayList<Jadwal> jadwals = new ArrayList<>();
            for(int j=1;j<array[i].length;j++){
                String[] splits = array[i][j].split("#");
                String namaStasiun = splits[0];
                double latitude = Double.parseDouble(splits[1]);
                double longtitude = Double.parseDouble(splits[2]);
                String jamDatang = splits[3];
                String jamPergi = splits[4];
                if(!stasiuns.containsKey(namaStasiun)){
                    stasiuns.put(namaStasiun,new Stasiun(namaStasiun,latitude,longtitude));
                }
                jadwals.add(new Jadwal(stasiuns.get(namaStasiun),jamDatang,jamPergi));
            }
            keretas.add(new Kereta(array[i][0],jadwals));
        }
        for(int i=0;i<keretas.size();i++){
            Log.d("namaKereta",""+keretas.get(i).getNamaKereta());
        }
        test.recycle();
    }
}
