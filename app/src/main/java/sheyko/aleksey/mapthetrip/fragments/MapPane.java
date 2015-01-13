package sheyko.aleksey.mapthetrip.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import sheyko.aleksey.mapthetrip.activities.SummaryActivity;
import sheyko.aleksey.mapthetrip.helpers.Constants.Map;
import sheyko.aleksey.mapthetrip.helpers.Constants.Timer.Commands;
import sheyko.aleksey.mapthetrip.helpers.Constants.Trip.Status;
import sheyko.aleksey.mapthetrip.utils.RegisterDeviceTask;
import sheyko.aleksey.mapthetrip.utils.UpdateTripStatusTask;

public class MapPane extends Fragment
        implements ConnectionCallbacks, LocationListener {

    OnActionbarTabSelectedListener mCallback;

    // Time counter
    private LinearLayout countersContainer;
    private TextView durationCounter;
    private TextView distanceCounter;
    private TimerTask timerTask;
    private boolean isCountdownJustStarted = true;
    private int elapsedSeconds = 0;

    public String mTripId;
    private Intent sendLocationIntent;

    // Map
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private Location startLocation;

    // Control buttons
    private Button startButton, pauseButton, finishButton;
    private TextView startButtonLabel, pauseButtonLabel, finishButtonLabel;
    private LocationClient mLocationClient;

    public MapPane() {
        // Required empty public constructor
    }

    // Main Activity will implement this interface
    public interface OnActionbarTabSelectedListener {
        public void onTabSelected(int position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnActionbarTabSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
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

        createMap();

        // Start button listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getLocationClient().connect();

                updateUiOnStart();

                startTimer();

                // Update trip status
                if (mTripId != null) {
                    new UpdateTripStatusTask().execute(
                            Status.RESUME);

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
                stopTimer(Commands.PAUSE);

                if (sendLocationIntent != null)
                    getActivity().stopService(sendLocationIntent);

                new UpdateTripStatusTask().execute(
                        Status.PAUSE);
            }

            private void updateUiOnPause() {
                mCallback.onTabSelected(2);

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
                        Status.FINISH);

                startActivity(new Intent(getActivity(), SummaryActivity.class));
            }
        });
    }

    private void createMap() {
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
    }

    // Location variables
    Location previousLocation;

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

        if (previousLocation == null)
            previousLocation = currentLocation;

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(currentLatLng)
                .bearing(currentLocation.bearingTo(previousLocation))
                .tilt(30)
                .zoom(17)
                .build()));

        // Draw path on map
        mMap.addPolygon(new PolygonOptions()
                .strokeColor(Color.parseColor("#9f5c8f"))
                .add(new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()),
                        new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));

        // Calculate distance
        String totalDistanceString = getDistance(startLocation, currentLocation);

        getTrip.increazeDistance(getDistance(previousLocation, currentLocation))

        // Update UI
        distanceCounter.setText(totalDistanceString);
        //TODO        sharedPrefs.edit().putString("Distance", totalDistanceString).commit();

        // Current turns into previous on the next iteration
        previousLocation = startLocation;
    }

    private String getDistance(Location startLocation, Location finalLocation) {
        double distance = finalLocation.distanceTo(startLocation) / 1000;
        return String.format("%.1f", distance);
    }

    private void updateUiOnStart() {
        mCallback.onTabSelected(1);

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
        if (pauseOrStop == Commands.STOP) elapsedSeconds = 0;
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
