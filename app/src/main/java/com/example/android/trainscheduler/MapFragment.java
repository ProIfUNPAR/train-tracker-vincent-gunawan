//package com.example.android.trainscheduler;
//
//import android.annotation.SuppressLint;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.model.CameraPosition;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//
///**
// * Created by toshiba pc on 3/1/2018.
// */
//
//public class MapFragment  extends Fragment implements
//        OnMapReadyCallback,
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener {
//
//    private GoogleMap mMap;
//    private LocationManager locationManager;
//    private LocationListener locationListener;
//    private GoogleApiClient mGoogleApiClient;
//    private Location loc;
//
//
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View v = inflater.inflate()
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//    }
//
//    @SuppressLint("MissingPermission")
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//
//        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
//            @Override
//            public boolean onMyLocationButtonClick() {
//                loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
////                Log.d("loclocloc",""+(loc==null));
//                LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
//                CameraPosition cameraPosition = new CameraPosition.Builder()
//                        .target(ll)      // Sets the center of the map to location user
//                        .zoom(17)                   // Sets the zoom
//                        .bearing(0)                // Sets the orientation of the camera to east
//                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
//                        .build();                   // Creates a CameraPosition from the builder
//                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//                return true;
//            }
//        });
//        mMap.setMyLocationEnabled(true);
//        if (loc != null) {
//            //label.setText("");
//            // label.append("\n " + loc.getLatitude() + " " + loc.getLongitude());
//            LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .target(ll)      // Sets the center of the map to location user
//                    .zoom(17)                   // Sets the zoom
//                    .bearing(0)                // Sets the orientation of the camera to east
//                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
//                    .build();                   // Creates a CameraPosition from the builder
//            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        }
//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                LatLng posisi = marker.getPosition();
//                CameraPosition cameraPosition = new CameraPosition.Builder()
//                        .target(posisi)      // Sets the center of the map to location user
//                        .zoom(17)                   // Sets the zoom
//                        .bearing(0)                // Sets the orientation of the camera to east
//                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
//                        .build();                   // Creates a CameraPosition from the builder
//                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//                return true;
//            }
//        });
//    }
//
//
//    @SuppressLint("MissingPermission")
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        if (loc != null && mMap != null) {
//            LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .target(ll)      // Sets the center of the map to location user
//                    .zoom(17)                   // Sets the zoom
//                    .bearing(0)                // Sets the orientation of the camera to east
//                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
//                    .build();                   // Creates a CameraPosition from the builder
//            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        }
//    }
//}
