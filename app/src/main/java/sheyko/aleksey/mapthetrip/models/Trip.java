package sheyko.aleksey.mapthetrip.models;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

import sheyko.aleksey.mapthetrip.utils.services.LocationService;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask;
import sheyko.aleksey.mapthetrip.utils.tasks.UpdateTripStatusTask;

public class Trip implements Parcelable {

    private Context mContext;
    private String tripId;
    float distance = 0;
    int duration = 0;
    String startTime;
    String stateCodes;
    String stateDistances;
    String totalDistance;

    // Listens for location service
    private Intent mPinCoordinatesIntent;

    // Trip status constants
    private static final String RESUME = "Resume";
    private static final String PAUSE = "Pause";
    private static final String FINISH = "Finish";

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

        if (isNetworkAvailable()) {
            new RegisterTripTask(mContext).execute();
        }

        // Sends location to server
        mPinCoordinatesIntent = new Intent(context, LocationService.class);
        context.startService(mPinCoordinatesIntent);
    }

    public void resume() {
        updateStatus(RESUME);
        mContext.startService(mPinCoordinatesIntent);
    }

    public void pause() {
        updateStatus(PAUSE);
        if (mPinCoordinatesIntent != null) {
            mContext.stopService(mPinCoordinatesIntent);
        }
    }

    public void finish() {
        updateStatus(FINISH);
        mContext.stopService(mPinCoordinatesIntent);
    }

    private void updateStatus(String status) {
        new UpdateTripStatusTask(mContext).execute(tripId, status);
    }

    public String getTripId() {
        return tripId;
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

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
