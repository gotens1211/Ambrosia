package com.mohak.ambrosia;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class share extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Button post;
    String share;
    EditText editText, editText2;
    GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private double latitude = -1, longitude = -1;
    Geocoder geocoder;
    GoogleMap googleMap;
    private ProgressDialog dialog;
    Button camera, gallery;
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private Uri imageUri;
    private static final String IMAGE_DIRECTORY_NAME = "Ambrosia";
    private static final String postUrl = "http://graph.mod.bz/api/latlong";
    private Marker mark;
    private static int RESULT_LOAD_IMG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        editText = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.location);
        post = (Button) findViewById(R.id.post);
        camera = (Button) findViewById(R.id.camera);
        gallery = (Button) findViewById(R.id.gallery);
        imageView = (ImageView) findViewById(R.id.imageview);
        //prevent keyboard from popping
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
// Start the Intent
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);

            }
        });
        dialog = new ProgressDialog(this);
        dialog.setMessage("Fetching Location");
        dialog.show();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        initialisemap();
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share = editText.getText().toString();
                //  parse(share, latitude, longitude);
                posting(share, latitude, longitude);

            }
        });

    }

    private void posting(final String share, double latitude, double longitude) {

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("str1", share);
            jsonObject.put("lat1", latitude);
            jsonObject.put("long3", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, postUrl, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }


        });

        requestQueue.add(jsonObjReq);
        Toast.makeText(com.mohak.ambrosia.share.this, "Posted", Toast.LENGTH_LONG).show();
        editText.setText("");
        editText.setHint("Share again (max 20)");
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setVisibility(View.VISIBLE);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            // Picasso.with(this).load(save(bitmap)).into(imageView);
            Bitmap bitmap1 = BitmapFactory.decodeFile(save(bitmap), options);
            imageView.setImageBitmap(bitmap1);

        } else if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(bitmap);
                save(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Log.d(TAG, String.valueOf(bitmap));
        }
    }

//    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
//        int width = bm.getWidth();
//        int height = bm.getHeight();
//        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
//        // create a matrix for the manipulation
//        Matrix matrix = new Matrix();
//        // resize the bit map
//        matrix.postScale(scaleWidth, scaleHeight);
//        // recreate the new Bitmap
//        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
//        return resizedBitmap;
//    }

    private String save(Bitmap bitmapImage) {

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Ambrosia");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mypath = new File(file.getPath(), "IMG" + timeStamp + ".jpg");
        if (!mypath.exists()) {
            Boolean b = file.mkdir();
            Toast.makeText(com.mohak.ambrosia.share.this, "" + b, Toast.LENGTH_SHORT).show();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(com.mohak.ambrosia.share.this, "" + mypath.toString(), Toast.LENGTH_SHORT).show();
        return mypath.toString();
    }


    private void initialisemap() {
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                R.id.map)).getMap();
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                latitude=marker.getPosition().latitude;
                longitude=marker.getPosition().longitude;
                address(latitude, longitude);
            }
        });
        if (googleMap == null) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    private void mark(double lat, double lon) {

        if (lat != -1 && lon != -1 && googleMap != null) {
            MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, lon)).title("#Foodie :D").draggable(true);
            mark = googleMap.addMarker(marker);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(lat, lon)).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            dialog.dismiss();

        } else {
            Toast.makeText(com.mohak.ambrosia.share.this, "error" + longitude + latitude, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        initialisemap();
        if (mark != null)
            mark.remove();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share, menu);
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

    @Override
    public void onConnected(Bundle bundle) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            mark(latitude, longitude);
            address(latitude, longitude);

        } else {
            dialog.dismiss();
            Toast.makeText(com.mohak.ambrosia.share.this, "Error! Could not fetch your location Plz Enter Location", Toast.LENGTH_LONG).show();

        }
    }

    private void address(double latitude, double longitude) {
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> address = geocoder.getFromLocation(latitude, longitude, 1);
            editText2.setText(address.get(0).getAddressLine(0) + ", " + address.get(0).getAddressLine(1) + ", " + address.get(0).getAddressLine(2));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
