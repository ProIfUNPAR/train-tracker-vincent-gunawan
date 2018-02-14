package com.example.android.trainscheduler;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MapMenu extends FragmentActivity
        implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GoogleApiClient mGoogleApiClient;
    private Location loc;
    private TextView tvSpeed;

    private Button hButton;
    private Spinner spinnerKereta, spinnerStasiun;
    private static MapMenu instance;
    private int idxKereta = -1;

    private TextView tvJarak,tvKecepatan;
    private double langNext,langCurr,latNext,latCurr,jarak;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_menu);
        this.instance = this;
        hButton = findViewById(R.id.homeButton);
        tvJarak = findViewById(R.id.tv_jarak);
        tvKecepatan = findViewById(R.id.tv_kecepatan);
        langNext = 0;
        langCurr = 0;
        latNext = 0;
        latCurr = 0;

        this.tvSpeed = findViewById(R.id.tv_kecepatan);
        ArrayList<Kereta> tempKereta = Menu.getInstance().getKereta();
        this.namaKereta = new ArrayList<>();
        for(Kereta k : tempKereta){
            namaKereta.add(k.getNamaKereta()+" ("+k.getJadwals().get(0).getStasiun().getNamaStasiun()+")");
        }
        this.namaJadwal = new ArrayList<>();
        this.setAllSpinner();

        hButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapMenu.this, Menu.class));
            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener  = new LocationListener() {
            @Override
            //waktu lokasinya pindah
            public void onLocationChanged(Location location) {
                tvKecepatan.setText(Menu.getInstance().speed + "km/jam");

                latCurr = loc.getLatitude();
                langCurr = loc.getLongitude();
                jarak = (new DistanceCalculation(latCurr,latNext,langCurr,langNext)).getJarak();
                tvJarak.setText(new DecimalFormat("#.##").format(jarak)+" km");
                location.getLatitude();
                tvSpeed.setText(String.format("%.2f", (location.getSpeed()*3.6)));
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }
            @Override
            //waktu GPS on
            public void onProviderEnabled(String s) {
            }
            @Override
            //waktu GPS off
            public void onProviderDisabled(String s) {
            }
        };
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, locationListener);
        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(ll)      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                return true;
            }
        });;
        mMap.setMyLocationEnabled(true);
        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc != null) {
            //label.setText("");
            // label.append("\n " + loc.getLatitude() + " " + loc.getLongitude());
            LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(ll)      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        ArrayList<Stasiun> stasiuns = Menu.getInstance().getStasiuns();

//        Bitmap bIcon=((BitmapDrawable)getResources().getDrawable(R.drawable.train_icon)).getBitmap();
//        bIcon = Bitmap.createScaledBitmap(bIcon, 60, 60, false);
//
//        for(int i=0;i<stasiuns.size();i++){
//            LatLng llStasiun = new LatLng(stasiuns.get(i).getLatitude(),stasiuns.get(i).getLongtitude());
//            String nama = stasiuns.get(i).getNamaStasiun();
//            mMap.addMarker(new MarkerOptions().position(llStasiun).title(nama).icon(BitmapDescriptorFactory.fromBitmap(bIcon)));
//        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng posisi = marker.getPosition();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(posisi)      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                return true;
            }
        });
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (loc != null && mMap != null) {
            LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(ll)      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static MapMenu getInstance(){
        return instance;
    }

    private ArrayList<String> namaKereta;
    private ArrayList<String> namaJadwal;

    public void setAllSpinner(){
        spinnerKereta = findViewById(R.id.spinnerKereta);
        ArrayAdapter<String> adapterKereta = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, namaKereta);
        spinnerKereta.setAdapter(adapterKereta);

        spinnerKereta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                MapMenu.getInstance().namaJadwal.clear();
                MapMenu.getInstance().idxKereta = i;
                Kereta selectedKereta = Menu.getInstance().getKereta().get(i);
                ArrayList<Jadwal> jadwals = selectedKereta.getJadwals();
                for(Jadwal j : jadwals){
                    Stasiun s = j.getStasiun();
                    String namaStasiun = s.getNamaStasiun();
                    String jamDatang = j.getJamDatang();
                    String jamPergi = j.getJamPergi();
                    MapMenu.getInstance().namaJadwal.add(namaStasiun+" (datang:"+jamDatang+", pergi:"+jamPergi+")");
                    MapMenu.getInstance().spinnerStasiun.setAdapter(new ArrayAdapter<String>(
                            MapMenu.getInstance(),R.layout.support_simple_spinner_dropdown_item,namaJadwal
                    ));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerStasiun = findViewById(R.id.spinnerStasiun);
        spinnerStasiun.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(loc != null) {
                    Kereta selectedKereta = Menu.getInstance().getKereta().get(MapMenu.getInstance().idxKereta);
                    Jadwal selectedJadwal = selectedKereta.getJadwals().get(i);
                    latNext = selectedJadwal.getStasiun().getLatitude();
                    langNext = selectedJadwal.getStasiun().getLongtitude();
                    latCurr = loc.getLatitude();
                    langCurr = loc.getLongitude();
                    jarak = (new DistanceCalculation(latCurr, latNext, langCurr, langNext)).getJarak();
                    tvJarak.setText(new DecimalFormat("#.##").format(jarak)+" km");

                    Bitmap bIcon=((BitmapDrawable)getResources().getDrawable(R.drawable.train_icon)).getBitmap();
                    bIcon = Bitmap.createScaledBitmap(bIcon, 60, 60, false);

                    ArrayList listOfStasiun = new ArrayList();
                    for(Jadwal j : selectedKereta.getJadwals()){
                        listOfStasiun.add(j.getStasiun());
                        LatLng llStasiun = new LatLng(j.getStasiun().getLatitude(),j.getStasiun().getLongtitude());
                        mMap.addMarker(new MarkerOptions().position(llStasiun).title(j.getStasiun().getNamaStasiun()).icon(BitmapDescriptorFactory.fromBitmap(bIcon)));
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
