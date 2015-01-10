package sheyko.aleksey.mapthetrip.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.Secure;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.adapters.NavigationAdapter;
import sheyko.aleksey.mapthetrip.utils.SendLocationService;
import sheyko.aleksey.mapthetrip.utils.UpdateTripStatusTask;


public class MainActivity extends Activity
        implements ConnectionCallbacks, LocationListener {
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
    private Intent sendLocationIntent;

    // Map
    private GoogleMap mMap;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private static final int UPDATE_INTERVAL = 120 * 1000;
    private static final int FASTEST_INTERVAL = 5 * 1000;
    private Location startLocation;
    private Editor editor;

    // Control buttons
    private Button startButton, pauseButton, finishButton;
    private TextView startButtonLabel, pauseButtonLabel, finishButtonLabel;
    private SharedPreferences sharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPrefs = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_APPEND);
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

                // Enables location tracking
                mLocationClient.connect();

                // Shows duration to user
                startTimer();
                if (mTripId == null)
                    durationCounter.setText(getString(R.string.duration_default_value));

                // UpdateTripStatus
                if (mTripId != null) {
                    try {
                        new UpdateTripStatusTask().execute(
                                mTripId,
                                "Resume",
                                getCurrentDateTime(),
                                getTimeZone(),
                                getUserId());
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    startService(sendLocationIntent);
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
                    stopService(sendLocationIntent);

                try {
                    new UpdateTripStatusTask().execute(
                            mTripId,
                            "Pause",
                            getCurrentDateTime(),
                            getTimeZone(),
                            getUserId());
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
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
                // Save trip data
                editor.putString("Duration", elapsedSeconds + "");
                editor.putString("DateTime", new SimpleDateFormat("dd MMM, hh:mm").format(new Date()).toLowerCase());
                editor.commit();

                try {
                    // Update trip status
                    new UpdateTripStatusTask().execute(
                            mTripId,
                            "Finish",
                            getCurrentDateTime(),
                            getTimeZone(),
                            getUserId());
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                startActivity(new Intent(MainActivity.this, SummaryActivity.class));
            }
        });
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
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        NavigationAdapter adapter = new NavigationAdapter(this, mActionTitles, mImageIds);
        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

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

        // Button labels
        startButtonLabel = (TextView) findViewById(R.id.start_button_label);
        pauseButtonLabel = (TextView) findViewById(R.id.pause_button_label);
        finishButtonLabel = (TextView) findViewById(R.id.finish_button_label);

        // UI counters
        countersContainer = (LinearLayout) findViewById(R.id.counters_container);
        durationCounter = (TextView) findViewById(R.id.duration_counter);
        distanceCounter = (TextView) findViewById(R.id.distance_counter);

        // Also we will get SharedPrefs here
        SharedPreferences sharedPrefs = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_APPEND);
        editor = sharedPrefs.edit();

        getDeviceId();
    }

    private String mDeviceId;

    private String getDeviceId() {
        mDeviceId = Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Parse.initialize(this, "w7h87LOw8fzK84g0noTS1b4nZhWYXBbRCendV756", "0uzaKEj3Q9R0kTRlq6pg4vawar1HkMTrWFeZ46Yb");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PhoneReg");
        query.whereEqualTo("deviceId", mDeviceId);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (e == null) {
                    if (scoreList.size() == 0) {
                        // Register ID if doesn't exist
                        ParseObject phoneReg = new ParseObject("PhoneReg");
                        phoneReg.put("deviceId", mDeviceId);
                        phoneReg.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                getDeviceId();
                            }
                        });
                    }
                }
            }
        });
        return mDeviceId;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Navigation drawer toggle
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
    }

    public void setTripId(String mTripId) {
        this.mTripId = mTripId;
        editor.putString("TripId", mTripId).commit();
    }

    // Location variables
    private LatLng previousLatLng;

    private String mPreviousState;

    @Override
    public void onLocationChanged(Location location) {

        String currentNewState;
        if (isCountdownJustStarted) {
            try {
                // Register device
                new RegisterDeviceTask().execute();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            // Save first point (to calculate distance)
            startLocation = location;

            // Update counter
            isCountdownJustStarted = false;

            currentNewState = "";
            mPreviousState = "";
            sharedPrefs.edit().putString("States", "").commit();
        }

        currentNewState = logDistanceForEachState(location.getLatitude(), location.getLongitude());
        String loggedStates = sharedPrefs.getString("States", "");
        if (!currentNewState.equals("") /* Не стоит на месте */
                && !mPreviousState.equals(currentNewState)) { /* Типо не в том же штате*/
            if (!loggedStates.equals("")) {
                loggedStates = loggedStates + ", " + currentNewState;
            } else {
                loggedStates = currentNewState;
            }
            sharedPrefs.edit().putString("States", loggedStates).commit();
            mPreviousState = currentNewState;
        }

        LatLng currentLatLng = new LatLng(
                location.getLatitude(), location.getLongitude());

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
        String totalDistanceString = calculateDistance(startLocation, location);

        // Update UI
        distanceCounter.setText(totalDistanceString);
        editor.putString("Distance", totalDistanceString).commit();

        // Current turns into previous on the next iteration
        previousLatLng = currentLatLng;
    }

    private double previousLat;
    private double previousLng;

    private String logDistanceForEachState(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.US);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException ignored) {
        }

        String state;
        if (addresses != null && previousLat != latitude && previousLng != longitude) {
            state = addresses.get(0).getAdminArea();

            Map<String, String> states = new HashMap<>();
            states.put("Alabama", "AL");
            states.put("Alaska", "AK");
            states.put("Alberta", "AB");
            states.put("American Samoa", "AS");
            states.put("Arizona", "AZ");
            states.put("Arkansas", "AR");
            states.put("Armed Forces (AE)", "AE");
            states.put("Armed Forces Americas", "AA");
            states.put("Armed Forces Pacific", "AP");
            states.put("British Columbia", "BC");
            states.put("California", "CA");
            states.put("Colorado", "CO");
            states.put("Connecticut", "CT");
            states.put("Delaware", "DE");
            states.put("District Of Columbia", "DC");
            states.put("Florida", "FL");
            states.put("Georgia", "GA");
            states.put("Guam", "GU");
            states.put("Hawaii", "HI");
            states.put("Idaho", "ID");
            states.put("Illinois", "IL");
            states.put("Indiana", "IN");
            states.put("Iowa", "IA");
            states.put("Kansas", "KS");
            states.put("Kentucky", "KY");
            states.put("Louisiana", "LA");
            states.put("Maine", "ME");
            states.put("Manitoba", "MB");
            states.put("Maryland", "MD");
            states.put("Massachusetts", "MA");
            states.put("Michigan", "MI");
            states.put("Minnesota", "MN");
            states.put("Mississippi", "MS");
            states.put("Missouri", "MO");
            states.put("Montana", "MT");
            states.put("Nebraska", "NE");
            states.put("Nevada", "NV");
            states.put("New Brunswick", "NB");
            states.put("New Hampshire", "NH");
            states.put("New Jersey", "NJ");
            states.put("New Mexico", "NM");
            states.put("New York", "NY");
            states.put("Newfoundland", "NF");
            states.put("North Carolina", "NC");
            states.put("North Dakota", "ND");
            states.put("Northwest Territories", "NT");
            states.put("Nova Scotia", "NS");
            states.put("Nunavut", "NU");
            states.put("Ohio", "OH");
            states.put("Oklahoma", "OK");
            states.put("Ontario", "ON");
            states.put("Oregon", "OR");
            states.put("Pennsylvania", "PA");
            states.put("Prince Edward Island", "PE");
            states.put("Puerto Rico", "PR");
            states.put("Quebec", "PQ");
            states.put("Rhode Island", "RI");
            states.put("Saskatchewan", "SK");
            states.put("South Carolina", "SC");
            states.put("South Dakota", "SD");
            states.put("Tennessee", "TN");
            states.put("Texas", "TX");
            states.put("Utah", "UT");
            states.put("Vermont", "VT");
            states.put("Virgin Islands", "VI");
            states.put("Virginia", "VA");
            states.put("Washington", "WA");
            states.put("West Virginia", "WV");
            states.put("Wisconsin", "WI");
            states.put("Wyoming", "WY");
            states.put("Yukon Territory", "YT");

            previousLat = latitude;
            previousLng = longitude;

            if (states.get(state) != null) {
                return states.get(state);
            }
            return "";
        } else {
            return "";
        }
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
        private String mTripId;

        @Override
        protected String doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain JSON response
            String resultJsonStr;

            try {
                // Construct the URL for the query
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("wsapp.mapthetrip.com")
                        .appendPath("TrucFuelLog.svc")
                        .appendPath("TFLRegDeviceandGetTripId")
                        .appendQueryParameter("DeviceUID", getDeviceId())
                        .appendQueryParameter("DeviceName", getDeviceName())
                        .appendQueryParameter("DeviceType", getDeviceType())
                        .appendQueryParameter("DeviceManufacturerName", getDeviceManufacturer())
                        .appendQueryParameter("DeviceModelName", getDeviceModel())
                        .appendQueryParameter("DeviceModelNumber", "1")
                        .appendQueryParameter("DeviceSystemName", "Android")
                        .appendQueryParameter("DeviceSystemVersion", getAndroidVersion())
                        .appendQueryParameter("DeviceSoftwareVersion", "1.0")
                        .appendQueryParameter("DevicePlatformVersion", getAndroidVersion())
                        .appendQueryParameter("DeviceFirmwareVersion", getAndroidVersion())
                        .appendQueryParameter("DeviceOS", "Android")
                        .appendQueryParameter("DeviceTimezone", getTimeZone())
                        .appendQueryParameter("LanguageUsedOnDevice", getDeviceLanguage())
                        .appendQueryParameter("HasCamera", isCameraAvailable())
                        .appendQueryParameter("UserId", "1")
                        .appendQueryParameter("TripDateTime", URLDecoder.decode(getCurrentDateTime()))
                        .appendQueryParameter("TripTimezone", getTimeZone())
                        .appendQueryParameter("UserDefinedTripId", "")
                        .appendQueryParameter("TripReferenceNumber", "")
                        .appendQueryParameter("EntityId", "1");
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

            setTripId(mTripId);
            // Start service to send location
            sendLocationIntent = new Intent(MainActivity.this, SendLocationService.class);
            sendLocationIntent.putExtra("action", "startTimer");
            sendLocationIntent.putExtra("tripId", mTripId);
            startService(sendLocationIntent);
        }
    }

    /* Device info getters */
    public String getDeviceName() {
        return Build.MODEL;
    }

    public String getDeviceType() {
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            return "Tablet";
        } else {
            return "Phone";
        }
    }

    public String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    public String getDeviceModel() {
        return Build.MODEL;
    }

    public String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    public String getTimeZone() {
        return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
    }

    public String getDeviceLanguage() {
        return Locale.getDefault().getDisplayName();
    }

    public String isCameraAvailable() {
        if (this.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return "true";
        } else {
            return "false";
        }
    }

    public String getUserId() {
        return "1";
    }

    public String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date());
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


    // Click listener for side navigation
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            } else if (position == 4) {
                startActivity(new Intent(MainActivity.this, StatsActivity.class));
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
