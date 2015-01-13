package sheyko.aleksey.mapthetrip.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.util.Timer;
import java.util.TimerTask;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.helpers.Constants.ActionBar.Tab;
import sheyko.aleksey.mapthetrip.helpers.Constants.Map;
import sheyko.aleksey.mapthetrip.helpers.Constants.Timer.Commands;
import sheyko.aleksey.mapthetrip.helpers.Constants.Trip.Status;
import sheyko.aleksey.mapthetrip.models.Trip;
import sheyko.aleksey.mapthetrip.services.SendLocationService;
import sheyko.aleksey.mapthetrip.ui.activities.SummaryActivity;
import sheyko.aleksey.mapthetrip.utils.RegisterDeviceTask.OnGetTripIdListener;
import sheyko.aleksey.mapthetrip.utils.UpdateTripStatusTask;

public class MapPane extends Fragment
        implements ConnectionCallbacks, LocationListener, OnGetTripIdListener {

    // Callback to update tabs in MainActivity
    OnActionbarTabSelectedListener mCallback;

    // Time counter
    private LinearLayout countersContainer;
    private TextView durationCounter;
    private TextView distanceCounter;
    private TimerTask timerTask;
    private int elapsedSeconds = 0;

    private Trip mCurrentTrip;
    public String mTripId;

    // Location variables
    private Intent sendLocationIntent;
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    Location previousLocation;

    // Control buttons
    private Button startButton, pauseButton, finishButton;
    private TextView startButtonLabel, pauseButtonLabel, finishButtonLabel;

    public MapPane() {
    }

    // Main Activity will implement this interface
    public interface OnActionbarTabSelectedListener {
        public void onTabSelected(int position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (OnActionbarTabSelectedListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        disableMapUiControls(getMap());

        // Start button listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI(Status.RESUME);
                startTimer();

                // Register new trip ID
                mCurrentTrip = new Trip(getDeviceId(), getDeviceType(), isCameraAvailable());

                // If continued, update trip status
                // to «resumed»
                if (mTripId != null) {
                    startLocationUpdates();

                    new UpdateTripStatusTask().execute(
                            mTripId,
                            Status.RESUME);

                    getActivity().startService(sendLocationIntent);
                }
            }

            public String getDeviceId() {
                return Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
            }

            public String getDeviceType() {
                boolean tabletSize = getActivity().getResources().getBoolean(R.bool.isTablet);
                if (tabletSize) {
                    return  "Tablet";
                } else {
                    return "Phone";
                }
            }

            public String isCameraAvailable() {
                if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    return "true";
                } else {
                    return "false";
                }
            }
        });

        // Pause button listener
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI(Status.PAUSE);
                adjustTimer(Commands.PAUSE);

                stopLocationUpdates();

                // Update status on server
                // to «paused»
                new UpdateTripStatusTask().execute(
                        mTripId,
                        Status.PAUSE);

                if (sendLocationIntent != null)
                    getActivity().stopService(sendLocationIntent);
            }
        });

        // Finish button listener
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Update trip status
                new UpdateTripStatusTask().execute(
                        mTripId,
                        Status.FINISH);

                startActivity(new Intent(getActivity(), SummaryActivity.class));
            }
        });
    }

    private void updateUI(String tripStatus) {
        switch (tripStatus) {
            case Status.RESUME:
                mCallback.onTabSelected(Tab.REST);

                startButton.setVisibility(View.GONE);
                startButtonLabel.setVisibility(View.GONE);

                pauseButton.setVisibility(View.VISIBLE);
                pauseButtonLabel.setVisibility(View.VISIBLE);

                finishButton.setVisibility(View.GONE);
                finishButtonLabel.setVisibility(View.GONE);

                countersContainer.setVisibility(View.VISIBLE);
                break;
            case Status.PAUSE:
                mCallback.onTabSelected(Tab.REST);

                pauseButton.setVisibility(View.GONE);
                pauseButtonLabel.setVisibility(View.GONE);

                startButton.setVisibility(View.VISIBLE);
                startButtonLabel.setVisibility(View.VISIBLE);

                finishButton.setVisibility(View.VISIBLE);
                finishButtonLabel.setVisibility(View.VISIBLE);

                startButtonLabel.setText(R.string.resume_trip_button_label);
                break;
        }
    }

    @Override
    public void onIdRetrieved(String tripId) {
        mCurrentTrip.setTripId(tripId);
        mTripId = tripId;

        getActivity().startService(new Intent(getActivity(), SendLocationService.class)
                .putExtra("action", "startTimer")
                .putExtra("tripId", mTripId));

        getLocationClient().connect();
    }

    private GoogleMap getMap() {
        return ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
    }

    private void disableMapUiControls(GoogleMap map) {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        mLocationClient.removeLocationUpdates(this);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onLocationChanged(Location currentLocation) {

        if (previousLocation == null)
            previousLocation = currentLocation;

        GoogleMap mMap = getMap();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .bearing(currentLocation.bearingTo(previousLocation))
                .tilt(30)
                .zoom(17)
                .build()));

        // Draw path on map
        mMap.addPolygon(new PolygonOptions()
                .strokeColor(Color.parseColor("#9f5c8f"))
                .add(new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()),
                        new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));

        // Increment distance by current sector
        mCurrentTrip.increazeDistance(getDistance(previousLocation, currentLocation));

        // Update UI
        distanceCounter.setText(formatDistanceString(mCurrentTrip.getDistance()));

        // Current turns into previous on the next iteration
        previousLocation = currentLocation;
    }

    private float getDistance(Location previousLocation, Location currentLocation) {
        return previousLocation.distanceTo(currentLocation) / 1000;
    }

    private String formatDistanceString(float distance) {
        return String.format("%.1f", distance);
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

    public void adjustTimer(int pauseOrStop) {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (pauseOrStop == Commands.STOP) elapsedSeconds = 0;
    }

    private String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private LocationClient getLocationClient() {
        // Create location client
        mLocationClient = new LocationClient(getActivity(), this, null);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(Map.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(Map.UPDATE_INTERVAL);

        return mLocationClient;
    }

    private void initializeViews(View rootView) {
        // Control buttons
        startButton = (Button) rootView.findViewById(R.id.yellow_button_start);
        pauseButton = (Button) rootView.findViewById(R.id.red_button_pause);
        finishButton = (Button) rootView.findViewById(R.id.black_button_finish);

        // Button labels
        startButtonLabel = (TextView) rootView.findViewById(R.id.start_button_label);
        pauseButtonLabel = (TextView) rootView.findViewById(R.id.pause_button_label);
        finishButtonLabel = (TextView) rootView.findViewById(R.id.finish_button_label);

        // UI counters
        countersContainer = (LinearLayout) rootView.findViewById(R.id.counters_container);
        durationCounter = (TextView) rootView.findViewById(R.id.duration_counter);
        distanceCounter = (TextView) rootView.findViewById(R.id.distance_counter);
    }
}
