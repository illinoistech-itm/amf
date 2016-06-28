package com.example.kaeuc.dronemaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,ActivityCompat.OnRequestPermissionsResultCallback,
        com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationDialog.ConfirmDialogListener{


    private static final String TAG = "MapsActivity";

    private AddressResultReceiver mResultReceiver;

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

    private LatLng centerLocation;

    /**
     * STATIC VARIABLES
     */

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final String [] PERMISSIONS_REQUIRED = {Manifest.permission.ACCESS_FINE_LOCATION};
    private String mAddressOutput;

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

        mResultReceiver = new AddressResultReceiver(new Handler());


        //Find UI Widgets
        btnRequest = (Button) findViewById(R.id.btn_request);
        txtAddress = (TextView) findViewById(R.id.txt_address);

        buildGoogleApiClient();



        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationUpdates();
                confirmLocation();

            }
        });


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


    @SuppressWarnings("MissingPermission")
    private void setMapInCurrentLocation(Location location) {
        LatLng currentPosition = new LatLng(location.getLatitude(),location.getLongitude());
        if (locationIsNull()){
            //Move camera
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    currentPosition,15);
            mMap.moveCamera(cameraUpdate);

        }else{
            //move camera to current position

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    currentPosition,15);

            mMap.moveCamera(cameraUpdate);
        }
    }

    private boolean locationIsNull(){
        return mCurrentLocation == null;
    }


    private void confirmLocation(){
        centerLocation = mMap.getCameraPosition().target;

        LocationDialog dialog = new LocationDialog();
        Bundle address = new Bundle();
        address.putString("address",mAddressOutput);
        dialog.setArguments(address);
        dialog.show(getFragmentManager(),"LocationDialogFragment");
    }



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
    protected void startLocationUpdates() {

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
//        Toast.makeText(this, "Start Location Updates", Toast.LENGTH_LONG).show();
    }


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

    protected void startIntentService(double lat, double lon) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        Bundle location = new Bundle();
        location.putDouble("latitude",lat);
        location.putDouble("longitude",lon);
        intent.putExtra("location",location);
        startService(intent);
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
        enableMyLocation();
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
            enableMyLocation();
        }else{
            //Disable what uses the permission
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


    /*TODO*/

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


    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }
}

