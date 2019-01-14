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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private RequestQueue mQueue;
    private GoogleMap myMap;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mQueue = Volley.newRequestQueue(this);
        callAPI();

    }

    private LatLng parseLocation(JSONObject sourceOrDestination ){

        try {
            String Name = sourceOrDestination.getString("name");
            double Latitude = Double.parseDouble(sourceOrDestination.getString("latitude"));
            double Longitude = Double.parseDouble(sourceOrDestination.getString("longitude"));
            LatLng position = new LatLng(Latitude, Longitude);
            myMap.addMarker(new MarkerOptions().position(position).title(Name));
            return position;
        }
        catch (JSONException e) {

            e.printStackTrace();
        }
        return null;
    }


    private void parseOtherUsersLocations(JSONArray users){
        try{
            for(int i = 0 ; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                String userName = user.getString("name");
                JSONObject coordinates = user.getJSONObject("coordinates");
                double longitude = Double.parseDouble(coordinates.getString("longitude"));
                double latitude = Double.parseDouble(coordinates.getString("latitude"));
                myMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(userName));
            }

        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }

    }

    private void callAPI() {
        String url = "https://staging.raye7.com/android_interns/index";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("The Response", response.toString());

                        try {
                            JSONObject source = response.getJSONObject("source"); //source
                            LatLng origin = parseLocation(source);

                            JSONObject destination = response.getJSONObject("destinaton"); //destination
                            LatLng desti = parseLocation(destination);

                            String dir_url = getRequestUrl(origin , desti);
                            TaskDirectionRequest taskDirectionRequest = new TaskDirectionRequest();
                            taskDirectionRequest.execute(dir_url);

                            JSONArray users = response.getJSONArray("users"); //other users
                            parseOtherUsersLocations(users);

                        } catch (JSONException e) {

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


    public void onMapReady(GoogleMap map) {
        myMap = map;
    }

    private String getRequestUrl(LatLng origin ,LatLng dest){
        String str_org = "origin=" + origin.latitude +","+origin.longitude;
        String str_dest = "destination=" + dest.latitude+","+dest.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String param = str_org +"&" + str_dest + "&" +sensor+"&" +mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param+
                "&key=AIzaSyBAWV6fcAFbvdBxmc5bVWQ5FIM3tPXMr5E";
        return url;
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection= null;
        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream= httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while((line = bufferedReader.readLine())!= null){
                stringBuffer.append(line);
            }
            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(inputStream !=null){
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }return  responseString;
    }
    @SuppressLint("MissingPermission")


    public class TaskDirectionRequest extends AsyncTask<String , Void , String>{
        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String , Void ,List<List<HashMap<String, String>>> >{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String , String>>> routes= null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionParser directionParser = new DirectionParser();
                routes = directionParser.parse(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            ArrayList points = null;
            PolylineOptions polylineOptions = new PolylineOptions();
            for(List<HashMap<String, String>>path :lists){
                points = new ArrayList();
                for(HashMap<String, String> point : path){
                    double lat =Double.parseDouble(point.get("lat"));
                    double lon =Double.parseDouble(point.get("lon"));
                    points.add(new LatLng(lat,lon));
                }
                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);

            }
            if(polylineOptions!=null){
                myMap.addPolyline(polylineOptions);

            }else{
                Toast.makeText(getApplicationContext() , "Direction not found" , Toast.LENGTH_SHORT).show();
            }
        }
    }
}