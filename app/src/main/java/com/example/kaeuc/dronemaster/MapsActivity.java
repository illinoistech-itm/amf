package com.example.kaeuc.dronemaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationProvider;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,ActivityCompat.OnRequestPermissionsResultCallback,
        com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationDialog.ConfirmDialogListener{

    /*TAG used for logs*/
    private static final String TAG = "MapsActivity";

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;

    private static boolean locationPermissionGranted = false;

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

        //Find UI Widgets
        btnRequest = (Button) findViewById(R.id.btn_request);
        txtAddress = (TextView) findViewById(R.id.txt_address);


        //Initiate the receiver
        mResultReceiver = new AddressResultReceiver(new Handler());

        if(checkNetworkConnection()){ // Internet connection successful

            // Sets the action when the button is clicked
            btnRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmLocation();
                }
            });
        }else{
            // Handle no connection cases
            Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show();
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
    private void setMapInCurrentLocation(Location location) {
        if(locationPermissionGranted){
            LatLng currentPosition = new LatLng(location.getLatitude(),location.getLongitude());
            //moves map camera to current position
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    currentPosition,15);
            mMap.moveCamera(cameraUpdate);
        }else{
            LatLng defaultPosition = new LatLng(41.881832,-87.623177);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    defaultPosition,15);
            mMap.moveCamera(cameraUpdate);
        }

    }


    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, LOCATION_PERMISSION_REQUEST_CODE);
            setMapInCurrentLocation(mCurrentLocation);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
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
    /*Starts the location updates, moves the current position marker,
    no need to check permission here as soon as they were checked at the initialization
    */

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if(locationPermissionGranted)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
//        Toast.makeText(this, "Start Location Updates", Toast.LENGTH_LONG).show();
    }

    /*Stops the location update, use when there is no need of the gps*/
    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
//        Toast.makeText(this, "Location Updates Stopped", Toast.LENGTH_SHORT).show();

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

    /*Shows the confirmation dialog to confirm the address */
    private void confirmLocation(){
        centerLocation = mMap.getCameraPosition().target;
        LocationDialog dialog = new LocationDialog();
        dialog.show(getFragmentManager(),"LocationDialogFragment");
    }

    /*Starts the geofence intent to retrieve the address from the coordinates given*/
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
            Log.i(TAG, getString(R.string.no_wifi_or_mobile));
            return false;
        }

        return true;
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
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        checkLocationPermissions();
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                centerLocation = mMap.getCameraPosition().target;
                startIntentService(centerLocation.latitude,centerLocation.longitude);
                mMap.clear();
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Enable the my location layer if the permission has been granted.
            checkLocationPermissions();
            locationPermissionGranted = true;
        }else{

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

    public void sendLocation(double lat, double lon){
        JSONObject locationObj = new JSONObject();
        try{
            locationObj.put(getString(R.string.location_latitude),lat);
            locationObj.put(getString(R.string.location_longitude),lon);
        }catch (JSONException e){
            e.printStackTrace();
        }

        if (locationObj.length() > 0){
            new SendDataToServer().execute(String.valueOf(locationObj));
        }
    }


    class SendDataToServer extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... params) {
            String jsonResponse = null;
            String jsonData = params[0];
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try{

                URL url = new URL(getString(R.string.server_url));
                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout( 10000 /*milliseconds*/ );
                connection.setConnectTimeout( 15000 /* milliseconds */ );
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();


                Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                writer.write(jsonData);

                writer.close();

                InputStream inputStream = connection.getInputStream();

                StringBuffer buffer = new StringBuffer();

                if(inputStream == null){
                    return null;
                }

                reader =  new BufferedReader(new InputStreamReader(inputStream));


                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                jsonResponse = buffer.toString();
                Log.i(TAG,jsonResponse);
                return jsonResponse;

            }catch (IOException e){
                e.printStackTrace();
            }finally {
                if(connection != null){
                    connection.disconnect();
                }

                if(reader != null){
                    try{
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //noinspection MissingPermission
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        setMapInCurrentLocation(mCurrentLocation);
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        stopLocationUpdates();
        sendLocation(centerLocation.latitude,centerLocation.longitude);

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }
}

