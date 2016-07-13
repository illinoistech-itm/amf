package com.example.kaeuc.dronemaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        com.google.android.gms.location.LocationListener,
        /*These interfaces are used to monitor the state of Google API Client*/
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        /*Interface used to create a dialog window and get the results*/
        LocationDialog.ConfirmDialogListener,SendDataToServer.AsyncResponse {

    /*TAG used for logs*/
    private static final String TAG = "MapsActivity";

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;

    private static boolean locationPermissionGranted = false;

    private static boolean gpsOn = false;

    /*Inner class responsible to receive the results of the geofence intent*/
    private AddressResultReceiver mResultReceiver;

    /*Stores the address returned by the geofence*/
    private String mAddressOutput;

    /*
    * UI Widgets
    */
    private GoogleMap mMap;
    private Button btnRequest;
    private TextView txtAddress;


    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /*
    * Stores the current device location
    */
    private Location mCurrentLocation;

    /* Coordinates of the screen center*/
    private LatLng centerLocation;

    /**
     * STATIC VARIABLES
     */

    /*Variables needed to request permission to get location*/
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final String [] PERMISSIONS_REQUIRED = {Manifest.permission.ACCESS_FINE_LOCATION};
    private boolean moveCameraToUser;
    private boolean resumed = false;



    /*
    *   ACTIVITY METHODS START
    */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //  Find UI Widgets
        btnRequest = (Button) findViewById(R.id.btn_request);
        txtAddress = (TextView) findViewById(R.id.txt_address);


        //  Initiate the receiver (which is gonna receive the address)
        mResultReceiver = new AddressResultReceiver(new Handler());

        //  Used in case the app was running before or change the orientation
        if(savedInstanceState != null){
            centerLocation = savedInstanceState.getParcelable("centerLocation");
            mCurrentLocation = savedInstanceState.getParcelable("currentLocation");
            moveCameraToUser = true;
        }
        //  TODO
        //  Separate this
        // Internet and GPS connection on
        if(checkNetworkConnection() && checkGpsConnection()){

            // Sets the action when the button is clicked
            btnRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmLocation();
                }
            });
        }else{
            // TODO
            // Handle no connection cases
            if(!checkNetworkConnection())
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "No GPS connection", Toast.LENGTH_SHORT).show();
        }
        buildGoogleApiClient();

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        resumed = true;
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
    }

    /*
    *   ACTIVITY METHODS END
    */


    /**
     *  CUSTOM METHODS START
     */

    /*Sets the map on the current location*/
    private void initMapCamera(Location location) {
        if(!moveCameraToUser) {
            if (locationPermissionGranted) {
                CameraPosition cameraPosition = CameraPosition.builder().target(
                        new LatLng(location.getLatitude(), location.getLongitude()))
                        .zoom(16f)
                        .bearing(0.0f)
                        .tilt(0.0f)
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                enableMyLocation();
            } else {
                CameraPosition cameraPosition = CameraPosition.builder().target(
                        new LatLng(41.881832, -87.623177))
                        .zoom(16f)
                        .bearing(0.0f)
                        .tilt(0.0f)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    private void initMapCamera(LatLng location) {
        if(!moveCameraToUser) {
            if (locationPermissionGranted) {
                CameraPosition cameraPosition = CameraPosition.builder().target(
                        new LatLng(location.latitude, location.longitude))
                        .zoom(16f)
                        .bearing(0.0f)
                        .tilt(0.0f)
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                enableMyLocation();
            }
        }
    }




    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            // Access to the location has been granted before and the map is initialized.
            locationPermissionGranted = true;
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i("location-updates-sample", "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    @SuppressWarnings("MissingPermission")
    /*
    *   Starts the location updates, moves the current position marker,
    *   no need to check permission here as soon as they were checked at the initialization
    */

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if(locationPermissionGranted)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    /*
     *  Stops the location update, use when there is no need of the gps
     */
    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(10000);
        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(10000/2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Shows a confirmation dialog to confirm the address
     */
    private void confirmLocation(){
        LocationDialog dialog = new LocationDialog();
        dialog.show(getFragmentManager(),"LocationDialogFragment");
    }

    /**
     * Starts the geofence intent to retrieve the address from the coordinates given
     */
    protected void startIntentService(double lat, double lon) {
        // Creates the new intent to run in the background
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        // Creates the bundle which will keep the location coordinates
        Bundle location = new Bundle();
        location.putDouble(getString(R.string.location_latitude),lat);
        location.putDouble(getString(R.string.location_longitude),lon);
        intent.putExtra(getString(R.string.location_bundle),location);
        startService(intent);
    }

    /**
     * Check whether the device is connected, and if so, whether the connection
     * is wifi or mobile (it could be something else).
     */
    private boolean checkNetworkConnection() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if(wifiConnected) {
                Log.i(TAG, getString(R.string.wifi_connection));
            } else if (mobileConnected){
                Log.i(TAG, getString(R.string.mobile_connection));
            }
        } else {
            Log.e(TAG, getString(R.string.no_wifi_or_mobile));
            return false;
        }
        return true;
    }


    private boolean checkGpsConnection(){
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * CUSTOM METHODS END
     */


    /**
     * INTERFACES METHODS START
     */

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        //  Runs when the app is partially visible or the orientation changes
        if(moveCameraToUser){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(centerLocation));
        }
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mMap.clear();
                centerLocation = mMap.getCameraPosition().target;
                startIntentService(centerLocation.latitude,centerLocation.longitude);
            }
        });
        enableMyLocation();
    }


    /**
     *  Sets the My Location button on the map
     */

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     *  Callback called after the permission request dialog
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Enable the my location layer if the permission has been granted.
            locationPermissionGranted = true;
            onConnected(new Bundle());
        }else{
            Toast.makeText(this, "You need to grant location permissions", Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

    @Override
    public void onTaskCompleted(String output) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Confirmation");
        alertDialog.setMessage(output);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    /**
     * INTERFACES METHODS END
     */


    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            txtAddress.setText(mAddressOutput);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                txtAddress.setText(mAddressOutput);
            }
        }
    }

    /*TODO*/

    public void sendLocation(double lat, double lon,String address){
        JSONObject locationObj = new JSONObject();
        try{
            locationObj.put(getString(R.string.location_latitude),lat);
            locationObj.put(getString(R.string.location_longitude),lon);
            locationObj.put("address",address);
        }catch (JSONException e){
            e.printStackTrace();
        }

        if (locationObj.length() > 0){
            new SendDataToServer(this).execute(String.valueOf(locationObj));
        }
    }





    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putParcelable("centerLocation",centerLocation);
        outState.putParcelable("currentLocation",mCurrentLocation);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //noinspection MissingPermission
        startLocationUpdates();
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        checkLocationPermissions();
        if(resumed){
            initMapCamera(centerLocation);
        }else{
            initMapCamera(mCurrentLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
//        stopLocationUpdates();
        sendLocation(centerLocation.latitude,centerLocation.longitude,mAddressOutput);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }
}
