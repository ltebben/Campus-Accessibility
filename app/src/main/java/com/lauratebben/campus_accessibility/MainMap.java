package com.lauratebben.campus_accessibility;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainMap extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private ArrayList<LatLng> markers = new ArrayList<LatLng>();
    private double lat, lon;
    String description, title;
    String httpResponse = null;
    Marker m = null;
    boolean toEnd = false;
    Button button = null;
    Thread ithread = null;

    String startLocation = null;
    String endLocation = null;
    AlertDialog.Builder departure = null;
    AlertDialog.Builder destination = null;
    EditText departText = null;
    EditText destText = null;
    String directionsResult = null;

    private void getDirections() {
        // Make http request from google here
        String request = "https://maps.googleapis.com/maps/api/directions/json?";
        String start = startLocation;
        start.replaceAll(Pattern.quote("\\s"), "+");
        String end = endLocation;
        end.replaceAll(Pattern.quote("\\s"), "+");
        request += "origin=";
        request += start;
        request += "&destination=";
        request += end;
        request += "&mode=walking&key=";
        // This is a different key. If it doesn't work try using the old one.
        request += "AIzaSyARJ_7oWPxlS8c2dJ84tm3dk7dutpUFtzQ";
        // At this point, the request is good to go
        // Somehow you make this request and it gives you a json document to sort through
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        button = (Button) findViewById(R.id.setRangeButton);
        departure = new AlertDialog.Builder(this);
        destination = new AlertDialog.Builder(this);
        departText = new EditText(this);
        destText = new EditText(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                departure.setTitle("Departure");
                departure.setView(departText);
                destination.setTitle("Destination");
                destination.setView(destText);

                departure.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   @Override
                    public void onClick(DialogInterface dialog, int which) {
                       startLocation = departText.getText().toString();
                   }
                });

                departure.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                destination.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        endLocation = destText.getText().toString();
                        if (startLocation != null && !startLocation.isEmpty() && endLocation != null && !endLocation.isEmpty()) {
                            getDirections();
                        }
                    }
                });

                destination.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                destination.show();
                departure.show();
            }
        });
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

    @Override
    public boolean onMarkerClick(Marker marker) {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle(marker.getTitle());
        builder1.setMessage(marker.getSnippet());
        builder1.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // continue with delete
            }
        });
        builder1.setIcon(android.R.drawable.ic_dialog_alert);
        builder1.show();


        return true;
    }

    private void getInput() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("How do you get around this?");
        final EditText desc = new EditText(this);
        desc.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(desc);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (toEnd) { dialog.cancel(); toEnd = false; return; }
                description = desc.getText().toString();
                m.setSnippet(description);
                if (description.isEmpty() || title.isEmpty() || description == null || title == null) {
                    markers.remove(markers.size() - 1);
                    m.remove();
                }
                else {
                    makeHTTPRequest();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (toEnd) { toEnd = false; return; }
                dialog.cancel();
                markers.remove(markers.size() - 1);
                m.remove();
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
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                toEnd = true;
                markers.remove(markers.size() - 1);
                m.remove();
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
        LatLng myLocation = getMyLocation();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                markers.add(point);
                m = mMap.addMarker(new MarkerOptions().position(new LatLng(point.latitude, point.longitude)).title(""));
                lat = point.latitude;
                lon = point.longitude;
                getInput();
            }
        });

        MakeHttpGetRequest();
        JSONArray arr = null;
        try {
            ithread.join();
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
            System.out.println("Error");
        }
    }
}
