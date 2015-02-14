package sheyko.aleksey.mapthetrip.models;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sheyko.aleksey.mapthetrip.utils.LocationService;
import sheyko.aleksey.mapthetrip.utils.tasks.GetSummaryInfoTask;
import sheyko.aleksey.mapthetrip.utils.tasks.GetSummaryInfoTask.OnSummaryDataRetrieved;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask.OnTripRegistered;
import sheyko.aleksey.mapthetrip.utils.tasks.SaveTripTask;
import sheyko.aleksey.mapthetrip.utils.tasks.SendCoordinatesTask;
import sheyko.aleksey.mapthetrip.utils.tasks.SendCoordinatesTask.OnLocationSent;
import sheyko.aleksey.mapthetrip.utils.tasks.SendStatusTask;

public class Trip implements OnLocationSent, OnSummaryDataRetrieved, OnTripRegistered {

    private Context mContext;
    private String mTripId;

    private Intent mLocationUpdatesIntent;

    private SharedPreferences mSharedPrefs;

    public Trip() {
    }

    public void start(Context context) {
        mContext = context;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        setStartTime();
        sendPreviousCoordinates();
        sendStatusUpdates();
    }

    private void setStartTime() {
        String startTime = new SimpleDateFormat("dd MMM, hh:mm", Locale.US)
                .format(new Date()).toLowerCase();
        mSharedPrefs.edit().putString(
                "start_time", startTime).apply();
    }

    private void sendPreviousCoordinates() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Coordinates");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> coordinates, ParseException e) {
                if (coordinates.size() != 0) {
                    new SendCoordinatesTask(mContext, Trip.this).execute(coordinates);
                } else {
                    new RegisterTripTask(mContext, Trip.this).execute();
                }
            }
        });
    }

    private void sendStatusUpdates() {
        ParseQuery<ParseObject> query =
                ParseQuery.getQuery("Statuses");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> statusUpdates, ParseException e) {
                new SendStatusTask(mContext).execute(statusUpdates);
                // Status updates then will be deleted inside SendStatusTask
            }
        });
    }

    @Override
    public void onLocationSent() {
        mTripId = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString("trip_id", "");
        new GetSummaryInfoTask(this).execute(mTripId);
    }

    @Override
    public void onSummaryDataRetrieved(String stateCodes, String stateDistances,
                                       String stateDurations) {
        mSharedPrefs.edit()
                .putString("state_codes", stateCodes)
                .putString("state_distances", stateDistances)
                .putString("state_durations", stateDurations)
                .apply();

        saveTrip();

        new RegisterTripTask(mContext, this).execute();
    }

    @Override
    public void onTripRegistered(Context context, String tripId) {
        startLocationUpdates();
    }

    private void saveTrip() {
        String distance = mSharedPrefs.getString("distance", "0");
        String duration = mSharedPrefs.getInt("duration", 0) + "";
        String stateCodes = mSharedPrefs.getString("state_codes", "");
        String stateDistances = mSharedPrefs.getString("state_distances", "0");
        String stateDurations = mSharedPrefs.getString("state_durations", "0");

        try {
            ParseObject coordinates = new ParseObject("SaveTasks");
            coordinates.put("trip_id", mTripId);
            coordinates.put("is_saved", "true");
            coordinates.put("total_distance", distance);
            coordinates.put("duration", duration);
            coordinates.put("name", "");
            coordinates.put("notes", "");
            coordinates.put("state_codes", stateCodes);
            coordinates.put("state_distances", stateDistances);
            coordinates.put("state_durations", stateDurations);
            coordinates.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    sendSaveTasks();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendSaveTasks() {
        ParseQuery<ParseObject> query =
                ParseQuery.getQuery("SaveTasks");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> saveTaskList, ParseException e) {
                new SaveTripTask().execute(saveTaskList);
                // Save tasks then will be deleted inside SaveTripTask
            }
        });
    }


    private void startLocationUpdates() {
        mLocationUpdatesIntent = new Intent(
                mContext, LocationService.class);
        // Start location updates
        // and register trip id
        startService();
    }

    public void pause() {
        stopService();
    }

    public void resume() {
        startService();
    }

    public void finish() {
        stopService();
    }

    private void startService() {
        if (mLocationUpdatesIntent != null) {
            mContext.startService(mLocationUpdatesIntent);
        }
    }

    private void stopService() {
        if (mLocationUpdatesIntent != null) {
            mContext.stopService(mLocationUpdatesIntent);
        }
    }
}
