package com.example.mapdemo;


import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskModel extends ViewModel {

    public MutableLiveData<Pair<LatLng, String>> posName = new MutableLiveData<>();
    public MutableLiveData<Pair<LatLng, String>> posUName = new MutableLiveData<>();
    public MutableLiveData<PolylineOptions> polylineOptions = new MutableLiveData<>();
    private LatLng position;

    private RequestQueue mQueue;

    public void setQueue(RequestQueue q) {
        this.mQueue = q;
    }


    private LatLng parseLocation(JSONObject sourceOrDestination) {

        try {
            String name = sourceOrDestination.getString("name");
            Double Latitude = Double.parseDouble(sourceOrDestination.getString("latitude"));
            Double Longitude = Double.parseDouble(sourceOrDestination.getString("longitude"));
            position = new LatLng(Latitude, Longitude);
            posName.setValue(new Pair<>(position, name));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return position;

    }


    private void parseOtherUsersLocations(JSONArray users) {
        try {
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                String userName = user.getString("name");
                JSONObject coordinates = user.getJSONObject("coordinates");
                Double LongitudeUsers =Double.parseDouble(coordinates.getString("longitude"));
                Double LatitudeUsers =Double.parseDouble(coordinates.getString("latitude"));
                position = new LatLng(LatitudeUsers, LongitudeUsers);
                posUName.setValue(new Pair<>(position, userName));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void callAPI() {
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

                            String dir_url = getRequestUrl(origin, desti);
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


    private String getRequestUrl(LatLng origin, LatLng dest) {
        String str_org = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String param = str_org + "&" + str_dest + "&" + sensor + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param +
                "&key=AIzaSyCYZGqi-oFS5uT-2Q4TY9ZzjuIkB-_cMgg";
        return url;
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    public class TaskDirectionRequest extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("ay7aga", s);
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
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
            Log.d("Dakhlt al method de " , "");

            ArrayList points;
            // PolylineOptions polylineOptions = new PolylineOptions();

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));
                    points.add(new LatLng(lat, lon));
                }
                PolylineOptions options = new PolylineOptions();
                options.addAll(points);
                options.width(15);
                options.color(Color.BLUE);
                options.geodesic(true);
                polylineOptions.setValue(options);
            }

        }
    }


}
