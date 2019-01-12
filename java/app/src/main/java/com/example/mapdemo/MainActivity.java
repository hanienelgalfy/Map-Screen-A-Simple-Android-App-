/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mapdemo;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.Volley;
import com.android.volley.Request;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;


/**
 * This shows how to create a simple activity with a map and a marker on the map.
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private RequestQueue mQueue;
    private GoogleMap myMap;
    private static final int LocationRequest=500;
    String name;
    String latitude;
    String longitude;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mQueue = Volley.newRequestQueue(this);
        jsonParse();

    }

    private void jsonParse() {

        String url = "https://staging.raye7.com/android_interns/index";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("The Response", response.toString());
                        JSONObject source;
                        JSONObject destination;
                        JSONArray users;
                        try {
                            //Source "City Stars"
                            source = response.getJSONObject("source");
                            name = source.getString("name");
                            latitude = source.getString("latitude");
                            longitude = source.getString("longitude");
                            double latitudeD = Double.parseDouble(latitude);
                            double longitudeD = Double.parseDouble(longitude);

                            myMap.addMarker(new MarkerOptions().position(new LatLng(latitudeD, longitudeD)).title(name));

                            //Destination "Raye7"
                            destination = response.getJSONObject("destinaton");
                            String nameDest = destination.getString("name");
                            Log.e("DESTINATION", destination.toString());
                            String latitudeDest = destination.getString("latitude");
                            String longitudeDest = destination.getString("longitude");
                            double latitudeDestD = Double.parseDouble(latitudeDest);
                            double longitudeDestD = Double.parseDouble(longitudeDest);
                            myMap.addMarker(new MarkerOptions().position(new LatLng(latitudeDestD, longitudeDestD)).title(nameDest));


                            onMapReady(myMap);

                            //USERS ON THE MAP
                            users = response.getJSONArray("users");
                            for(int i =0; i < users.length(); i++) {
                                JSONObject user1 = users.getJSONObject(i);
                                String nameu1 = user1.getString("name");
                                JSONObject coordinatesu1 = user1.getJSONObject("coordinates");
                                String longitudeu1 = coordinatesu1.getString("longitude");
                                String latitudeu1 = coordinatesu1.getString("latitude");
                                double longitudeu1D = Double.parseDouble(longitudeu1);
                                double latitudeu1D = Double.parseDouble(latitudeu1);
                                myMap.addMarker(new MarkerOptions().position(new LatLng(latitudeu1D, longitudeu1D)).title(nameu1));
                            }


                        } catch (JSONException e) {
                            //   Log.d("HA B2AAAAAA" , name);
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }
    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we
     * just add a marker near Africa.
     */

    public void onMapReady(GoogleMap map) {
   myMap = map;
        myMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LocationRequest);
            return;
        }
        myMap.setMyLocationEnabled(true);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LocationRequest:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    myMap.setMyLocationEnabled(true);
                }
                break;
        }

    }
}
