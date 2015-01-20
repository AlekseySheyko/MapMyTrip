package sheyko.aleksey.mapthetrip.models;

import com.orm.SugarRecord;

public class Coordinate extends SugarRecord<Coordinate> {

    String tripId;
    String latitude;
    String longitude;
    String altitude;
    String accuracy;

    public Coordinate(String tripId, String latitude, String longitude, String altitude, String accuracy) {
        this.tripId = tripId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
    }

    public String getTripId() {
        return tripId;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getAltitude() {
        return altitude;
    }

    public String getAccuracy() {
        return accuracy;
    }
}
