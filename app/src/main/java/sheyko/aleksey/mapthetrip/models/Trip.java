package sheyko.aleksey.mapthetrip.models;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import sheyko.aleksey.mapthetrip.utils.LocationService;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask.OnTripRegistered;
import sheyko.aleksey.mapthetrip.utils.tasks.SendLocationTask;
import sheyko.aleksey.mapthetrip.utils.tasks.SendLocationTask.OnLocationSent;

public class Trip implements OnTripRegistered, Parcelable, OnLocationSent {

    private Context mContext;
    private String tripId;
    float distance = 0;
    int duration = 0;
    String startTime;
    String stateCodes;
    String stateDistances;
    String totalDistance;

    // Listens for location service
    private Intent mLocationUpdatesIntent;

    public Trip() {
    }

    public Trip(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator CREATOR = new Creator<Trip>() {
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tripId);
        dest.writeFloat(distance);
        dest.writeInt(duration);
        dest.writeString(startTime);
        dest.writeString(stateCodes);
        dest.writeString(stateDistances);
        dest.writeString(totalDistance);
    }

    private void readFromParcel(Parcel in) {
        tripId = in.readString();
        distance = in.readFloat();
        duration = in.readInt();
        startTime = in.readString();
        stateCodes = in.readString();
        stateDistances = in.readString();
        totalDistance = in.readString();
    }

    public void start(Context context) {
        mContext = context;
        setStartTime();

        if (isOnline()) {
            sendCoordinatesToServer();
        }
        // Sends location to server
        mLocationUpdatesIntent = new Intent(context, LocationService.class);
        context.startService(mLocationUpdatesIntent);
    }

    private void sendCoordinatesToServer() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Coordinates");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> coordinates, ParseException e) {
                if (coordinates.size() != 0) {
                    new SendLocationTask(mContext, Trip.this).execute(coordinates);
                    for (ParseObject coordinate : coordinates) {
                        coordinate.unpinInBackground();
                    }
                }
            }
        });
    }

    @Override
    public void onLocationSent() {
        new RegisterTripTask(mContext, this).execute();
    }

    public void resume() {
        mContext.startService(mLocationUpdatesIntent);
    }

    public void pause() {
        if (mLocationUpdatesIntent != null) {
            mContext.stopService(mLocationUpdatesIntent);
        }
    }

    public void finish() {
        mContext.stopService(mLocationUpdatesIntent);
    }

    public String getDistance() {
        return String.format("%.1f", distance);
    }

    public void increazeDistance(float increment) {
        distance = distance + increment;
    }

    public int getDuration() {
        return duration;
    }

    public void incrementDuration(int increment) {
        duration = duration + increment;
    }

    public String getStartTime() {
        return startTime;
    }

    private void setStartTime() {
        startTime = new SimpleDateFormat("dd MMM, hh:mm").format(new Date()).toLowerCase();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
