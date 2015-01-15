package sheyko.aleksey.mapthetrip.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

import java.util.concurrent.TimeUnit;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.models.Trip;
import sheyko.aleksey.mapthetrip.models.Trip.Status;
import sheyko.aleksey.mapthetrip.ui.activities.SummaryActivity;
import sheyko.aleksey.mapthetrip.utils.helpers.Constants.ActionBar.Tab;
import sheyko.aleksey.mapthetrip.utils.helpers.Constants.Map;
import sheyko.aleksey.mapthetrip.utils.helpers.Constants.Timer.Commands;
import sheyko.aleksey.mapthetrip.utils.tasks.UpdateTripStatusTask;

public class MapPane extends Fragment
        implements LocationListener, OnClickListener, ConnectionCallbacks {

    // Callback to update tabs in MainActivity
    OnTabSelectedListener mCallback;

    // Time counter
    private LinearLayout countersContainer;
    private TextView durationCounter;
    private TextView distanceCounter;

    private Trip mCurrentTrip;
    private String mTripId;

    // Location variables
    private Intent sendIntent;
    private LocationClient mLocationClient;
    private LocationRequest locationRequest;
    private Location previousLocation;
    private GoogleMap mMap;

    // Control buttons
    private Button mStartButton;
    private Button mPauseButton;
    private Button mFinishButton;

    // Button labels
    private TextView mStartButtonLabel;
    private TextView mPauseButtonLabel;
    private TextView mFinishButtonLabel;

    // Required empty constructor
    public MapPane() {
    }

    public interface OnTabSelectedListener {
        public void onTabSelected(int position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Actionbar tabs listener
        mCallback = (OnTabSelectedListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        mMap = disableMapUiControls(
                getFragmentManager().findFragmentById(R.id.map));

        mStartButton = (Button) rootView.findViewById(R.id.startButton);
        mStartButton.setOnClickListener(this);

        mPauseButton = (Button) rootView.findViewById(R.id.pauseButton);
        mPauseButton.setOnClickListener(this);

        mFinishButton = (Button) rootView.findViewById(R.id.finishButton);
        mFinishButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startButton:
                updateUiOnStart();

                if (mCurrentTrip == null) {

                    mCurrentTrip = new Trip();
                    mCurrentTrip.start(this.getActivity());
                } else {
                    mCurrentTrip.resume();
                }

                break;
            case R.id.pauseButton:
                updateUiOnPause();

                mCurrentTrip.pause();

                break;
            case R.id.finishButton:

                mCurrentTrip.finish();

                startActivity(new Intent(
                        this.getActivity(), SummaryActivity.class));
                break;
        }
    }

    private void initializeViews(View rootView) {

        // Button labels
        mStartButtonLabel = (TextView) rootView.findViewById(R.id.start_button_label);
        mPauseButtonLabel = (TextView) rootView.findViewById(R.id.pause_button_label);
        mFinishButtonLabel = (TextView) rootView.findViewById(R.id.finish_button_label);

        // UI counters
        countersContainer = (LinearLayout) rootView.findViewById(R.id.counters_container);
        durationCounter = (TextView) rootView.findViewById(R.id.duration_counter);
        distanceCounter = (TextView) rootView.findViewById(R.id.distance_counter);
    }

    private void updateUiOnStart() {
        createLocationClient(this.getActivity())
                .connect();

        mCallback.onTabSelected(Tab.REST);

        mStartButton.setVisibility(View.GONE);
        mStartButtonLabel.setVisibility(View.GONE);

        mPauseButton.setVisibility(View.VISIBLE);
        mPauseButtonLabel.setVisibility(View.VISIBLE);

        mFinishButton.setVisibility(View.GONE);
        mFinishButtonLabel.setVisibility(View.GONE);

        countersContainer.setVisibility(View.VISIBLE);

        startUiStopwatch();
    }

    private void updateUiOnPause() {
        stopLocationUpdates(mLocationClient);

        mCallback.onTabSelected(Tab.REST);

        mPauseButton.setVisibility(View.GONE);
        mPauseButtonLabel.setVisibility(View.GONE);

        mStartButton.setVisibility(View.VISIBLE);
        mStartButtonLabel.setVisibility(View.VISIBLE);

        mFinishButton.setVisibility(View.VISIBLE);
        mFinishButtonLabel.setVisibility(View.VISIBLE);

        mStartButtonLabel.setText(R.string.resume_trip_button_label);

        pauseUiStopWatch();
    }

    private GoogleMap disableMapUiControls(Fragment fragment) {
        GoogleMap map = ((MapFragment) fragment).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        return map;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        startLocationUpdates(mLocationClient);
    }

    private LocationClient createLocationClient(Context context) {
        // Create location client
        mLocationClient = new LocationClient(context, this, null);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        int UPDATE_INTERVAL = 5 * 1000; // 5 seconds
        locationRequest.setInterval(UPDATE_INTERVAL);
        return mLocationClient;
    }

    private void startLocationUpdates(LocationClient client) {
        client.requestLocationUpdates(locationRequest, this);
    }

    private void stopLocationUpdates(LocationClient client) {
        client.removeLocationUpdates(this);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onLocationChanged(Location currentLocation) {

        if (previousLocation == null)
            previousLocation = currentLocation;

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .bearing(previousLocation.bearingTo(currentLocation))
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

    public void adjustTimer(int pauseOrStop) {
        //TODO        if (timerTask != null) {
        //            timerTask.cancel();
        //            timerTask = null;
        //        }
        //        if (pauseOrStop == Commands.STOP) elapsedSeconds = 0;
    }
}
