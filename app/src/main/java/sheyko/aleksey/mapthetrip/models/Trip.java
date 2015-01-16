package sheyko.aleksey.mapthetrip.models;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.orm.SugarRecord;

import sheyko.aleksey.mapthetrip.utils.services.LocationService;
import sheyko.aleksey.mapthetrip.utils.tasks.GetSummaryInfoTask;
import sheyko.aleksey.mapthetrip.utils.tasks.GetSummaryInfoTask.OnStatesDataRetrieved;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask.OnTripRegistered;
import sheyko.aleksey.mapthetrip.utils.tasks.UpdateTripStatusTask;

public class Trip extends SugarRecord<Trip>
        implements OnTripRegistered, OnStatesDataRetrieved, Parcelable {

    private Context mContext;
    private String tripId;
    //    boolean isSaved;
    float distance = 0;
    int duration = 0;
    //    String name;
    //    String note;
    String stateCodes;
    String stateDistances;
    String totalDistance;

    private Intent mLocationUpdates;

    // Trip status constants
    private static final String RESUME = "Resume";
    private static final String PAUSE = "Pause";
    private static final String FINISH = "Finish";

    public Trip() {
    }

    public Trip(Parcel in) {
        readFromParcel( in );
    }

    public static final Parcelable.Creator CREATOR = new Creator<Trip>() {
        public Trip createFromParcel(Parcel in) {
            return new Trip( in );
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
        dest.writeString(stateCodes);
        dest.writeString(stateDistances);
        dest.writeString(totalDistance);
    }

    private void readFromParcel(Parcel in) {
        tripId = in.readString();
        distance = in.readFloat();
        duration = in.readInt();
        stateCodes = in.readString();
        stateDistances = in.readString();
        totalDistance = in.readString();
    }

    public void start(Context context) {
        mContext = context;
        new RegisterTripTask(context, this).execute();
    }

    @Override
    public void onTripRegistered(Context context, String id) {
        setTripId(id);
        // Sends location to server
        mLocationUpdates = new Intent(context, LocationService.class);
        mLocationUpdates.putExtra("Trip ID", getTripId());
        context.startService(mLocationUpdates);
    }

    public void resume() {
        updateStatus(RESUME);
        mContext.startService(mLocationUpdates);
    }

    public void pause() {
        updateStatus(PAUSE);
        mContext.stopService(mLocationUpdates);
    }

    public void finish() {
        updateStatus(FINISH);
        mContext.stopService(mLocationUpdates);
        new GetSummaryInfoTask(this).execute(getTripId());
    }

    @Override
    public void onStatesDataRetrieved(String stateCodes, String stateDistances, String totalDistance) {
        setStateCodes(stateCodes);
        setStateDistances(stateDistances);
        setTotalDistance(totalDistance);
    }

    private void updateStatus(String status) {
        new UpdateTripStatusTask(mContext).execute(tripId, status);
    }

    public String getTripId() {
        return tripId;
    }

    private void setTripId(String tripId) {
        this.tripId = tripId;
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

    public String getStateCodes() {
        return stateCodes;
    }

    public void setStateCodes(String stateCodes) {
        this.stateCodes = stateCodes;
    }

    public String getStateDistances() {
        return stateDistances;
    }

    public void setStateDistances(String stateDistances) {
        this.stateDistances = stateDistances;
    }

    public String getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(String totalDistance) {
        this.totalDistance = totalDistance;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
