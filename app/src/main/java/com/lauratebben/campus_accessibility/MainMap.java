package com.lauratebben.campus_accessibility;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<LatLng> markers = new ArrayList<LatLng>();
    private double lat, lon;
    boolean endClick1 = false, endClick2 = false;
    String description, title;
    String httpResponse = null;

    Thread ithread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private LatLng getMyLocation() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        // if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        return new LatLng(39.132456, -84.515691);
       /* }
        Location location = manager.getLastKnownLocation(manager.getBestProvider(criteria, false));
        lat = location.getLatitude(); lon = location.getLongitude();
        return new LatLng(location.getLatitude(), location.getLongitude());*/
    }

    private void getInput(final Marker m) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("How do you get around this?");
        final EditText desc = new EditText(this);
        desc.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(desc);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                description = desc.getText().toString();
                m.setSnippet(description);
                endClick1 = true;
                if (endClick1 && endClick2) {
                    makeHTTPRequest();
                    endClick1 = false;
                    endClick2 = false;
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Describe the Location");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                title = input.getText().toString();
                m.setTitle(title);
                endClick2 = true;
                if (endClick1 && endClick2) {
                    makeHTTPRequest();
                    endClick1 = false;
                    endClick2 = false;
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void makeHTTPRequest() {
        String url = "http://54.152.111.115:21300/comment/";
        final String charset = "UTF-8";
        String longitude = Double.toString(lon);
        String latitude = Double.toString(lat);

        try {
            final String query = String.format("title=%s&description=%s&longitude=%s&latitude=%s",
                    URLEncoder.encode(title, charset),
                    URLEncoder.encode(description, charset),
                    URLEncoder.encode(longitude, charset),
                    URLEncoder.encode(latitude, charset));
            System.out.println(query);
            final URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run(){
                    try{
                        try (final OutputStream output = connection.getOutputStream()){
                            output.write(query.getBytes(charset));
                        }

                        InputStream response = connection.getInputStream();
                        System.out.println(response);
                    } catch (Exception e){e.printStackTrace();}
                }
            });
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void MakeHttpGetRequest() {

        ithread = new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    URLConnection connection = (new URL("http://54.152.111.115:21300/comment/")).openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.connect();

                    // Read and store the result line by line then return the entire string.
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder html = new StringBuilder();
                    for (String line; (line = reader.readLine()) != null; ) {
                        html.append(line);
                    }
                    in.close();

                    httpResponse = html.toString();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ithread.start();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady (GoogleMap googleMap){
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng myLocation = getMyLocation();
        //mMap.addMarker(new MarkerOptions().position(myLocation).title("Your current location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                markers.add(point);
                final Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(point.latitude, point.longitude)).title("Title"));
                lat = point.latitude;
                lon = point.longitude;
                m.setSnippet("Please describe the problem.");
                getInput(m);
            }
        });

        MakeHttpGetRequest();
        JSONArray arr = null;
        try {
            ithread.join();
            System.out.println(httpResponse);
            arr = new JSONArray(httpResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String id = obj.getString("_id");
                String desc = obj.getString("description");
                String title = obj.getString("title");
                double lat = obj.getDouble("latitude");
                double longitude = obj.getDouble("longitude");
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, longitude)).title(title).snippet(desc));
            }
        } catch (Exception e) {
            System.out.println("Fuck off, java");
        }
    }
}
