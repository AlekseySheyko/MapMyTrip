package sheyko.aleksey.mapthetrip.models;

import com.orm.SugarRecord;

import java.util.ArrayList;

public class Trip extends SugarRecord<Trip> {

    String tripId;
    boolean isSaved;
    float distance;
    int duration;
    String name;
    String note;
    ArrayList<String> states;
    ArrayList<String> stateDistances;
    ArrayList<String> stateDurations;

    public Trip() {
    }

    public Trip(String tripId) {
        this.tripId = tripId;
    }

    public String getTripId() {
        return tripId;
    }
}
