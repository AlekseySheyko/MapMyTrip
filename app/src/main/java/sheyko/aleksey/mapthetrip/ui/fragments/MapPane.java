package sheyko.aleksey.mapthetrip.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import java.util.Timer;
import java.util.TimerTask;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.models.Trip;
import sheyko.aleksey.mapthetrip.ui.activities.SummaryActivity;
import sheyko.aleksey.mapthetrip.utils.helpers.Constants.ActionBar.Tab;

public class MapPane extends Fragment
        implements OnClickListener {

    // Callback to update tabs in MainActivity
    OnTabSelectedListener mCallback;

    private TextView distanceCounter;
    private TextView durationCounter;

    private Trip mCurrentTrip;

    // Control buttons
    private Button mStartButton;
    private Button mPauseButton;
    private Button mFinishButton;

    // Button labels
    private TextView mStartButtonLabel;
    private TextView mPauseButtonLabel;
    private TextView mFinishButtonLabel;

    // Timer
    private TimerTask timerTask;
    private int elapsedSeconds = 0;
    private LinearLayout mCountersContainer;

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

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("GPSLocationUpdates"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Bundle b = intent.getBundleExtra("Location");
            Location lastKnownLoc = b.getParcelable("Location");
            Log.i("MapPane", String.valueOf(lastKnownLoc.getLatitude()));
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        initializeViews(rootView);

        disableMapUiControls(
                getFragmentManager().findFragmentById(R.id.map));

        mStartButton.setOnClickListener(this);
        mPauseButton.setOnClickListener(this);
        mFinishButton.setOnClickListener(this);
    }

    private void initializeViews(View rootView) {
        mStartButton = (Button) rootView.findViewById(R.id.startButton);
        mPauseButton = (Button) rootView.findViewById(R.id.pauseButton);
        mFinishButton = (Button) rootView.findViewById(R.id.finishButton);

        mStartButtonLabel = (TextView) rootView.findViewById(R.id.start_button_label);
        mPauseButtonLabel = (TextView) rootView.findViewById(R.id.pause_button_label);
        mFinishButtonLabel = (TextView) rootView.findViewById(R.id.finish_button_label);

        mCountersContainer = (LinearLayout)
                rootView.findViewById(R.id.counters_container);

        // UI counters
        durationCounter = (TextView)
                rootView.findViewById(R.id.duration_counter);
        distanceCounter = (TextView)
                rootView.findViewById(R.id.distance_counter);
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

    private void updateUiOnStart() {
        mCallback.onTabSelected(Tab.REST);

        mStartButton.setVisibility(View.GONE);
        mStartButtonLabel.setVisibility(View.GONE);

        mPauseButton.setVisibility(View.VISIBLE);
        mPauseButtonLabel.setVisibility(View.VISIBLE);

        mCountersContainer.setVisibility(View.VISIBLE);

        mFinishButton.setVisibility(View.GONE);
        mFinishButtonLabel.setVisibility(View.GONE);

        startUiStopwatch();
    }

    private void updateUiOnPause() {
        mCallback.onTabSelected(Tab.GAS);

        mPauseButton.setVisibility(View.GONE);
        mPauseButtonLabel.setVisibility(View.GONE);

        mStartButton.setVisibility(View.VISIBLE);
        mStartButtonLabel.setVisibility(View.VISIBLE);
        mStartButtonLabel.setText(R.string.resume_trip_button_label);

        mFinishButton.setVisibility(View.VISIBLE);
        mFinishButtonLabel.setVisibility(View.VISIBLE);

        pauseStopwatch();
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

//    @Override
//    public void onLocationChanged(Location currentLocation) {
//
//        if (previousLocation == null)
//            previousLocation = currentLocation;
//
//    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
//            .target(new LatLng(latitude, longitude))
//            .tilt(30)
//    .zoom(17)
//    .build()));
//
//        // Draw path on map
//        mMap.addPolygon(new PolygonOptions()
//                .strokeColor(Color.parseColor("#9f5c8f"))
//                .add(new LatLng(previousLocation.getLatitude(), previousLocation.getLongitude()),
//                        new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
//
//        // Increment distance by current sector
//        //TODO        mCurrentTrip.increazeDistance(getDistance(previousLocation, currentLocation));
//
//        // Update UI
//        //TODO        distanceCounter.setText(String.format("%.1f", (mCurrentTrip.getDistance())));
//
//        // Current turns into previous on the next iteration
//        previousLocation = currentLocation;
//    }

    private float getDistance(Location previousLocation, Location currentLocation) {
        return previousLocation.distanceTo(currentLocation) / 1000;
    }

    public void startUiStopwatch() {
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

    public void pauseStopwatch() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }
}
