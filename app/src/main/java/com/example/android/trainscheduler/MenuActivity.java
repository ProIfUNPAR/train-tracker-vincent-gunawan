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
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransitMode;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
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
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MenuActivity extends FragmentActivity
        implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DirectionCallback {
    public static double KECEPATAN_DEFAULT = 40.0;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GoogleApiClient mGoogleApiClient;
    private Location loc;
    private Spinner spinnerKereta, spinnerStasiun;
    private static MenuActivity instance;
    private int idxKereta = -1;
    private TextView tvJarak, tvSpeed, tvWaktu;
    private double langNext, langCurr, latNext, latCurr, jarak;
    private int stationPos;
    private float speed;
    private ArrayList<String> namaKereta;
    private ArrayList<String> namaJadwal;
    private ArrayList<LatLng> directionList;
    private ArrayList<PolylineOptions> alPO;
    private MainPresenter presenter;
    private int banyakPolyline;
    private double jarakTotal;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_menu);
        this.tvJarak = findViewById(R.id.tv_jarak);
        this.tvJarak = findViewById(R.id.tv_jarak);
        this.tvSpeed = findViewById(R.id.tv_kecepatan);
        this.tvWaktu = findViewById(R.id.tv_waktu);

        this.langNext = 0;
        this.langCurr = 0;
        this.latNext = 0;
        this.latCurr = 0;
        this.instance = this;

        this.jarakTotal = 0;
        this.banyakPolyline = 0;

        this.namaKereta = new ArrayList<>();
        this.namaJadwal = new ArrayList<>();
        this.alPO = new ArrayList<>();
        this.setAllSpinner();

        this.presenter = new MainPresenter();

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
        locationListener = new LocationListener() {
            @Override
            //waktu lokasinya pindah
            public void onLocationChanged(Location location) {
                loc = location;

                latCurr = loc.getLatitude();
                langCurr = loc.getLongitude();
                jarak = (new DistanceCalculation(latCurr, latNext, langCurr, langNext)).getJarak();
                tvJarak.setText(new DecimalFormat("#.##").format(jarak) + " km");

                speed = loc.getSpeed() * 3.6f;
                tvSpeed.setText(String.format("%.2f km/jam", (speed)));
                android.util.Log.d("speed", speed + "");
                int[] waktu = presenter.hitungWaktu(jarak, speed);
                tvWaktu.setText(presenter.formatWaktu(waktu[0], waktu[1], waktu[2]));


                stationPos = spinnerStasiun.getSelectedItemPosition();
                if (jarak <= 0.1) {
                    stationPos++;
                    if (stationPos < spinnerStasiun.getCount()) {
                        makeNotif(spinnerStasiun.getSelectedItem().toString());
                        spinnerStasiun.setSelection(stationPos);
                    } else {
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
                loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (loc != null) {
                    LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(ll)      // Sets the center of the map to location user
                            .zoom(17)                   // Sets the zoom
                            .bearing(0)                // Sets the orientation of the camera
                            .tilt(0)                   // Sets the tilt of the camera
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
                        .bearing(0)                // Sets the orientation of the camera
                        .tilt(0)                   // Sets the tilt of the camera
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
                    .bearing(0)                // Sets the orientation of the camera
                    .tilt(0)                   // Sets the tilt of the camera
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
                        .bearing(0)                // Sets the orientation of the camera
                        .tilt(0)                   // Sets the tilt of the camera
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
                    .bearing(0)                // Sets the orientation of the camera
                    .tilt(0)                   // Sets the tilt of the camera
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

    public static MenuActivity getInstance() {
        return instance;
    }

    public void setAllSpinner() {
        for (Kereta k : LoadingActivity.getInstance().getKereta()) {
            namaKereta.add(k.getNamaKereta() + " (Tujuan:" + k.getJadwals().get(k.getJadwals().size() - 1).getStasiun().getNamaStasiun() + ")");
        }
        spinnerKereta = findViewById(R.id.spinnerKereta);
        ArrayAdapter<String> adapterKereta = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, namaKereta);
        spinnerKereta.setAdapter(adapterKereta);

        spinnerKereta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                alPO = new ArrayList<>();
                banyakPolyline = 0;
                presenter.resetResult();

                MenuActivity.getInstance().namaJadwal.clear();
                MenuActivity.getInstance().idxKereta = i;
                Kereta selectedKereta = LoadingActivity.getInstance().getKereta().get(i);
                ArrayList<Jadwal> jadwals = selectedKereta.getJadwals();
                for (Jadwal j : jadwals) {
                    Stasiun s = j.getStasiun();
                    String namaStasiun = s.getNamaStasiun();
                    String jamDatang = j.getJamDatang();
                    String jamPergi = j.getJamPergi();
                    MenuActivity.getInstance().namaJadwal.add(namaStasiun + " (datang:" + jamDatang + ", pergi:" + jamPergi + ")");
                    MenuActivity.getInstance().spinnerStasiun.setAdapter(new ArrayAdapter<String>(
                            MenuActivity.getInstance(), R.layout.support_simple_spinner_dropdown_item, namaJadwal
                    ));
                }

                LatLng ll = new LatLng(jadwals.get(0).getStasiun().getLatitude(), jadwals.get(0).getStasiun().getLongtitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(ll)      // Sets the center of the map to location user
                        .zoom(8)                   // Sets the zoom
                        .bearing(0)                // Sets the orientation of the camera
                        .tilt(0)                   // Sets the tilt of the camera
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                setAllMarkerAndLine(selectedKereta);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerStasiun = findViewById(R.id.spinnerStasiun);
        spinnerStasiun.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.d("onItemSelected","dipanggil teru/s");
                if (loc != null) {
                    Kereta selectedKereta = LoadingActivity.getInstance().getKereta().get(MenuActivity.getInstance().idxKereta);
                    Jadwal selectedJadwal = selectedKereta.getJadwals().get(i);
                    latNext = selectedJadwal.getStasiun().getLatitude();
                    langNext = selectedJadwal.getStasiun().getLongtitude();
                    latCurr = loc.getLatitude();
                    langCurr = loc.getLongitude();
                    jarak = (new DistanceCalculation(latCurr, latNext, langCurr, langNext)).getJarak();
                    LatLng latLngCur = new LatLng(latCurr, langCurr);
                    LatLng latLngNxt = new LatLng(latNext, langNext);

                    GoogleDirection.withServerKey(getString(R.string.google_direction_api)).
                            from(latLngCur).
                            to(latLngNxt).
                            transitMode(TransitMode.RAIL).
                            transportMode(TransportMode.TRANSIT).
                            execute(new DirectionCallback() {
                                @Override
                                public void onDirectionSuccess(Direction direction, String rawBody) {
                                    Route route = direction.getRouteList().get(0);
                                    Leg leg = route.getLegList().get(0);
                                    jarak = Double.parseDouble(leg.getDistance().getValue());
                                }
                                @Override
                                public void onDirectionFailure(Throwable t) {
                                    Toast toast = Toast.makeText(MenuActivity.getInstance(),"Direction Failed",Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            });
                    tvJarak.setText(new DecimalFormat("#.##").format(jarak) + " km");

                    int[] waktu = presenter.hitungWaktu(jarak, KECEPATAN_DEFAULT);
                    tvWaktu.setText(presenter.formatWaktu(waktu[0], waktu[1], waktu[2]));
//                    setAllMarkerAndLine(selectedKereta);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    public void makeNotif(String station) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.train_icon_black)
                        .setContentTitle("Perhatian")
                        .setTimeoutAfter(3000)
                        .setVibrate(new long[]{1000, 1000})
                        .setContentText("Anda Telah tiba di stasiun " + station);
        int mNotificationId = 1;
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private void setAllMarkerAndLine(Kereta selectedKereta) {
        mMap.clear();

        Bitmap blackIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.train_icon_black)).getBitmap();
        blackIcon = Bitmap.createScaledBitmap(blackIcon, 80, 80, false);
        Bitmap redIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.train_icon_red)).getBitmap();
        redIcon = Bitmap.createScaledBitmap(redIcon, 120, 120, false);
        Bitmap greenIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.train_icon_green)).getBitmap();
        greenIcon = Bitmap.createScaledBitmap(greenIcon, 120, 120, false);
        ArrayList<Stasiun> listOfStasiun = new ArrayList();
        ArrayList<Jadwal> tempJadwals = selectedKereta.getJadwals();
        for (int j = 0; j < tempJadwals.size(); j++) {
            listOfStasiun.add(tempJadwals.get(j).getStasiun());
            LatLng llStasiun = new LatLng(tempJadwals.get(j).getStasiun().getLatitude(), tempJadwals.get(j).getStasiun().getLongtitude());
            if (j == 0) {
                mMap.addMarker(new MarkerOptions().position(llStasiun).title(tempJadwals.get(j).getStasiun().getNamaStasiun()).icon(BitmapDescriptorFactory.fromBitmap(greenIcon)));
            } else if (j == tempJadwals.size() - 1) {
                mMap.addMarker(new MarkerOptions().position(llStasiun).title(tempJadwals.get(j).getStasiun().getNamaStasiun()).icon(BitmapDescriptorFactory.fromBitmap(redIcon)));
            } else {
                mMap.addMarker(new MarkerOptions().position(llStasiun).title(tempJadwals.get(j).getStasiun().getNamaStasiun()).icon(BitmapDescriptorFactory.fromBitmap(blackIcon)));
            }
        }
        for (int j = 0; j < listOfStasiun.size() - 1; j++) {
            LatLng currLatLng = new LatLng(listOfStasiun.get(j).getLatitude(), listOfStasiun.get(j).getLongtitude());
            LatLng nextLatLng = new LatLng(listOfStasiun.get(j + 1).getLatitude(), listOfStasiun.get(j + 1).getLongtitude());
            GoogleDirection.withServerKey(getString(R.string.google_direction_api)).
                    from(currLatLng).
                    to(nextLatLng).
                    transportMode(TransportMode.TRANSIT).
                    transitMode(TransitMode.RAIL).
                    execute(this);
        }
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        Route route = direction.getRouteList().get(0);
        Leg leg = route.getLegList().get(0);
        directionList = leg.getDirectionPoint();
        PolylineOptions po = DirectionConverter.createPolyline(this, directionList, 3, Color.RED);
        mMap.addPolyline(po);

        alPO.add(po);
        banyakPolyline++;
        Kereta currentKereta = LoadingActivity.getInstance().getKereta().get(spinnerKereta.getSelectedItemPosition());
        if (banyakPolyline == currentKereta.getJadwals().size() - 1) {
            presenter.checkNearbyPolyline(alPO, loc);

            //DI LUAR POLYLINE
            if (presenter.getNearbyIndex() == -1) {

            }
            //DI DALAM POLYLINE
            else {

            }
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Toast toast = Toast.makeText(MenuActivity.getInstance(),"Direction Failed",Toast.LENGTH_LONG);
        toast.show();
    }
}
