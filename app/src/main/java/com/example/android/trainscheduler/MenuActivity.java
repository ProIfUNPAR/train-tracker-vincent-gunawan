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
import android.util.Log;
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
import java.util.List;

public class MenuActivity extends FragmentActivity
        implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
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
    private MainPresenter presenter;
    private int banyakPolyline;
    private TextView tvHereToSelected, tvJarakTotal, tvWaktuTotal;
    private ArrayList<Stasiun> listStasiunCurrentTrain;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_menu);
        this.tvJarak = findViewById(R.id.tv_jarak);
        this.tvJarak = findViewById(R.id.tv_jarak);
        this.tvSpeed = findViewById(R.id.tv_kecepatan);
        this.tvWaktu = findViewById(R.id.tv_waktu);
        this.tvHereToSelected = findViewById(R.id.tv_here_to_selected);
        this.tvJarakTotal = findViewById(R.id.tv_jarak_total);
        this.tvWaktuTotal = findViewById(R.id.tv_waktu_total);

        this.langNext = 0;
        this.langCurr = 0;
        this.latNext = 0;
        this.latCurr = 0;
        this.instance = this;
        this.banyakPolyline = 0;
        this.listStasiunCurrentTrain = new ArrayList<>();
        this.namaKereta = new ArrayList<>();
        this.namaJadwal = new ArrayList<>();
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
                Log.d("LOCATION", "DIPANGGIL");
                loc = location;

                latCurr = loc.getLatitude();
                langCurr = loc.getLongitude();
                jarak = presenter.getJarak(latCurr, latNext, langCurr, langNext);
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

                if (banyakPolyline == listStasiunCurrentTrain.size() - 1) {
                    presenter.isPolylineNearby(loc);
                    int idx = presenter.getNearestStasiun();
                    //DI LUAR POLYLINE
                    if (idx == -1) {
                        idx = presenter.getNearestStasiun(listStasiunCurrentTrain, loc);
                        Stasiun nextStasiun = listStasiunCurrentTrain.get(idx);
                        double jarakTotal = presenter.getJarak(loc.getLatitude(), nextStasiun.getLatitude(), loc.getLongitude(), nextStasiun.getLongtitude());
                        for (int i = idx; i < listStasiunCurrentTrain.size() - 1; i++) {
                            Stasiun s1 = listStasiunCurrentTrain.get(i);
                            Stasiun s2 = listStasiunCurrentTrain.get(i + 1);
                            jarakTotal += presenter.getJarak(s1.getLatitude(), s2.getLatitude(), s1.getLongtitude(), s2.getLongtitude());
                            Log.d("JARAK", "" + jarakTotal);
                        }
                        tvJarakTotal.setText(new DecimalFormat("#.##").format(jarakTotal) + " km");

                        waktu = presenter.hitungWaktu(jarakTotal, KECEPATAN_DEFAULT);
                        tvWaktuTotal.setText(presenter.formatWaktu(waktu[0], waktu[1], waktu[2]));
                    }
                    //DI DALAM POLYLINE
                    else {
                        idx += 1;
                        Stasiun nextStasiun = listStasiunCurrentTrain.get(idx);
                        double jarakTotal = presenter.getJarak(loc.getLatitude(), nextStasiun.getLatitude(), loc.getLongitude(), nextStasiun.getLongtitude());
                        Log.d("JARAK", "" + jarakTotal);
                        for (int i = idx; i < listStasiunCurrentTrain.size() - 1; i++) {
                            Stasiun s1 = listStasiunCurrentTrain.get(i);
                            Stasiun s2 = listStasiunCurrentTrain.get(i + 1);
                            jarakTotal += presenter.getJarak(s1.getLatitude(), s2.getLatitude(), s1.getLongtitude(), s2.getLongtitude());
                            Log.d("JARAK", "" + jarakTotal);
                        }
                        tvJarakTotal.setText(new DecimalFormat("#.##").format(jarakTotal) + " km");

                        waktu = presenter.hitungWaktu(jarakTotal, KECEPATAN_DEFAULT);
                        tvWaktuTotal.setText(presenter.formatWaktu(waktu[0], waktu[1], waktu[2]));
                    }
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            //waktu GPS on
            public void onProviderEnabled(String s) {
                getBestLocation();
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
//        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        getBestLocation();
        tvHereToSelected.setText("Dari sini ke Stasiun " + LoadingActivity.getInstance().getKereta().get(0).getJadwals().get(0).getStasiun().getNamaStasiun());

        this.setAllSpinner();
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

    public Location getLoc() {
        return this.loc;
    }

    public Location getLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = locationManager.getLastKnownLocation(provider);

            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(false);
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
//        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        loc = getLastKnownLocation();
        getBestLocation();
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
            namaKereta.add(k.getNamaKereta() + " (" + k.getJadwals().get(0).getStasiun().getNamaStasiun() + " - " + k.getJadwals().get(k.getJadwals().size() - 1).getStasiun().getNamaStasiun() + ")");
        }
        spinnerKereta = findViewById(R.id.spinnerKereta);
        ArrayAdapter<String> adapterKereta = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, namaKereta);
        ListAdapter la = new ListAdapter(this, R.layout.support_simple_spinner_dropdown_item, namaKereta);
        spinnerKereta.setAdapter(la);

        spinnerKereta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                banyakPolyline = 0;

                MenuActivity.getInstance().namaJadwal.clear();
                MenuActivity.getInstance().idxKereta = i;
                Kereta selectedKereta = LoadingActivity.getInstance().getKereta().get(i);

                presenter.resetResult(selectedKereta.getJadwals().size());

                ArrayList<Jadwal> jadwals = selectedKereta.getJadwals();
                for (Jadwal j : jadwals) {
                    Stasiun s = j.getStasiun();
                    String namaStasiun = s.getNamaStasiun();
                    String jamDatang = j.getJamDatang();
                    String jamPergi = j.getJamPergi();
                    MenuActivity.getInstance().namaJadwal.add(namaStasiun + " (datang: " + jamDatang + " ,pergi: " + jamPergi + ")");
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
                Log.d("onItemSelected", "dipanggil teru/s");
                if (loc != null) {
                    Kereta selectedKereta = LoadingActivity.getInstance().getKereta().get(MenuActivity.getInstance().idxKereta);
                    Jadwal selectedJadwal = selectedKereta.getJadwals().get(i);
                    latNext = selectedJadwal.getStasiun().getLatitude();
                    langNext = selectedJadwal.getStasiun().getLongtitude();
                    latCurr = loc.getLatitude();
                    langCurr = loc.getLongitude();
                    jarak = presenter.getJarak(latCurr, latNext, langCurr, langNext);
                    tvJarak.setText(new DecimalFormat("#.##").format(jarak) + " km");

                    int[] waktu = presenter.hitungWaktu(jarak, KECEPATAN_DEFAULT);
                    tvWaktu.setText(presenter.formatWaktu(waktu[0], waktu[1], waktu[2]));

                    tvHereToSelected.setText("Dari sini ke Stasiun " + selectedJadwal.getStasiun().getNamaStasiun());
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

    public void getBestLocation() {
        SingleShotLocationProvider.requestSingleUpdate(this,
                new SingleShotLocationProvider.LocationCallback() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                        mMap.setMyLocationEnabled(true);
                        Log.d("Location", "my location is " + location.toString());
                        if (loc == null) {
                            loc = new Location("");
                        }
                        loc.setLatitude(location.latitude);
                        loc.setLongitude(location.longitude);
                    }
                });
    }

    public void getBestLocation2() {
        SingleShotLocationProvider.requestSingleUpdate(this,
                new SingleShotLocationProvider.LocationCallback() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                        mMap.setMyLocationEnabled(true);
                        Log.d("Location", "my location is " + location.toString());
                        if (loc == null) {
                            loc = new Location("");
                        }
                        loc.setLatitude(location.latitude);
                        loc.setLongitude(location.longitude);

                        Kereta selectedKereta = LoadingActivity.getInstance().getKereta().get(MenuActivity.getInstance().idxKereta);
                        Jadwal selectedJadwal = selectedKereta.getJadwals().get(spinnerStasiun.getSelectedItemPosition());
                        latNext = selectedJadwal.getStasiun().getLatitude();
                        langNext = selectedJadwal.getStasiun().getLongtitude();
                        latCurr = loc.getLatitude();
                        langCurr = loc.getLongitude();
                        jarak = presenter.getJarak(latCurr, latNext, langCurr, langNext);
                        tvJarak.setText(new DecimalFormat("#.##").format(jarak) + " km");

                        int[] waktu = presenter.hitungWaktu(jarak, KECEPATAN_DEFAULT);
                        tvWaktu.setText(presenter.formatWaktu(waktu[0], waktu[1], waktu[2]));

                        tvHereToSelected.setText("Dari sini ke Stasiun " + selectedJadwal.getStasiun().getNamaStasiun());


                        presenter.isPolylineNearby(loc);
                        int idx = presenter.getNearestStasiun();
                        //DI LUAR POLYLINE
                        if (idx == -1) {
                            idx = presenter.getNearestStasiun(listStasiunCurrentTrain, loc);
                            Log.d("JARAK INDEX", idx + "");
                            Stasiun nextStasiun = listStasiunCurrentTrain.get(idx);
                            double jarakTotal = presenter.getJarak(loc.getLatitude(), nextStasiun.getLatitude(), loc.getLongitude(), nextStasiun.getLongtitude());
                            for (int i = idx; i < listStasiunCurrentTrain.size() - 1; i++) {
                                Stasiun s1 = listStasiunCurrentTrain.get(i);
                                Stasiun s2 = listStasiunCurrentTrain.get(i + 1);
                                jarakTotal += presenter.getJarak(s1.getLatitude(), s2.getLatitude(), s1.getLongtitude(), s2.getLongtitude());
                                Log.d("JARAK", "" + jarakTotal);
                            }
                            tvJarakTotal.setText(new DecimalFormat("#.##").format(jarakTotal) + " km");

                            waktu = presenter.hitungWaktu(jarakTotal, KECEPATAN_DEFAULT);
                            tvWaktuTotal.setText(presenter.formatWaktu(waktu[0], waktu[1], waktu[2]));
                        }
                        //DI DALAM POLYLINE
                        else {
                            idx += 1;
                            Stasiun nextStasiun = listStasiunCurrentTrain.get(idx);
                            double jarakTotal = presenter.getJarak(loc.getLatitude(), nextStasiun.getLatitude(), loc.getLongitude(), nextStasiun.getLongtitude());
                            Log.d("JARAK", "" + jarakTotal);
                            for (int i = idx; i < listStasiunCurrentTrain.size() - 1; i++) {
                                Stasiun s1 = listStasiunCurrentTrain.get(i);
                                Stasiun s2 = listStasiunCurrentTrain.get(i + 1);
                                jarakTotal += presenter.getJarak(s1.getLatitude(), s2.getLatitude(), s1.getLongtitude(), s2.getLongtitude());
                                Log.d("JARAK", "" + jarakTotal);
                            }
                            tvJarakTotal.setText(new DecimalFormat("#.##").format(jarakTotal) + " km");

                            waktu = presenter.hitungWaktu(jarakTotal, KECEPATAN_DEFAULT);
                            tvWaktuTotal.setText(presenter.formatWaktu(waktu[0], waktu[1], waktu[2]));
                        }
                    }
                });
    }

    private void setAllMarkerAndLine(Kereta selectedKereta) {
        mMap.clear();
        listStasiunCurrentTrain.clear();

        Bitmap blackIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.train_icon_black)).getBitmap();
        blackIcon = Bitmap.createScaledBitmap(blackIcon, 80, 80, false);
        Bitmap redIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.train_icon_red)).getBitmap();
        redIcon = Bitmap.createScaledBitmap(redIcon, 120, 120, false);
        Bitmap greenIcon = ((BitmapDrawable) getResources().getDrawable(R.drawable.train_icon_green)).getBitmap();
        greenIcon = Bitmap.createScaledBitmap(greenIcon, 120, 120, false);
        ArrayList<Jadwal> tempJadwals = selectedKereta.getJadwals();
        for (int j = 0; j < tempJadwals.size(); j++) {
            listStasiunCurrentTrain.add(tempJadwals.get(j).getStasiun());
            LatLng llStasiun = new LatLng(tempJadwals.get(j).getStasiun().getLatitude(), tempJadwals.get(j).getStasiun().getLongtitude());
            if (j == 0) {
                mMap.addMarker(new MarkerOptions().position(llStasiun).title(tempJadwals.get(j).getStasiun().getNamaStasiun()).icon(BitmapDescriptorFactory.fromBitmap(greenIcon)));
            } else if (j == tempJadwals.size() - 1) {
                mMap.addMarker(new MarkerOptions().position(llStasiun).title(tempJadwals.get(j).getStasiun().getNamaStasiun()).icon(BitmapDescriptorFactory.fromBitmap(redIcon)));
            } else {
                mMap.addMarker(new MarkerOptions().position(llStasiun).title(tempJadwals.get(j).getStasiun().getNamaStasiun()).icon(BitmapDescriptorFactory.fromBitmap(blackIcon)));
            }
        }
        for (int j = 0; j < listStasiunCurrentTrain.size() - 1; j++) {
            LatLng currLatLng = new LatLng(listStasiunCurrentTrain.get(j).getLatitude(), listStasiunCurrentTrain.get(j).getLongtitude());
            LatLng nextLatLng = new LatLng(listStasiunCurrentTrain.get(j + 1).getLatitude(), listStasiunCurrentTrain.get(j + 1).getLongtitude());
            final int finalJ = j;
            GoogleDirection.withServerKey(getString(R.string.google_direction_api)).
                    from(currLatLng).
                    to(nextLatLng).
                    transportMode(TransportMode.TRANSIT).
                    transitMode(TransitMode.RAIL).
                    alternativeRoute(false).
                    execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            Log.d("LOCATION", "DIPANGGIL1");
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);
                            directionList = leg.getDirectionPoint();
                            PolylineOptions po = DirectionConverter.createPolyline(MenuActivity.getInstance(), directionList, 3, Color.RED);
                            mMap.addPolyline(po);
                            presenter.setPanjangRute(finalJ, po);
                            banyakPolyline++;

                            if (banyakPolyline == listStasiunCurrentTrain.size() - 1) {
                                getBestLocation2();
                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                            Toast toast = Toast.makeText(MenuActivity.getInstance(), "Direction Failed", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
        }
    }
}
