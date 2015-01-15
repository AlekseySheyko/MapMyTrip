package sheyko.aleksey.mapthetrip.models;

import android.content.Context;
import android.content.Intent;

import com.orm.SugarRecord;

import sheyko.aleksey.mapthetrip.utils.services.SendLocationService;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterTripTask.OnTripRegistered;
import sheyko.aleksey.mapthetrip.utils.tasks.UpdateTripStatusTask;

public class Trip extends SugarRecord<Trip>
        implements OnTripRegistered {

    private String tripId;
    //    boolean isSaved;
    //    float distance = 0;
    //    int duration;
    //    String name;
    //    String note;
    //    ArrayList<String> states;
    //    ArrayList<String> stateDistances;
    //    ArrayList<String> stateDurations;

    // Trip status constants
    private static final String RESUME = "Resume";
    private static final String PAUSE = "Pause";
    private static final String FINISH = "Finish";

    public Trip() {
    }

    public void start(Context context) {
        new RegisterTripTask(context, this).execute();
    }

    public void resume() {
        updateStatus(RESUME);
    }

    public void pause() {
        updateStatus(PAUSE);

        // TODO: Stop location send service
    }

    public void finish() {
        updateStatus(FINISH);
    }

    @Override
    public void onTripRegistered(Context context, String id) {
        setTripId(id);
        // Sends location to server
        context.startService(
                new Intent(context, SendLocationService.class));
    }

    private void updateStatus(String status) {
        new UpdateTripStatusTask().execute(tripId, status);
    }

    private void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
