package sheyko.aleksey.mapmytrip.ui;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationClient;
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
import java.net.URLDecoder;
import java.util.Timer;
import java.util.TimerTask;

import sheyko.aleksey.mapmytrip.R;
import sheyko.aleksey.mapmytrip.utils.NavigationAdapter;
import sheyko.aleksey.mapmytrip.utils.UpdateTripStatusTask;


public class MainActivity extends Activity
        implements ConnectionCallbacks, com.google.android.gms.location.LocationListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Navigation drawer
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    // Action bar
    private ActionBar actionBar;
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

    // Map
    private GoogleMap mMap;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private static final int UPDATE_INTERVAL = 60 * 1000;
    private static final int FASTEST_INTERVAL = 10 * 1000;
    private Location startLocation;
    private Location finalLocation;
    private SharedPreferences sharedPrefs;
    private Editor editor;

    // Control buttons
    private Button startButton, pauseButton, finishButton;
    private TextView startButtonLabel, pauseButtonLabel, finishButtonLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        addActionBarTabs();
        addSideNavigation();

        createMap();
        createLocationClient();

        // Start button listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUiOnStart();

                mLocationClient.connect();

                startTimer();

                /*
                TODO
                if (mTripId != null)
                    new UpdateTripStatusTask().execute(mTripId, "Resume", getCurrentDateTime());
                */
            }

            private void updateUiOnStart() {
                actionBar.setTitle(getString(R.string.recording_label));
                actionBar.removeAllTabs();
                actionBar.addTab(tab1, 0, false);
                actionBar.addTab(tab2, 1, true);
                actionBar.addTab(tab3, 2, false);

                startButton.setVisibility(View.GONE);
                startButtonLabel.setVisibility(View.GONE);

                pauseButton.setVisibility(View.VISIBLE);
                pauseButtonLabel.setVisibility(View.VISIBLE);

                finishButton.setVisibility(View.GONE);
                finishButtonLabel.setVisibility(View.GONE);

                countersContainer.setVisibility(View.VISIBLE);
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

                new UpdateTripStatusTask().execute(mTripId, "Pause", getCurrentDateTime());
            }

            private void updateUiOnPause() {
                actionBar.setTitle(getString(R.string.pause_label));
                actionBar.removeAllTabs();
                actionBar.addTab(tab1, 0, false);
                actionBar.addTab(tab2, 1, false);
                actionBar.addTab(tab3, 2, true);

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
                // Update trip status
                new UpdateTripStatusTask().execute(mTripId, "Finish", getCurrentDateTime());
                startActivity(new Intent(MainActivity.this, InputActivity.class));
            }
        });

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

                    private String convertSecondsToHMmSs(long seconds) {
                        long s = seconds % 60;
                        long m = (seconds / 60) % 60;
                        long h = (seconds / (60 * 60)) % 24;
                        return String.format("%02d:%02d:%02d", h,m,s);
                    }
                });
            }};
        mTimer.schedule(timerTask, 0, 1000);
    }

    public void stopTimer(int pauseOrStop) {
        timerTask.cancel();
        timerTask = null;
        if (pauseOrStop == STOP) elapsedSeconds = 0;
    }

    private void addActionBarTabs() {
        // Setup action bar for tabs
        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        tab1 = actionBar.newTab()
                .setText(R.string.travel_tab_label)
                .setIcon(R.drawable.ic_travel)
                .setTabListener(new ActionBar.TabListener() {
                    @Override
                    public void onTabSelected(Tab tab, FragmentTransaction ft) {
                    }

                    @Override
                    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
                    }

                    @Override
                    public void onTabReselected(Tab tab, FragmentTransaction ft) {
                    }
                });
        actionBar.addTab(tab1);

        tab2 = actionBar.newTab()
                .setText(R.string.gas_tab_label)
                .setIcon(R.drawable.ic_gas)
                .setTabListener(new ActionBar.TabListener() {
                    @Override
                    public void onTabSelected(Tab tab, FragmentTransaction ft) {
                    }

                    @Override
                    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
                    }

                    @Override
                    public void onTabReselected(Tab tab, FragmentTransaction ft) {
                    }
                });
        actionBar.addTab(tab2);

        tab3 = actionBar.newTab()
                .setText(R.string.rest_tab_label)
                .setIcon(R.drawable.ic_rest)
                .setTabListener(new ActionBar.TabListener() {
                    @Override
                    public void onTabSelected(Tab tab, FragmentTransaction ft) {
                    }

                    @Override
                    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
                    }

                    @Override
                    public void onTabReselected(Tab tab, FragmentTransaction ft) {
                    }
                });
        actionBar.addTab(tab3);
        }
    }

    private void addSideNavigation() {
        mTitle = mDrawerTitle = getTitle();
        String[] mActionTitles = getResources().getStringArray(R.array.action_titles);
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);

        Integer[] mImageIds = new Integer[]{
                R.drawable.nav_record, R.drawable.nav_log_trip,
                R.drawable.nav_log_gas, R.drawable.nav_log_rest,
                R.drawable.nav_stats, R.drawable.nav_settings
        };
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        NavigationAdapter adapter = new NavigationAdapter(this, mActionTitles, mImageIds);
        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                actionBar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                actionBar.setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void createLocationClient() {
        // Create location client
        mLocationClient = new LocationClient(this, this, null);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    private void createMap() {
        // Create Google Map
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
    }

    private void initializeViews() {
        startButton = (Button) findViewById(R.id.yellow_button_start);
        pauseButton = (Button) findViewById(R.id.red_button_pause);
        finishButton = (Button) findViewById(R.id.black_button_finish);

        startButtonLabel = (TextView) findViewById(R.id.start_button_label);
        pauseButtonLabel = (TextView) findViewById(R.id.pause_button_label);
        finishButtonLabel = (TextView) findViewById(R.id.finish_button_label);

        countersContainer = (LinearLayout)
                findViewById(R.id.counters_container);

        durationCounter = (TextView) findViewById(R.id.duration_counter);
        distanceCounter = (TextView) findViewById(R.id.distance_counter);

        // Also we will get SharedPrefs here
        sharedPrefs = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_APPEND);
        editor = sharedPrefs.edit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }
    @Override
    public void onDisconnected() {}

    public void setTripId(String mTripId) {
        this.mTripId = mTripId;
        editor.putString("TripId", mTripId);
        Log.i(TAG, "А у вас вон то: TripId = " + mTripId);
    }

    // Location variables
    private LatLng previousLatLng;
    private Location previousLocation;

    @Override
    public void onLocationChanged(Location location) {

        if (isCountdownJustStarted) {
            // Register device at first startup
            new RegisterDeviceTask().execute();

            startLocation = location;
            isCountdownJustStarted = false;
        }

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (previousLocation == null) previousLatLng = currentLatLng;
        float mBearing = bearingBetweenLatLngs(currentLatLng, previousLatLng);

        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                    .target(currentLatLng)
                    .bearing(mBearing)
                    .tilt(45)
                    .zoom(17)
                    .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Instantiates a new Polygon object and adds points to define a rectangle
        PolygonOptions rectOptions;
        rectOptions = new PolygonOptions()
                .strokeColor(Color.parseColor("#9f5c8f"));

        // If new session
        if (previousLatLng == null) {
            rectOptions.add(new LatLng(currentLatLng.latitude, currentLatLng.longitude));
        } else {
            rectOptions.add(
                    new LatLng(previousLatLng.latitude, previousLatLng.longitude),
                    new LatLng(currentLatLng.latitude, currentLatLng.longitude));
        }
        // Get back the mutable Polygon
        mMap.addPolygon(rectOptions);

        previousLatLng = currentLatLng;
        previousLocation = location;
        finalLocation = location;

        double totalDistance = finalLocation.distanceTo(startLocation);

        if (sharedPrefs.getString("metric_units", "miles").equals("miles")) totalDistance = totalDistance * 0.621371;

        String totalDistanceString;

        if (totalDistance / 1000 <= 99) {
            totalDistanceString = String.format("%.2f", totalDistance / 1000);
        } else if (totalDistance / 1000 >= 100 && totalDistance / 1000 <= 999) {
            totalDistanceString = String.format("%.1f", totalDistance / 1000);
        } else {
            totalDistanceString = String.format("%.0f", totalDistance / 1000);
        }

        distanceCounter.setText(totalDistanceString);

        /*
        TODO
        if (mTripId != null) {
            new SendLocationTask().execute(
                    // ID
                    mTripId,
                    // Latitude
                    String.valueOf(currentLatLng.latitude),
                    // Longitude
                    String.valueOf(currentLatLng.longitude),
                    // Date and time
                    getCurrentDateTime());
        }
         */
    }

    public class RegisterDeviceTask extends AsyncTask<Void, Void, String> {
        private String mTripId;

        @Override
        protected String doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain JSON response as a string
            String resultJsonStr;

            try {
                // Construct the URL for the query
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("wsapp.mapthetrip.com")
                        .appendPath("TrucFuelLog.svc")
                        .appendPath("TFLRegDeviceandGetTripId")
                        .appendQueryParameter("DeviceUID", "12345")
                        .appendQueryParameter("DeviceName", "TestDevice")
                        .appendQueryParameter("DeviceType", "TestType")
                        .appendQueryParameter("DeviceManufacturerName", "Test")
                        .appendQueryParameter("DeviceModelName", "Test")
                        .appendQueryParameter("DeviceModelNumber", "12334")
                        .appendQueryParameter("DeviceSystemName", "TestSystem")
                        .appendQueryParameter("DeviceSystemVersion", "1.0")
                        .appendQueryParameter("DeviceSoftwareVersion", "1.0")
                        .appendQueryParameter("DevicePlatformVersion", "1.0")
                        .appendQueryParameter("DeviceFirmwareVersion", "1.0")
                        .appendQueryParameter("DeviceOS", "IOS")
                        .appendQueryParameter("DeviceTimezone", "EST")
                        .appendQueryParameter("LanguageUsedOnDevice", "English")
                        .appendQueryParameter("HasCamera", "true")
                        .appendQueryParameter("UserId", "1")
                        .appendQueryParameter("TripDateTime", URLDecoder.decode("2014-12-12%2010:10:10"))
                        .appendQueryParameter("TripTimezone", "IST")
                        .appendQueryParameter("UserDefinedTripId", "123")
                        .appendQueryParameter("TripReferenceNumber", "REF123")
                        .appendQueryParameter("EntityId", "1");
                String mUrlString = builder.build().toString();

                URL mUrl = new URL(mUrlString);

                // Create the request and open the connection
                urlConnection = (HttpURLConnection) mUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) return null;

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                resultJsonStr = buffer.toString();
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
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
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
            Log.i(TAG, "А у нас такое: mTripId = " + mTripId);
            setTripId(mTripId);
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

    private String getCurrentDateTime() {

        return "2014-12-18 10:10:10";
    }

    // Click listener for side navigation
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            } else if (position == 4) {
                startActivity(new Intent(MainActivity.this, SummaryActivity.class));
            } else {
                comingSoonToast();
            }
        }

        private void comingSoonToast() {
            Toast.makeText(MainActivity.this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}
