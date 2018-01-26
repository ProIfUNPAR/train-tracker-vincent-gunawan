package com.example.android.trainscheduler;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapMenu extends FragmentActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient mFusedLocationClient;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location loc;

    private TextView label;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_menu);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        label = (TextView) findViewById(R.id.textView);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                label.setText("");
                label.append("\n " + location.getLatitude() + " " + location.getLongitude());
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

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{
//                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
//                }, 10);
//                return;
//            }
//        } else {
//            locationManager.requestLocationUpdates(
//                    LocationManager.GPS_PROVIDER, 5000, 1, locationListener);
//            mFusedLocationClient.getLastLocation()
//                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            if (location != null) {
//                                loc = location;
//                            }
//                        }
//                    });
//
//        }
//        locationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER, 5000, 1, locationListener);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            loc = location;
                        }
                    }
                });

        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        label.append("\n " + loc.getLatitude() + " " + loc.getLongitude());

        changeActivity();
    }

//    @SuppressLint("MissingPermission")
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case 10:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    mFusedLocationClient.getLastLocation()
//                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                                @Override
//                                public void onSuccess(Location location) {
//                                    if (location != null) {
//                                        loc = location;
//                                    }
//                                }
//                            });
//                }
//        }
//    }

    private void changeActivity() {
        Button hButton = findViewById(R.id.homeButton);
        hButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapMenu.this, Menu.class));
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        LatLng ll = new LatLng(loc.getLatitude(),loc.getLongitude());

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(ll)      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.addMarker(new MarkerOptions().position(ll).title("Current Location"));
    }
}
