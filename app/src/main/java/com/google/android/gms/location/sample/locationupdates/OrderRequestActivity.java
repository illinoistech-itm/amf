package com.google.android.gms.location.sample.locationupdates;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class OrderRequestActivity extends AppCompatActivity {
    // Strings to represent the intent when it's called
    public static final String ORDER_ACTION = "myApp.ACTION_REQUEST";
    public static final String ORDER_CATEGORY= "myApp.CATEGORY_REQUEST";

    //UI Widgets
    private TextView txtViewLatitude;
    private TextView txtViewLongitude;
    private TextView txtViewError;

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
            final Bundle location = previousIntent.getBundleExtra("location");
            txtViewLatitude.append(location.getString("latitude"));
            txtViewLongitude.append(location.getString("longitude"));
            txtViewError.setVisibility(View.INVISIBLE);

        }else{
            txtViewError.setVisibility(View.VISIBLE);
            txtViewError.setText(R.string.location_error);
            txtViewLongitude.setVisibility(View.INVISIBLE);
            txtViewLatitude.setVisibility(View.INVISIBLE);
        }

    }

}
