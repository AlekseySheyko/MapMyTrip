package sheyko.aleksey.mapthetrip.models;

import com.orm.SugarRecord;

import sheyko.aleksey.mapthetrip.utils.RegisterDeviceTask;

public class Trip extends SugarRecord<Trip> {

    String tripId;
//    boolean isSaved;
//    float distance;
//    int duration;
//    String name;
//    String note;
//    ArrayList<String> states;
//    ArrayList<String> stateDistances;
//    ArrayList<String> stateDurations;

    public Trip() {
        new RegisterDeviceTask().execute();
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
}
