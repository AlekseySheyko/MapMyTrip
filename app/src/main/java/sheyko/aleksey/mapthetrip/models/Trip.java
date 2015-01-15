package sheyko.aleksey.mapthetrip.models;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.orm.SugarRecord;

import sheyko.aleksey.mapthetrip.utils.helpers.Constants;
import sheyko.aleksey.mapthetrip.utils.services.LocationService;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask.OnTripRegistered;
import sheyko.aleksey.mapthetrip.utils.tasks.UpdateTripStatusTask;

public class Trip extends SugarRecord<Trip>
        implements OnTripRegistered {

    private Context mContext;
    private String tripId;
    //    boolean isSaved;
    //    float distance = 0;
    //    int duration;
    //    String name;
    //    String note;
    //    ArrayList<String> states;
    //    ArrayList<String> stateDistances;
    //    ArrayList<String> stateDurations;
    private Intent mSendIntent;

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
        startAlarm();
    }

    public void resume() {
        updateStatus(RESUME);
        mContext.startService(mSendIntent);
    }

    public void pause() {
        updateStatus(PAUSE);
        mContext.stopService(mSendIntent);
    }

    public void finish() {
        updateStatus(FINISH);
        mContext.stopService(mSendIntent);
    }

    @Override
    public void onTripRegistered(Context context, String id) {
        setTripId(id);
        // Sends location to server
        mSendIntent = new Intent(context, LocationService.class);
        context.startService(mSendIntent);
    }

    private void startAlarm() {
        alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, Trip.class);
        intent.putExtra("tripId", id);
        alarmIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(), 1000 * 60, alarmIntent);

        Intent localIntent =
                new Intent(Constants.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(localIntent);
    }

    private void updateStatus(String status) {
        new UpdateTripStatusTask().execute(tripId, status);
    }

    private void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
