package com.example.android.trainscheduler;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Menu extends AppCompatActivity {
    private LocationManager locationManager;
    private static Context instance;
    public DistanceCalculation dc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        instance = this;

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

        dc = new DistanceCalculation(-6.914430,-7.329102,107.602447,108.355991);
        Log.d("test",dc.count()+"");
    }

    private void changeActivity(){
        Button mapButton = (Button)findViewById(R.id.mapButton);
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

    public static Context getContext(){
        return instance;
    }
    public DbHelper dbHelper = new DbHelper(this);
    public void dbWrite(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
//        values.put(DbHelper.DbEntry.COLUMN_ID,2);
        values.put(DbHelper.DbEntry.COLUMN_NAME,"NAMA A");
        values.put(DbHelper.DbEntry.COLUMN_LATITUDE,1.0);
        values.put(DbHelper.DbEntry.COLUMN_LONGTITUDE,2.0);

        db.insertOrThrow(DbHelper.DbEntry.TABLE_NAME, null, values);
    }

    public void dbRead(){
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
        String itemD = c.getString(c.getColumnIndex(DbHelper.DbEntry.COLUMN_NAME));
        Log.d("dBReadTag",""+itemD);

    }
}
