package com.example.android.trainscheduler;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.android.gms.location.LocationRequest.PRIORITY_NO_POWER;
import static java.util.Collections.sort;

public class MenuActivity extends FragmentActivity
        implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    public static double KECEPATAN_DEFAULT = 60.0;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GoogleApiClient mGoogleApiClient;
    private Location loc;
    private Spinner spinnerKereta, spinnerStasiun;
    private static MenuActivity instance;
    private int idxKereta = -1;
    private TextView tvJarak,tvSpeed,tvWaktu;
    private double langNext,langCurr,latNext,latCurr,jarak;
    private int stationPos;
    private ArrayList<String> namaKereta;
    private ArrayList<String> namaJadwal;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocationRequest req=new LocationRequest();
        req.setPriority(PRIORITY_NO_POWER);
//        req.setInterval(5*60*1000);
//        req.setFastestInterval(60*1000);
//        req.setMaxWaitTime(60*60*1000);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_menu);
        this.instance = this;
        this.tvJarak = findViewById(R.id.tv_jarak);
        this.tvJarak = findViewById(R.id.tv_jarak);
        this.tvSpeed = findViewById(R.id.tv_kecepatan);
        this.tvWaktu = findViewById(R.id.tv_waktu);
        this.langNext = 0;
        this.langCurr = 0;
        this.latNext = 0;
        this.latCurr = 0;

        ArrayList<Kereta> tempKereta = LoadingActivity.getInstance().getKereta();
        this.namaKereta = new ArrayList<>();
        for(Kereta k : tempKereta){
            namaKereta.add(k.getNamaKereta()+" ("+k.getJadwals().get(0).getStasiun().getNamaStasiun()+")");
        }
        this.namaJadwal = new ArrayList<>();
        this.setAllSpinner();

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
                loc = location;

                latCurr = loc.getLatitude();
                langCurr = loc.getLongitude();
                jarak = (new DistanceCalculation(latCurr,latNext,langCurr,langNext)).getJarak();
                tvJarak.setText(new DecimalFormat("#.##").format(jarak)+" km");

                loc.getLatitude();
                float speed = loc.getSpeed();
                tvSpeed.setText(String.format("%.2f km/jam", (speed*3.6)));

                int temp = (int) (jarak/speed);
                int jam = 0;
                if(speed <= 0){
                    jam = (int)Math.floor((jarak/KECEPATAN_DEFAULT)/60);
                }else{
                    jam = (int) Math.floor((jarak/speed)/60);
                }
                int menit = (temp % 1) * 60;
                tvWaktu.setText(formatWaktu(jam,menit));

                stationPos = spinnerStasiun.getSelectedItemPosition();
                if (jarak<=0.1){
                    stationPos++;
                    if (stationPos < spinnerStasiun.getCount()){
                        makeNotif(spinnerStasiun.getSelectedItem().toString());
                        spinnerStasiun.setSelection(stationPos);
                    }
                    else {
                        makeNotif("terakhir kereta ini!");
                    }
                }

            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }
            @Override
            //waktu GPS on
            public void onProviderEnabled(String s) {
                mMap.setMyLocationEnabled(true);
//                loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                Log.d("loclocloc",""+(loc==null));
                if (loc != null) {
                    LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(ll)      // Sets the center of the map to location user
                            .zoom(17)                   // Sets the zoom
                            .bearing(0)                // Sets the orientation of the camera to east
                            .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
            @Override
            //waktu GPS off
            public void onProviderDisabled(String s) {
            }
        };

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, locationListener);
//        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
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
        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                Log.d("loclocloc",""+(loc==null));
                LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(ll)      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .bearing(0)                // Sets the orientation of the camera to east
                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                return true;
            }
        });
        mMap.setMyLocationEnabled(true);
        if (loc != null) {
            //label.setText("");
            // label.append("\n " + loc.getLatitude() + " " + loc.getLongitude());
            LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(ll)      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng posisi = marker.getPosition();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(posisi)      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .bearing(0)                // Sets the orientation of the camera to east
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
        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (loc != null && mMap != null) {
            LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(ll)      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
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

    public static MenuActivity getInstance(){
        return instance;
    }

    public void setAllSpinner(){
        spinnerKereta = findViewById(R.id.spinnerKereta);
        ArrayAdapter<String> adapterKereta = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, namaKereta);
        spinnerKereta.setAdapter(adapterKereta);

        spinnerKereta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                MenuActivity.getInstance().namaJadwal.clear();
                MenuActivity.getInstance().idxKereta = i;
                Kereta selectedKereta = LoadingActivity.getInstance().getKereta().get(i);
                ArrayList<Jadwal> jadwals = selectedKereta.getJadwals();
                for(Jadwal j : jadwals){
                    Stasiun s = j.getStasiun();
                    String namaStasiun = s.getNamaStasiun();
                    String jamDatang = j.getJamDatang();
                    String jamPergi = j.getJamPergi();
                    MenuActivity.getInstance().namaJadwal.add(namaStasiun+" (datang:"+jamDatang+", pergi:"+jamPergi+")");
                    MenuActivity.getInstance().spinnerStasiun.setAdapter(new ArrayAdapter<String>(
                            MenuActivity.getInstance(),R.layout.support_simple_spinner_dropdown_item,namaJadwal
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
//                Log.d("onItemSelected","dipanggil terus");
                if(loc != null) {
                    Kereta selectedKereta = LoadingActivity.getInstance().getKereta().get(MenuActivity.getInstance().idxKereta);
                    Jadwal selectedJadwal = selectedKereta.getJadwals().get(i);
                    latNext = selectedJadwal.getStasiun().getLatitude();
                    langNext = selectedJadwal.getStasiun().getLongtitude();
                    latCurr = loc.getLatitude();
                    langCurr = loc.getLongitude();
                    jarak = (new DistanceCalculation(latCurr, latNext, langCurr, langNext)).getJarak();
                    tvJarak.setText(new DecimalFormat("#.##").format(jarak)+" km");

                    double temp =jarak/KECEPATAN_DEFAULT;
                    int jam = (int)Math.floor(temp);
                    int menit = (int)((temp % 1) * 60);
//                    Log.d("waktuwaktu",temp+" "+jam+" "+menit+" "+jarak+" "+KECEPATAN_DEFAULT);
                    tvWaktu.setText(formatWaktu(jam,menit));

                    mMap.clear();
                    Bitmap blackIcon=((BitmapDrawable)getResources().getDrawable(R.drawable.train_icon_black)).getBitmap();
                    blackIcon = Bitmap.createScaledBitmap(blackIcon, 80, 80, false);
                    Bitmap redIcon=((BitmapDrawable)getResources().getDrawable(R.drawable.train_icon_red)).getBitmap();
                    redIcon = Bitmap.createScaledBitmap(redIcon, 120, 120, false);
                    Bitmap greenIcon=((BitmapDrawable)getResources().getDrawable(R.drawable.train_icon_green)).getBitmap();
                    greenIcon = Bitmap.createScaledBitmap(greenIcon, 120, 120, false);
                    ArrayList<Stasiun> listOfStasiun = new ArrayList();
                    ArrayList<Jadwal>  tempJadwals = selectedKereta.getJadwals();
                    for(int j=0;j<tempJadwals.size();j++){
                        listOfStasiun.add(tempJadwals.get(j).getStasiun());
                        LatLng llStasiun = new LatLng(tempJadwals.get(j).getStasiun().getLatitude(),tempJadwals.get(j).getStasiun().getLongtitude());
                        if(j==0) {
                            mMap.addMarker(new MarkerOptions().position(llStasiun).title(tempJadwals.get(j).getStasiun().getNamaStasiun()).icon(BitmapDescriptorFactory.fromBitmap(greenIcon)));
                        }else if(j==tempJadwals.size()-1){
                            mMap.addMarker(new MarkerOptions().position(llStasiun).title(tempJadwals.get(j).getStasiun().getNamaStasiun()).icon(BitmapDescriptorFactory.fromBitmap(redIcon)));
                        }else{
                            mMap.addMarker(new MarkerOptions().position(llStasiun).title(tempJadwals.get(j).getStasiun().getNamaStasiun()).icon(BitmapDescriptorFactory.fromBitmap(blackIcon)));
                        }
                    }
                    for(int j = 0;j<listOfStasiun.size()-1;j++){
                        LatLng currLatLng = new LatLng(listOfStasiun.get(j).getLatitude(),listOfStasiun.get(j).getLongtitude());
                        LatLng nextLatLng = new LatLng(listOfStasiun.get(j+1).getLatitude(),listOfStasiun.get(j+1).getLongtitude());
                        mMap.addPolyline(new PolylineOptions()
                                .add(currLatLng, nextLatLng)
                                .width(5)
                                .color(Color.RED));
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    @Override
    public void onBackPressed() { }

    public void makeNotif(String station){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.train_icon_black)
                        .setContentTitle("Perhatian")
                        .setTimeoutAfter(3000)
                        .setVibrate(new long[] {1000,1000})
                        .setContentText("Anda Telah tiba di stasiun " + station);
        int mNotificationId = 1;
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId,mBuilder.build());
    }
    public String formatWaktu(int jam, int menit){
        String sJam = (jam < 10)? "0"+jam : jam+"";
        String sMenit = (menit < 10)? "0"+menit : (""+menit).substring(0,2);
        return sJam+":"+sMenit+":00";
    }
}
