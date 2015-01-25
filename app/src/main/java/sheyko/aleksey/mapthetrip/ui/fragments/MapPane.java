package sheyko.aleksey.mapthetrip.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.models.Trip;
import sheyko.aleksey.mapthetrip.ui.activities.SummaryActivity;
import sheyko.aleksey.mapthetrip.utils.helpers.Constants.ActionBar.Tab;
import sheyko.aleksey.mapthetrip.utils.tasks.UpdateTripStatusTask;

public class MapPane extends Fragment
        implements OnClickListener {

    // Callback to update tabs in MainActivity
    OnTabSelectedListener mCallback;

    private Trip mCurrentTrip;
    private GoogleMap mMap;

    // Control buttons
    private Button mStartButton;
    private Button mPauseButton;
    private Button mFinishButton;

    // Button labels
    private TextView mStartButtonLabel;
    private TextView mPauseButtonLabel;
    private TextView mFinishButtonLabel;

    // Trip status constants
    private static final String RESUME = "Resume";
    private static final String PAUSE = "Pause";
    private static final String FINISH = "Finish";

    // Timer
    private TimerTask mTimerTask;
    private LinearLayout mCountersContainer;
    private TextView mDistanceCounter;
    private TextView mDurationCounter;
    private int isTripStarted = 0;

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
                mLocationReciever, new IntentFilter("LocationUpdates"));
    }

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

        // UI counters
        mCountersContainer = (LinearLayout)
                rootView.findViewById(R.id.counters_container);
        mDurationCounter = (TextView)
                rootView.findViewById(R.id.duration_counter);
        mDistanceCounter = (TextView)
                rootView.findViewById(R.id.distance_counter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startButton:

                if (mCurrentTrip == null) {
                    // If button label is «Start»
                    if (isOnline()) {
                        mCurrentTrip = new Trip();
                        mCurrentTrip.start(this.getActivity());
                        updateUiOnStart();
                    } else {
                        Toast.makeText(MapPane.this.getActivity(),
                                "Please connect to a network", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // If button label is «Resume»
                    mCurrentTrip.resume();
                    pinCurrentStatus(RESUME);
                    updateUiOnStart();
                }

                break;
            case R.id.pauseButton:
                updateUiOnPause();

                mCurrentTrip.pause();
                pinCurrentStatus(PAUSE);

                break;
            case R.id.finishButton:

                mCurrentTrip.finish();
                pinCurrentStatus(FINISH);

                // Start Summary Activity
                // on «Finish» button pressed
                startActivity(new Intent(
                        this.getActivity(), SummaryActivity.class)
                        // Passing current instance of Trip class,
                        // containing id, distance, duration, etc.
                        .putExtra("CurrentTrip", mCurrentTrip));
                break;
        }
    }

    private void pinCurrentStatus(String status) {
        String tripId = PreferenceManager.getDefaultSharedPreferences(MapPane.this.getActivity())
                .getString("trip_id", "");
        try {
            ParseObject coordinates = new ParseObject("Status");
            coordinates.put("trip_id", tripId);
            coordinates.put("status", status);
            coordinates.pinInBackground();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            updateStatusOnServer();
        }
    }

    private void updateStatusOnServer() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Status");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> statuses, ParseException e) {
                if (isOnline()) {
                    for (ParseObject status : statuses) {
                        new UpdateTripStatusTask(MapPane.this.getActivity()).execute(
                                status.getString("trip_id"), status.getString("status"));
                        status.unpinInBackground();
                    }
                }
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void updateUiOnStart() {
        if (isTripStarted < 2) {
            isTripStarted++;
        }

        if (getActivity() != null && getActivity().getActionBar() != null)
            getActivity().getActionBar()
                    .setTitle(getString(R.string.recording_label));

        mCallback.onTabSelected(Tab.REST);
        if (mTimerTask == null && getActivity() != null)
            getActivity().setProgressBarIndeterminateVisibility(true);

        mStartButton.setVisibility(View.GONE);
        mStartButtonLabel.setVisibility(View.GONE);

        mPauseButton.setVisibility(View.VISIBLE);
        mPauseButtonLabel.setVisibility(View.VISIBLE);

        mCountersContainer.setVisibility(View.VISIBLE);

        mFinishButton.setVisibility(View.GONE);
        mFinishButtonLabel.setVisibility(View.GONE);
    }

    private void updateUiOnPause() {
        if (getActivity() != null && getActivity().getActionBar() != null)
            getActivity().getActionBar()
                    .setTitle(getString(R.string.pause_label));

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

    private Location mPreviousLocation;

    private BroadcastReceiver mLocationReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() != null)
                getActivity().setProgressBarIndeterminateVisibility(false);

            if (mTimerTask == null)
                startUiStopwatch();

            // Get extra data included in the Intent
            Bundle b = intent.getBundleExtra("Location");
            Location currentLocation = b.getParcelable("Location");
            if (mPreviousLocation == null)
                mPreviousLocation = currentLocation;

            moveCameraFocus(currentLocation);

            //Increment distance by current sector
            mCurrentTrip.increazeDistance(
                    getDistance(mPreviousLocation, currentLocation));

            // Update UI counter
            mDistanceCounter.setText(mCurrentTrip.getDistance());

            // Current turns into previous on the next iteration
            mPreviousLocation = currentLocation;
        }
    };

    private void moveCameraFocus(Location currentLocation) {
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(
                                currentLocation.getLatitude(),
                                currentLocation.getLongitude()))
                        .tilt(30)
                        .zoom(17)
                        .build()));

        mMap.addPolygon(new PolygonOptions()
                .strokeColor(Color.parseColor("#9f5c8f"))
                .add(
                        new LatLng(mPreviousLocation.getLatitude(),
                                mPreviousLocation.getLongitude()),
                        new LatLng(currentLocation.getLatitude(),
                                currentLocation.getLongitude())));
    }

    private float getDistance(Location previousLocation, Location currentLocation) {
        return previousLocation.distanceTo(currentLocation) * 0.000621371f;
    }

    private GoogleMap disableMapUiControls(Fragment fragment) {
        mMap = ((MapFragment) fragment).getMap();
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        return mMap;
    }

    public void startUiStopwatch() {
        final Handler handler = new Handler();
        Timer mTimer = new Timer();
        mTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        int ONE_SECOND = 1;
                        mCurrentTrip.incrementDuration(ONE_SECOND);
                        mDurationCounter.setText(
                                convertSecondsToHMmSs(mCurrentTrip.getDuration()));
                    }
                });
            }

        };
        mTimer.schedule(mTimerTask, 0, 1000);
    }

    private String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public void pauseStopwatch() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }
}
