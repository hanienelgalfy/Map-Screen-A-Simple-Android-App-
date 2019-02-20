package com.example.mapdemo;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;

import com.google.android.gms.maps.model.PolylineOptions;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private TaskModel taskModel;
    private GoogleMap myMap;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        taskModel = ViewModelProviders.of(this).get(TaskModel.class);

        RequestQueue mQueue = Volley.newRequestQueue(this);
        taskModel.setQueue(mQueue);
        initObservers();
        taskModel.callAPI();


    }

    void initObservers() {
        taskModel.posName.observe(this, new Observer<Pair<LatLng, String>>() {
            @Override
            public void onChanged(@Nullable Pair<LatLng, String> latLngStringPair) {
                showMarkerOnMap(latLngStringPair);

            }
        });

        taskModel.posUName.observe(this, new Observer<Pair<LatLng, String>>() {
            @Override
            public void onChanged(@Nullable Pair<LatLng, String> latLngStringPair) {
                showOtherUsers(latLngStringPair);

            }
        });

        taskModel.polylineOptions.observe(this, new Observer<PolylineOptions>() {
            @Override
            public void onChanged(@Nullable PolylineOptions polylineOptions) {
                Direction(polylineOptions);
            }
        });
    }

    public void onMapReady(GoogleMap map) {

        myMap = map;
    }

    private void showMarkerOnMap(Pair<LatLng, String> posName) {
        myMap.addMarker(new MarkerOptions().position(posName.first).title(posName.second));

    }

    private void showOtherUsers(Pair<LatLng, String> posUName) {
        myMap.addMarker(new MarkerOptions().position(posUName.first).title(posUName.second));

    }

    private void Direction(PolylineOptions polylineOptions) {

        myMap.addPolyline(polylineOptions);
    }

}