package sheyko.aleksey.mapthetrip.models;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;

import sheyko.aleksey.mapthetrip.utils.services.LocationService;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask.OnTripRegistered;
import sheyko.aleksey.mapthetrip.utils.tasks.UpdateTripStatusTask;

public class Trip implements OnTripRegistered, Parcelable {

    private Context mContext;
    private String tripId;
    float distance = 0;
    int duration = 0;
    String startTime;
    String stateCodes;
    String stateDistances;
    String totalDistance;

    // Listens for location service
    private Intent mLocationIntent;

    // Trip status constants
    private static final String RESUME = "Resume";
    private static final String PAUSE = "Pause";
    private static final String FINISH = "Finish";

    public Trip() {
    }

    public Trip(Parcel in) {
        readFromParcel(in);
    }

    public void start(Context context) {
        mContext = context;
        new RegisterTripTask(context, this).execute();
    }

    @Override
    public void onTripRegistered(Context context, String id) {
        tripId = id;
        setStartTime();
        // Sends location to server
        mLocationIntent = new Intent(context, LocationService.class);
        mLocationIntent.putExtra("Trip ID", tripId);
        context.startService(mLocationIntent);

        PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                .putString("trip_id", tripId).apply();
    }

    public void resume() {
        updateStatus(RESUME);
        mContext.startService(mLocationIntent);
    }

    public void pause() {
        updateStatus(PAUSE);
        if (mLocationIntent != null) {
            mContext.stopService(mLocationIntent);
        }
    }

    public void finish() {
        updateStatus(FINISH);
        mContext.stopService(mLocationIntent);
    }

    private void updateStatus(String status) {
        new UpdateTripStatusTask(mContext).execute(tripId, status);
    }

    public String getId() {
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

    @Override
    public int describeContents() {
        return 0;
    }
}
