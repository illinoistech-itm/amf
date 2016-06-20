package com.google.android.gms.location.sample.locationupdates;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class OrderRequestActivity extends AppCompatActivity {
    // Strings to represent the intent when it's called
    public static final String ORDER_ACTION = "myApp.ACTION_REQUEST";
    public static final String ORDER_CATEGORY= "myApp.CATEGORY_REQUEST";

    //UI Widgets
    private TextView txtViewLatitude;
    private TextView txtViewLongitude;
    private TextView txtViewError;

    private Bundle location;
    private GoogleMap mapShowLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Find UI widgets
        txtViewLatitude = (TextView) findViewById(R.id.txt_latitude);
        txtViewLongitude = (TextView) findViewById(R.id.txt_longitude);
        txtViewError = (TextView) findViewById(R.id.txt_error);


        /*
        * Retrieves the previous intent (previous activity info)
        * and checks if it has the bundle which stores the location
        */
        Intent previousIntent = getIntent();
        if (previousIntent.hasExtra("location")){
            location = previousIntent.getBundleExtra("location");
            txtViewLatitude.append(location.getString("latitude"));
            txtViewLongitude.append(location.getString("longitude"));
            txtViewError.setVisibility(View.INVISIBLE);
            setUpMapIfNeeded();

        }else{
            txtViewError.setVisibility(View.VISIBLE);
            txtViewError.setText(R.string.location_error);
            txtViewLongitude.setVisibility(View.INVISIBLE);
            txtViewLatitude.setVisibility(View.INVISIBLE);
        }

    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mapShowLocation == null) {
            // Try to obtain the map from the SupportMapFragment.
            mapShowLocation = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_showLocation))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mapShowLocation != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

        Double lat = Double.parseDouble(location.getString("latitude"));
        Double lon = Double.parseDouble(location.getString("longitude"));
        LatLng latLng = new LatLng(lat,lon);
        mapShowLocation.setMyLocationEnabled(true);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,15);
        mapShowLocation.animateCamera(cameraUpdate);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
