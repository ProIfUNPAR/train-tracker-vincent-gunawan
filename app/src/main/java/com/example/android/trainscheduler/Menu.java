package com.example.android.trainscheduler;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class Menu extends AppCompatActivity {
    private LocationManager locationManager;
    private static Menu instance;
    public DistanceCalculation dc;
    private ArrayList<Stasiun> stasiuns;
    public DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        instance = this;

        this.dbHelper = new DbHelper(this);
        this.stasiuns = new ArrayList<Stasiun>();

        dc = new DistanceCalculation(-6.914430, -7.329102, 107.602447, 108.355991);
//        Log.d("rStasiun", dc.count() + "");
        dbWrite();
        dbRead();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

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
        return this.stasiuns;
    }

    public void dbWrite() {
        String[] rStasiun = getResources().getStringArray(R.array.stasiun);
        for (int i = 0; i < rStasiun.length; i++) {
            String[] split = rStasiun[i].split(" ");
            String nama = "";
            for (int j = 0; j < split.length - 2; j++) {
                nama = nama + split[j] + " ";
            }
            nama = nama.trim();
            double latitude = Double.parseDouble(split[split.length - 2]);
            double longtitude = Double.parseDouble(split[split.length - 1]);
//            Log.d("StasiunDB",nama+"|"+latitude+"|"+longtitude);
            stasiuns.add(new Stasiun(nama, latitude, longtitude));
        }

        int size = stasiuns.size();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "SELECT count(*) FROM " + DbHelper.DbEntry.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        if (size > count) {
            ContentValues values = new ContentValues();
            for (int i = 0; i < stasiuns.size(); i++) {
                values.put(DbHelper.DbEntry.COLUMN_NAME, stasiuns.get(i).getNamaStasiun());
                values.put(DbHelper.DbEntry.COLUMN_LATITUDE, stasiuns.get(i).getLatitude());
                values.put(DbHelper.DbEntry.COLUMN_LONGTITUDE, stasiuns.get(i).getLongtitude());
                db.insertOrThrow(DbHelper.DbEntry.TABLE_NAME, null, values);
            }
        }
    }

    public void dbRead() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                DbHelper.DbEntry.COLUMN_ID,
                DbHelper.DbEntry.COLUMN_NAME,
                DbHelper.DbEntry.COLUMN_LATITUDE,
                DbHelper.DbEntry.COLUMN_LONGTITUDE
        };
        Cursor c = db.query(
                DbHelper.DbEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        c.moveToFirst();
        while (!c.isAfterLast()) {
            int id = c.getInt(c.getColumnIndex(DbHelper.DbEntry.COLUMN_ID));
            String name = c.getString(c.getColumnIndex(DbHelper.DbEntry.COLUMN_NAME));
            Double latitude = c.getDouble(c.getColumnIndex(DbHelper.DbEntry.COLUMN_LATITUDE));
            Double longtitude = c.getDouble(c.getColumnIndex(DbHelper.DbEntry.COLUMN_LONGTITUDE));
//            Log.d("dBReadTag", id + "|" + name + "|" + latitude + "|" + longtitude);
            c.moveToNext();
        }
    }
}
