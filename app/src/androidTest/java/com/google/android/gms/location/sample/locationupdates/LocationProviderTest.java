/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.location.sample.locationupdates;

import android.location.Location;
import android.os.Build;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * Test showing use of mock location data to test code using the Google Play Location APIs. To
 * run this test, you must first check the "Allow mock locations" setting within
 * Settings -> Developer options.
 */
public class LocationProviderTest extends ActivityInstrumentationTestCase2<LocationProvider> {
    public static final String TAG = "LocationProviderTest";

    /**
     * The name of the mock location.
     */
    public static final String NORTH_POLE =
            "com.google.android.gms.location.sample.locationupdates" + ".NORTH_POLE";

    public static final float NORTH_POLE_LATITUDE = 90.0f;

    public static final float NORTH_POLE_LONGITUDE = 0.0f;

    public static final float ACCURACY_IN_METERS = 10.0f;

    public static final int AWAIT_TIMEOUT_IN_MILLISECONDS = 2000;

    /**
     * The activity under test.
     */
    private LocationProvider mLocationProvider;

    public LocationProviderTest() {
        super(LocationProvider.class);
    }

    /**
     * Gets the activity under test, obtains a connection to GoogleApiClient if necessary, and
     * sets mock location.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLocationProvider = getActivity();
        ensureGoogleApiClientConnection();
    }

    /**
     * Tests location using a mock location object.
     */
    public void testUsingMockLocation() {
        Log.v(TAG, "Testing current location");

        // Use a Location object set to the coordinates of the North Pole to set the mock location.
        setMockLocation(createNorthPoleLocation());

        // Make sure that the activity under test exists and GoogleApiClient is connected.
        confirmPreconditions();

        // Simulate a Start Updates button click.
        clickStartUpdatesButton();

        final Location testLocation = createNorthPoleLocation();

        assertEquals(testLocation.getLatitude(), mLocationProvider.mCurrentLocation.getLatitude(),
                0.000001f);
        assertEquals(testLocation.getLongitude(), mLocationProvider.mCurrentLocation.getLongitude(),
                 0.000001f);
        assertEquals(String.valueOf(testLocation.getLatitude()),
                "");
        assertEquals(String.valueOf(testLocation.getLongitude()),
               "");
    }

    /**
     * If a connection to GoogleApiClient is lost, attempts to reconnect.
     */
    private void ensureGoogleApiClientConnection() {
        if (!mLocationProvider.mGoogleApiClient.isConnected()) {
            mLocationProvider.mGoogleApiClient.blockingConnect();
        }
    }

    /**
     * Confirms that the activity under test exists and has a connected GoogleApiClient.
     */
    private void confirmPreconditions() {
        assertNotNull("mLocationProvider is null", mLocationProvider);
        assertTrue("GoogleApiClient is not connected", mLocationProvider.mGoogleApiClient.isConnected());
    }

    /**
     * Simulates a Start Updates button click.
     */
    private void clickStartUpdatesButton() {
        mLocationProvider.runOnUiThread(new Runnable() {
            public void run() {
                mLocationProvider.mStartUpdatesButton.performClick();
            }
        });
    }

    /**
     * Calls the asynchronous methods
     * {@link com.google.android.gms.location.FusedLocationProviderApi#setMockMode} and
     * {@link com.google.android.gms.location.FusedLocationProviderApi#setMockLocation} to set the
     * mock location. Uses nested callbacks when calling setMockMode() and setMockLocation().
     * Each method returns a {@link Status} through a
     * {@link com.google.android.gms.common.api.PendingResult<>}, and setMockLocation() is called
     * only if setMockMode() is successful. Maintains a
     * {@link CountDownLatch} with a count of 1, which makes the current thread wait. Decrements
     * the CountDownLatch count only if the mock location is successfully set, allowing the set up
     * to complete.
     */
    private void setMockLocation(final Location mockLocation) {
        // We use a CountDownLatch to ensure that all asynchronous tasks complete within setUp. We
        // set the CountDownLatch count to 1 and decrement this count only when we are certain that
        // mock location has been set.
        final CountDownLatch lock = new CountDownLatch(1);

        // First, ensure that the location provider is in mock mode. Using setMockMode() ensures
        // that only locations specified in setMockLocation(GoogleApiClient, Location) are used.
        LocationServices.FusedLocationApi.setMockMode(mLocationProvider.mGoogleApiClient, true)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.v(TAG, "Mock mode set");
                            // Set the mock location to be used for the location provider. This
                            // location is used in place of any actual locations from the underlying
                            // providers (network or gps).
                            LocationServices.FusedLocationApi.setMockLocation(
                                    mLocationProvider.mGoogleApiClient,
                                    mockLocation
                            ).setResultCallback(new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    if (status.isSuccess()) {
                                        Log.v(TAG, "Mock location set");
                                        // Decrement the count of the latch, releasing the waiting
                                        // thread. This permits lock.await() to return.
                                        Log.v(TAG, "Decrementing latch count");
                                        lock.countDown();
                                    } else {
                                        Log.e(TAG, "Mock location not set");
                                    }
                                }
                            });
                        } else {
                            Log.e(TAG, "Mock mode not set");
                        }
                    }
                });

        try {
            // Make the current thread wait until the latch has counted down to zero.
            Log.v(TAG, "Waiting until the latch has counted down to zero");
            lock.await(AWAIT_TIMEOUT_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Log.i(TAG, "Waiting thread awakened prematurely", exception);
        }
    }

    /**
     * Creates and returns a Location object set to the coordinates of the North Pole.
     */
    private Location createNorthPoleLocation() {
        Location mockLocation = new Location(NORTH_POLE);
        mockLocation.setLatitude(NORTH_POLE_LATITUDE);
        mockLocation.setLongitude(NORTH_POLE_LONGITUDE);
        mockLocation.setAccuracy(ACCURACY_IN_METERS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        mockLocation.setTime(System.currentTimeMillis());
        return mockLocation;
    }
}
