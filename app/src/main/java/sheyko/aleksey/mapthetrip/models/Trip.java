package sheyko.aleksey.mapthetrip.models;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.orm.SugarRecord;

import sheyko.aleksey.mapthetrip.utils.services.LocationService;
import sheyko.aleksey.mapthetrip.utils.tasks.GetSummaryInfoTask;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask.OnTripRegistered;
import sheyko.aleksey.mapthetrip.utils.tasks.UpdateTripStatusTask;

public class Trip extends SugarRecord<Trip>
        implements OnTripRegistered {

    private Context mContext;
    private String tripId;
    //    boolean isSaved;
    float distance = 0;
    int duration = 0;
    //    String name;
    //    String note;
    //    ArrayList<String> states;
    //    ArrayList<String> stateDistances;
    //    ArrayList<String> stateDurations;
    private Intent mLocationUpdates;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    // Trip status constants
    private static final String RESUME = "Resume";
    private static final String PAUSE = "Pause";
    private static final String FINISH = "Finish";

    public Trip() {
    }

    public void start(Context context) {
        mContext = context;
        new RegisterTripTask(context, this).execute();
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
        new GetSummaryInfoTask().execute(getTripId());
    }

    @Override
    public void onTripRegistered(Context context, String id) {
        setTripId(id);
        // Sends location to server
        mLocationUpdates = new Intent(context, LocationService.class);
        mLocationUpdates.putExtra("Trip ID", getTripId());
        context.startService(mLocationUpdates);
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
}
