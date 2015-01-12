package sheyko.aleksey.mapthetrip.fragments;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.activities.SummaryActivity;
import sheyko.aleksey.mapthetrip.helpers.Constants;
import sheyko.aleksey.mapthetrip.helpers.Constants.TripStates;
import sheyko.aleksey.mapthetrip.models.DeviceInfo;
import sheyko.aleksey.mapthetrip.models.Trip;
import sheyko.aleksey.mapthetrip.services.SendLocationService;
import sheyko.aleksey.mapthetrip.utils.UpdateTripStatusTask;

public class MapPane extends Fragment
        implements ConnectionCallbacks, LocationListener {
    public static final String TAG = MapPane.class.getSimpleName();

    // Action bar
    private ActionBar mActionBar;
    private Tab tab1;
    private Tab tab2;
    private Tab tab3;

    // Time counter
    private LinearLayout countersContainer;
    private TextView durationCounter;
    private TextView distanceCounter;
    private TimerTask timerTask;
    private boolean isCountdownJustStarted = true;
    private int elapsedSeconds = 0;
    private static final int JUST_PAUSE = 0;
    private static final int STOP = 1;

    // User session
    public String mTripId;
    private Intent sendLocationIntent;

    // Map
    private GoogleMap mMap;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private static final int UPDATE_INTERVAL = 5 * 1000;
    private Location startLocation;

    // Control buttons
    private Button startButton, pauseButton, finishButton;
    private TextView startButtonLabel, pauseButtonLabel, finishButtonLabel;

    public MapPane() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createLocationClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        addActionBarTabs();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);

        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Start button listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUiOnStart();

                startTimer();

                // UpdateTripStatus
                if (mTripId != null) {
                    try {
                        new UpdateTripStatusTask().execute(
                                mTripId,
                                TripStates.resume,
                                new DeviceInfo().getCurrentDateTime(),
                                new DeviceInfo().getTimeZone(),
                                Constants.userId);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    getActivity().startService(sendLocationIntent);
                }
            }
        });

        // Pause button listener
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUiOnPause();

                if (mLocationClient.isConnected()) {
                    // Disconnecting the client invalidates it.
                    mLocationClient.disconnect();
                }
                stopTimer(JUST_PAUSE);

                if (sendLocationIntent != null)
                    getActivity().stopService(sendLocationIntent);

                try {
                    new UpdateTripStatusTask().execute(
                            mTripId,
                            TripStates.pause,
                            new DeviceInfo().getCurrentDateTime(),
                            new DeviceInfo().getTimeZone(),
                            Constants.userId);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            private void updateUiOnPause() {
                mActionBar.setTitle(getString(R.string.pause_label));
                mActionBar.removeAllTabs();
                mActionBar.addTab(tab1, 0, false);
                mActionBar.addTab(tab2, 1, false);
                mActionBar.addTab(tab3, 2, true);

                pauseButton.setVisibility(View.GONE);
                pauseButtonLabel.setVisibility(View.GONE);

                startButton.setVisibility(View.VISIBLE);
                startButtonLabel.setVisibility(View.VISIBLE);

                finishButton.setVisibility(View.VISIBLE);
                finishButtonLabel.setVisibility(View.VISIBLE);

                startButtonLabel.setText(R.string.resume_trip_button_label);
            }
        });

        // Finish button listener
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save trip data
                //TODO                sharedPrefs.edit()
                //                        .putString("Duration", elapsedSeconds + "")
                //                        .putString("DateTime", new SimpleDateFormat("dd MMM, hh:mm").format(new Date()).toLowerCase())
                //                        .commit();

                // Update trip status
                new UpdateTripStatusTask().execute(
                        mTripId,
                        TripStates.finish,
                        new DeviceInfo().getCurrentDateTime(),
                        new DeviceInfo().getTimeZone(),
                        Constants.userId);

                startActivity(new Intent(getActivity(), SummaryActivity.class));
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
    }

    // Location variables
    private LatLng previousLatLng;

    @Override
    public void onLocationChanged(Location currentLocation) {

        if (isCountdownJustStarted) {
            new RegisterDeviceTask().execute();

            // Save first point (to calculate distance)
            startLocation = currentLocation;

            // Update counter
            isCountdownJustStarted = false;
        }

        LatLng currentLatLng = new LatLng(
                currentLocation.getLatitude(), currentLocation.getLongitude());

        if (previousLatLng == null)
            previousLatLng = currentLatLng;

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(currentLatLng)
                .bearing(bearingBetweenLatLngs(currentLatLng, previousLatLng))
                .tilt(30)
                .zoom(17)
                .build()));

        // Draw path on map
        mMap.addPolygon(new PolygonOptions()
                .strokeColor(Color.parseColor("#9f5c8f"))
                .add(previousLatLng, currentLatLng));

        // Calculate distance
        String totalDistanceString = calculateDistance(startLocation, currentLocation);

        // Update UI
        distanceCounter.setText(totalDistanceString);
        //TODO        sharedPrefs.edit().putString("Distance", totalDistanceString).commit();

        // Current turns into previous on the next iteration
        previousLatLng = currentLatLng;
    }

    private void updateUiOnStart() {
        mActionBar.setTitle(getString(R.string.recording_label));
        mActionBar.removeAllTabs();
        mActionBar.addTab(tab1, 0, false);
        mActionBar.addTab(tab2, 1, true);
        mActionBar.addTab(tab3, 2, false);

        startButton.setVisibility(View.GONE);
        startButtonLabel.setVisibility(View.GONE);

        pauseButton.setVisibility(View.VISIBLE);
        pauseButtonLabel.setVisibility(View.VISIBLE);

        finishButton.setVisibility(View.GONE);
        finishButtonLabel.setVisibility(View.GONE);

        countersContainer.setVisibility(View.VISIBLE);
    }

    // Countdown timer
    public void startTimer() {
        final Handler handler = new Handler();
        Timer mTimer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        durationCounter.setText(convertSecondsToHMmSs(elapsedSeconds));
                        elapsedSeconds++;
                    }
                });
            }
        };
        mTimer.schedule(timerTask, 0, 1000);
    }

    private String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public void stopTimer(int pauseOrStop) {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (pauseOrStop == STOP) elapsedSeconds = 0;
    }

    private void addActionBarTabs() {
        // Setup action bar for tabs
        mActionBar = getActivity().getActionBar();
        if (mActionBar != null) {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            TabListener tabListener = new TabListener() {
                @Override
                public void onTabSelected(Tab tab, FragmentTransaction ft) {
                }

                @Override
                public void onTabUnselected(Tab tab, FragmentTransaction ft) {
                }

                @Override
                public void onTabReselected(Tab tab, FragmentTransaction ft) {
                }
            };

            tab1 = mActionBar.newTab()
                    .setText(R.string.travel_tab_label)
                    .setIcon(R.drawable.ic_travel);
            tab1.setTabListener(tabListener);
            mActionBar.addTab(tab1);

            tab2 = mActionBar.newTab()
                    .setText(R.string.gas_tab_label)
                    .setIcon(R.drawable.ic_gas);
            tab2.setTabListener(tabListener);
            mActionBar.addTab(tab2);

            tab3 = mActionBar.newTab()
                    .setText(R.string.rest_tab_label)
                    .setIcon(R.drawable.ic_rest);
            tab3.setTabListener(tabListener);
            mActionBar.addTab(tab3);
        }
    }


    private void createLocationClient() {
        // Create location client
        mLocationClient = new LocationClient(getActivity(), this, null);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
    }

    private void initializeViews(View container) {
        // Control buttons
        startButton = (Button) container.findViewById(R.id.yellow_button_start);
        pauseButton = (Button) container.findViewById(R.id.red_button_pause);
        finishButton = (Button) container.findViewById(R.id.black_button_finish);

        // Button labels
        startButtonLabel = (TextView) container.findViewById(R.id.start_button_label);
        pauseButtonLabel = (TextView) container.findViewById(R.id.pause_button_label);
        finishButtonLabel = (TextView) container.findViewById(R.id.finish_button_label);

        // UI counters
        countersContainer = (LinearLayout) container.findViewById(R.id.counters_container);
        durationCounter = (TextView) container.findViewById(R.id.duration_counter);
        distanceCounter = (TextView) container.findViewById(R.id.distance_counter);
    }

    private String calculateDistance(Location startLocation, Location finalLocation) {
        double totalDistance = finalLocation.distanceTo(startLocation) * 0.621371 / 1000 /* in miles */;
        String totalDistanceString;
        if (totalDistance <= 99) {
            totalDistanceString = String.format("%.2f", totalDistance);
        } else if (totalDistance >= 100 && totalDistance <= 999) {
            totalDistanceString = String.format("%.1f", totalDistance);
        } else {
            totalDistanceString = String.format("%.0f", totalDistance);
        }
        return totalDistanceString;
    }

    public class RegisterDeviceTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain JSON response
            String resultJsonStr;
            DeviceInfo deviceInfo = new DeviceInfo();

            try {
                // Construct the URL for the query
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("wsapp.mapthetrip.com")
                        .appendPath("TrucFuelLog.svc")
                        .appendPath("TFLRegDeviceandGetTripId")
                        .appendQueryParameter("DeviceUID", deviceInfo.getDeviceId())
                        .appendQueryParameter("DeviceName", deviceInfo.getModel())
                        .appendQueryParameter("DeviceType", deviceInfo.getDeviceType())
                        .appendQueryParameter("DeviceManufacturerName", deviceInfo.getManufacturer())
                        .appendQueryParameter("DeviceModelName", deviceInfo.getModel())
                        .appendQueryParameter("DeviceModelNumber", Constants.modelNumber)
                        .appendQueryParameter("DeviceSystemName", Constants.systemName)
                        .appendQueryParameter("DeviceSystemVersion", deviceInfo.getAndroidVersion())
                        .appendQueryParameter("DeviceSoftwareVersion", Constants.softwareVersion)
                        .appendQueryParameter("DevicePlatformVersion", deviceInfo.getAndroidVersion())
                        .appendQueryParameter("DeviceFirmwareVersion", deviceInfo.getAndroidVersion())
                        .appendQueryParameter("DeviceOS", Constants.systemName)
                        .appendQueryParameter("DeviceTimezone", deviceInfo.getTimeZone())
                        .appendQueryParameter("LanguageUsedOnDevice", deviceInfo.getLocale())
                        .appendQueryParameter("HasCamera", deviceInfo.isCameraAvailable())
                        .appendQueryParameter("UserId", Constants.userId)
                        .appendQueryParameter("TripDateTime", deviceInfo.getCurrentDateTime())
                        .appendQueryParameter("TripTimezone", deviceInfo.getTimeZone())
                        .appendQueryParameter("UserDefinedTripId", Constants.userDefinedTripId)
                        .appendQueryParameter("TripReferenceNumber", Constants.referenceNumber)
                        .appendQueryParameter("EntityId", Constants.entityId);

                String mUrlString = builder.build().toString();
                Log.i(TAG, "Service: TFLRegDeviceandGetTripIdResult,\n" +
                        "Query: " + java.net.URLDecoder.decode(mUrlString, "UTF-8"));

                URL mUrl = new URL(mUrlString);

                // Create the request and open the connection
                urlConnection = (HttpURLConnection) mUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) return null;

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                resultJsonStr = buffer.toString();

                Log.i(TAG, "Service: TFLRegDeviceandGetTripIdResult,\n" +
                        "Result: " + java.net.URLDecoder.decode(resultJsonStr, "UTF-8"));

                try {
                    JSONObject regResultObject = new JSONObject(resultJsonStr);
                    JSONObject regResponse = regResultObject.getJSONObject("TFLRegDeviceandGetTripIdResult");

                    String mParseStatus = regResponse.getString("Status");
                    if (mParseStatus.equals("Success")) {
                        mTripId = regResponse.getString("TripId");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return mTripId;
        }

        @Override
        protected void onPostExecute(String mTripId) {
            super.onPostExecute(mTripId);

            new Trip(mTripId + "").save();

            mLocationClient.connect();

            // Start service to send location
            getActivity().startService(new Intent(getActivity(), SendLocationService.class)
                    .putExtra("action", "startTimer")
                    .putExtra("tripId", mTripId));
        }
    }


    private Location convertLatLngToLocation(LatLng latLng) {
        Location location = new Location("someLoc");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    private float bearingBetweenLatLngs(LatLng beginLatLng, LatLng endLatLng) {
        Location beginLocation = convertLatLngToLocation(beginLatLng);
        Location endLocation = convertLatLngToLocation(endLatLng);
        return endLocation.bearingTo(beginLocation);
    }
}
