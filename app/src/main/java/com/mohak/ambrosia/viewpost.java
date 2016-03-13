package com.mohak.ambrosia;

import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class viewpost extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private double latitude = -1, longitude = -1;
    private static final String urL = "http://graph.mod.bz/publications/ambrosia";
    private ArrayList<singleItem> singleItems;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpost);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }


    private void fetch() {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray ambro = response.getJSONArray("ambrosia");
                    for (int i = 0; i < ambro.length(); i++) {
                        JSONObject object = ambro.getJSONObject(i);
                        String share = object.getString("str1");
                        Double lat = object.getDouble("lat1");
                        Double lonng = object.getDouble("long3");
                        double d = compare(latitude, longitude, lat, lonng);
                        if (d <= 5000) {
                            mark(share, lat, lonng);
                            Log.d("dis", "" + d);
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void mark(String share, double lat, double lon) {

        if (lat != -1 && lon != -1 && googleMap != null) {
            MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, lon)).title(share).draggable(false);
            googleMap.addMarker(marker);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(lat, lon)).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .radius(2500)
                    .strokeColor(Color.RED)
                    .fillColor(Color.LTGRAY));

        } else {
            Toast.makeText(this, "error" + longitude + latitude, Toast.LENGTH_LONG).show();
        }

    }


    private double compare(double latitude, double longitude, Double lat, Double lonng) {

        Location selected_location = new Location("locationA");
        selected_location.setLatitude(latitude);
        selected_location.setLongitude(longitude);
        Location near_locations = new Location("locationA");
        near_locations.setLatitude(lat);
        near_locations.setLongitude(lonng);
        double distance = selected_location.distanceTo(near_locations);
        return distance;
    }

    private void initialisemap() {
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                R.id.map)).getMap();
        if (googleMap == null) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_viewpost, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialisemap();

    }

    @Override
    public void onConnected(Bundle bundle) {

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            fetch();
//            mark(latitude, longitude);
//            address(latitude, longitude);

        } else {

            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
