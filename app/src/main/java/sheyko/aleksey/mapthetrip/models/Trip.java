package sheyko.aleksey.mapthetrip.models;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.provider.Settings.Secure;

import com.orm.SugarRecord;

import sheyko.aleksey.mapthetrip.R;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterDeviceTask;
import sheyko.aleksey.mapthetrip.utils.tasks.RegisterDeviceTask.OnTripRegistered;

public class Trip extends SugarRecord<Trip> {

    String tripId;
//    boolean isSaved;
    float distance = 0;
//    int duration;
//    String name;
//    String note;
//    ArrayList<String> states;
//    ArrayList<String> stateDistances;
//    ArrayList<String> stateDurations;

    public Trip() {
    }

    public void start(Context context) {

        new RegisterDeviceTask(context).execute();

    }





    public Trip(String tripId) {
        this.tripId = tripId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public float getDistance() {
        return distance;
    }

    public void increazeDistance(float increment) {
        distance = distance + increment;
    }
}
