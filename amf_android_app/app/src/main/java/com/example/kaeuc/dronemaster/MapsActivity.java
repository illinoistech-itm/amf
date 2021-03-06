package com.example.kaeuc.dronemaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        com.google.android.gms.location.LocationListener,
        /*These interfaces are used to monitor the state of Google API Client*/
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        /*Interface used to create a dialog window and get the results*/
        LocationDialog.ConfirmDialogListener, ServerTaskResponse,//LocationDialog의 ConfirmDialoglistener와 OnServerTaskCompleted함수
        DroneLocationResponse {

    //  TAG used for logs

    private static final String TAG = "MapsActivity";


    /*  Checking variables  */

    private boolean wifiConnected = false;
    private boolean mobileConnected = false;
    private boolean locationPermissionGranted = false;
    private boolean moveCameraToUser = false;
    private boolean activityResumed = false;
    private boolean orderDelivered = false;

    long now;
    Date date;
    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /*  Drone Variables */

    //    private String droneRequestedID;
    private MarkerOptions droneMarker;
    private Handler droneHandler = new Handler();
    private static final long DRONE_POSITION_INTERVAL = 1000;


    /* Location variables */

    //  Stores parameters for requests to the FusedLocationProviderApi.
    protected LocationRequest mLocationRequest;
    //  Stores the address returned by the geofence
    private String mAddressOutput;
    // Stores the current device location
    private Location mCurrentLocation;
    //  Coordinates of the screen center
    private LatLng centerLocation;


     /* STATIC VARIABLES */

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    //  Permissions needed
    private static final String[] PERMISSIONS_REQUIRED = {Manifest.permission.ACCESS_FINE_LOCATION};


    /*  UI Widgets  */
    private GoogleMap mMap;
    private Button btnRequest;
    private TextView txtAddress;
    /*private CheckBox checkOne;
    private CheckBox checkTwo;
    private Button btnEtc;*///previous ui
    private CheckBox battery;
    private CheckBox blood_sample;
    private CheckBox first_aid_kit;
    private CheckBox flashlight;
    private CheckBox food;
    private CheckBox map;
    private CheckBox radio;
    private CheckBox tissue;
    private CheckBox tool;
    private CheckBox water;
    private Toolbar toolbar;


    // Instance to receive the geofence address
    private AddressResultReceiver mResultReceiver;

    // Provides the entry point to Google Play services.
    protected GoogleApiClient mGoogleApiClient;

    // Unique instance ID. Maybe can change for a user ID in the future */
    private String instanceAppID = UUID.randomUUID().toString();
    private String ipAddress = "";


    // Inner class responsible to receive the results of the geofence intent
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

    //  Thread to get the drone location every time in a interval
    Runnable getDroneLocation = new Runnable() {
        @Override
        public void run() {
            if (!orderDelivered) {
                new DroneLocation(MapsActivity.this).execute(instanceAppID, ipAddress);
                droneHandler.postDelayed(this, DRONE_POSITION_INTERVAL);
            }
        }
    };


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
        /*checkOne = (CheckBox) findViewById(R.id.cb_one);
        checkTwo = (CheckBox) findViewById(R.id.cb_two);
        btnEtc = (Button) findViewById(R.id.btn_ect);*///previous ui
        battery = (CheckBox) findViewById(R.id.battery);
        blood_sample = (CheckBox) findViewById(R.id.blood);
        first_aid_kit = (CheckBox) findViewById(R.id.first_aid_kit);
        flashlight = (CheckBox) findViewById(R.id.flashlight);
        food = (CheckBox) findViewById(R.id.food);
        map = (CheckBox) findViewById(R.id.i_map);
        radio = (CheckBox) findViewById(R.id.radio);
        tissue = (CheckBox) findViewById(R.id.tissue);
        tool = (CheckBox) findViewById(R.id.tool);
        water = (CheckBox) findViewById(R.id.water);
        btnRequest = (Button) findViewById(R.id.btn_request);
        txtAddress = (TextView) findViewById(R.id.txt_address);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setLogo(R.drawable.app_icon);
//        getSupportActionBar().setDisplayUseLogoEnabled(true);


        //  Initiate the receiver (which is gonna receive the address)
        mResultReceiver = new AddressResultReceiver(new Handler());

        //  Used in case the app was running before or change the orientation
        if (savedInstanceState != null) {
            centerLocation = savedInstanceState.getParcelable("centerLocation");
            mCurrentLocation = savedInstanceState.getParcelable("currentLocation");
            moveCameraToUser = true;
        }

        activityResumed = false;

        //  Checks if the gps and network are on and working
        if (checkNetworkConnection()) {//&&checkGpsConnection()추가할것
            // Sets the action when the button is clicked
            btnRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmLocation();
                }
            });
            buildGoogleApiClient();
        } else if (!checkNetworkConnection()) {
            // TODO
            // Change the layout for no internet connection
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.no_connection);
        } else if (!checkGpsConnection()) {
            //  TODO
            // Turn on gps dialog
            Toast.makeText(this, "No GPS connection", Toast.LENGTH_SHORT).show();
        }

        /*btnEtc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MapsActivity.this, CheckActivity.class);
                startActivity(myIntent);
            }
        });*///previous ui
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        activityResumed = true;
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        droneHandler.removeCallbacks(getDroneLocation);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actionbar_menus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.action_settings): {
                showInputDialog();
                return true;
            }
            case (R.id.search_address): {
                showSearchDialog();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /*
    *   ACTIVITY METHODS END
    */


    /**
     * CUSTOM METHODS START
     */

    /*Sets the map on the current location*/
    private void initMapCamera(Location location) {
        if (!moveCameraToUser) {
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
        if (!moveCameraToUser) {
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
        if (locationPermissionGranted)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
        LocationRequest request = LocationRequest.create();
        // mLocationRequest = new LocationRequest();
        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        request.setInterval(1000);
        // mLocationRequest.setInterval(1000);
        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        request.setFastestInterval(1000 / 2);
        //mLocationRequest.setFastestInterval(1000/2);
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Shows a confirmation dialog to confirm the address
     */
    private void confirmLocation() {
        LocationDialog dialog = new LocationDialog();
        dialog.show(getFragmentManager(), "LocationDialogFragment");
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
        location.putDouble(getString(R.string.location_latitude), lat);
        location.putDouble(getString(R.string.location_longitude), lon);
        intent.putExtra(getString(R.string.location_bundle), location);
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
            if (wifiConnected) {
                Log.i(TAG, getString(R.string.wifi_connection));
            } else if (mobileConnected) {
                Log.i(TAG, getString(R.string.mobile_connection));
            }
        } else {
            Log.e(TAG, getString(R.string.no_wifi_or_mobile));
            return false;
        }
        return true;
    }

    private boolean checkGpsConnection() {
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private String createRequestID() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy_HH:mm:ss:ms");
        String formattedDate = df.format(c.getTime());
        return getString(R.string.request_prefix) + formattedDate;
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
        if (moveCameraToUser) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(centerLocation));
        }
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                centerLocation = mMap.getCameraPosition().target;
                startIntentService(centerLocation.latitude, centerLocation.longitude);
            }
        });
        enableMyLocation();
    }


    /**
     * Sets the My Location button on the map
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
     * Callback called after the permission request dialog
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
        } else {
            Toast.makeText(this, "You need to grant GPS permissions", Toast.LENGTH_LONG).show();
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


    public void onServerTaskCompleted(JSONObject output) {
        int codeResult = 0;
        try {
            codeResult = output.getInt("result");
            if (codeResult == 1) {
                // maybe remove this later
//            droneRequestedID = output.getString("droneID");
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Confirmation.");
                alertDialog.setMessage(output.getString("response"));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                droneHandler.postDelayed(getDroneLocation, DRONE_POSITION_INTERVAL);
                                btnRequest.setEnabled(false);
                            }
                        });
                alertDialog.show();
            } else if (codeResult == -1) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Ooops!");
                alertDialog.setMessage(output.getString("response"));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            } else {
                // TODO
                // error message
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDroneLocationResponse(JSONObject output) {
        LatLng position = null;
        int resultCode = 0;
        try {
            resultCode = output.getInt("RESPONSE");
            if (resultCode == 200) {
                mMap.clear();
                position = new LatLng(output.getDouble("LATITUDE"), output.getDouble("LONGITUDE"));
                droneMarker = new MarkerOptions().position(position)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.drone_icon))
                        .anchor((float) 0.5, (float) 0.5)
                        .rotation((float) 90.0)
                        .flat(true);
                mMap.addMarker(droneMarker);
            } else if (resultCode == -2) {
                mMap.clear();
                Toast.makeText(this, "The package was delivered!", Toast.LENGTH_LONG).show();
                btnRequest.setEnabled(true);
                orderDelivered = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * INTERFACES METHODS END
     */

    public void sendRequestInfo(double lat, double lon, String address, String instanceId, String reqId) {
        JSONObject locationObj = new JSONObject();
        try {
            locationObj.put(getString(R.string.instance_id), instanceId);
            locationObj.put(getString(R.string.request_id), reqId);
            locationObj.put(getString(R.string.location_latitude), lat);
            locationObj.put(getString(R.string.location_longitude), lon);
            locationObj.put(getString(R.string.address), address);

            if (battery.isChecked())
                locationObj.put("drone_type_one", "1");
            if (blood_sample.isChecked())
                locationObj.put("drone_type_two", "2");
            if (first_aid_kit.isChecked())
                locationObj.put("drone_type_three", "3");
            if (flashlight.isChecked())
                locationObj.put("drone_type_four", "4");
            if (map.isChecked())
                locationObj.put("drone_type_five", "5");
            if (radio.isChecked())
                locationObj.put("drone_type_six", "6");
            if (tissue.isChecked())
                locationObj.put("drone_type_seven", "7");
            if (tool.isChecked())
                locationObj.put("drone_type_eight", "8");
            if (water.isChecked())
                locationObj.put("drone_type_nine", "9");
            if (food.isChecked())
                locationObj.put("drone_type_ten", "10");


        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (locationObj.length() > 0) {
            new ServerAccess(this).execute(String.valueOf(locationObj), ipAddress);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putParcelable("centerLocation", centerLocation);
        outState.putParcelable("currentLocation", mCurrentLocation);
        super.onSaveInstanceState(outState);

    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //noinspection MissingPermission
        startLocationUpdates();
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        checkLocationPermissions();
        if (activityResumed) {
            initMapCamera(centerLocation);
        } else {
            initMapCamera(mCurrentLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Google API connection was suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "It was not possible to connect to Google API.");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        sendRequestInfo(centerLocation.latitude, centerLocation.longitude,
                mAddressOutput,
                instanceAppID,
                createRequestID());
//                sendRequestItemInfo();
                Toast.makeText(getApplicationContext(), "Success to send the information about checkbox", Toast.LENGTH_SHORT).show();
    }

    /* These two checkboxes are for calling different type of drones. (ex Checkbox 1 calls type 1 drones)
    * This method is for sending information that which checkbox is checked or not.
    * There is no string variable which is receiving checkbox information in , so placed "drone type two/one" instead.
    * Needs to be fixed.
    * */

    /*public void sendRequestItemInfo() {
        *//*ArrayList<HashMap<String , String>> arrayList = new ArrayList<>();
        HashMap<String, String> hashMap = new HashMap<>();

//            droneObj.put("drone type one", checkOne.isChecked());
//            droneObj.put("drone type two", checkTwo.isChecked());
            if (battery.isChecked())
                hashMap.put("drone_type","1");
            if (blood_sample.isChecked())
                hashMap.put("drone_type","2");
            if (first_aid_kit.isChecked())
                hashMap.put("drone_type","3");
            if (flashlight.isChecked())
                hashMap.put("drone_type","4");
            if (map.isChecked())
                hashMap.put("drone_type","5");
            if (radio.isChecked())
                hashMap.put("drone_type","6");
            if (tissue.isChecked())
                hashMap.put("drone_type","7");
            if (tool.isChecked())
                hashMap.put("drone_type","8");
            if (water.isChecked())
                hashMap.put("drone_type","9");
            if (food.isChecked())
                hashMap.put("drone_type","10");
        arrayList.add(hashMap);
        return String.valueOf(arrayList);*//*
        JSONObject droneObj = new JSONObject();
        try {
            *//*droneObj.put("drone type one", checkOne.isChecked());
            droneObj.put("drone type two", checkTwo.isChecked());*//*//previous ui
            if(battery.isChecked())
                droneObj.put("drone type one","1");
            if(blood_sample.isChecked())
                droneObj.put("drone type two","2");
            if(first_aid_kit.isChecked())
                droneObj.put("drone type three","3");
            if(flashlight.isChecked())
                droneObj.put("drone type four","4");
            if(map.isChecked())
                droneObj.put("drone type five","5");
            if(radio.isChecked())
                droneObj.put("drone type six","6");
            if(tissue.isChecked())
                droneObj.put("drone type seven","7");
            if(tool.isChecked())
                droneObj.put("drone type eight","8");
            if(water.isChecked())
                droneObj.put("drone type nine","9");
            if(food.isChecked())
                droneObj.put("drone type ten","10");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (droneObj.length() > 0) {
            new ServerAccess(this).execute(String.valueOf(droneObj), ipAddress);
        }
    }*/



    protected void showInputDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

//        Resources res = getResources();
        final EditText editText = (EditText) promptView.findViewById(R.id.edt_ipAddress);
//        String server_get_url = String.format(res.getString(R.string.server_get_url),editText.getText());
//        String server_post_url = String.format(res.getString(R.string.server_post_url),editText.getText());


        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ipAddress = ""+ editText.getText();
                        Toast.makeText(MapsActivity.this, "IP Address: "+ipAddress + " saved.", Toast.LENGTH_LONG).show();

                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    protected void showSearchDialog() {
        // get search_dialog.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.search_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        final EditText edtAddress = (EditText) promptView.findViewById(R.id.edt_address);
        final EditText edtLongitude = (EditText) promptView.findViewById(R.id.edt_longitude);
        final EditText edtLatitude = (EditText) promptView.findViewById(R.id.edt_latitude);

        Button btnAddress = (Button) promptView.findViewById(R.id.btn_address);
        Button btnCoordinate = (Button) promptView.findViewById(R.id.btn_coordinate);

        final Geocoder geocoder = new Geocoder(this);

        btnAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = edtAddress.getText().toString();
                List<Address> addressList = null;

                if(location != null || location.equals("")) {
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    //mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        });

        btnCoordinate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double latitude_d = Double.parseDouble(edtLatitude.getText().toString());
                Double longitude_d = Double.parseDouble(edtLongitude.getText().toString());
                LatLng latLng = new LatLng(latitude_d, longitude_d);
                //mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setNegativeButton("Close",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
