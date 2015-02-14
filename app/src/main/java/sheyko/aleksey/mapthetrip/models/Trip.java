package sheyko.aleksey.mapthetrip.models;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sheyko.aleksey.mapthetrip.utils.LocationService;
import sheyko.aleksey.mapthetrip.utils.tasks.SendCoordinatesTask;
import sheyko.aleksey.mapthetrip.utils.tasks.SendCoordinatesTask.OnLocationSent;

public class Trip implements OnLocationSent {

    private Context mContext;

    private Intent mLocationUpdatesIntent;

    private SharedPreferences mSharedPrefs;

    public Trip() {
    }

    public void start(Context context) {
        mContext = context;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        setStartTime();
        sendPreviousCoordinates();
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
                    for (ParseObject coordinate : coordinates) {
                        coordinate.deleteInBackground();
                    }
                } else {
                    startLocationUpdates();
                }
            }
        });
    }

    @Override
    public void onLocationSent() {
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        mLocationUpdatesIntent = new Intent(
                mContext, LocationService.class);
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
