package sheyko.aleksey.mapthetrip.utils;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.parse.ParseObject;

import sheyko.aleksey.mapthetrip.models.Device;


public class LocationService extends Service
        implements ConnectionCallbacks, LocationListener {

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;

    private String mLatitude;
    private String mLongitude;
    private String mAltitude;
    private String mAccuracy;

    public LocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createLocationClient().connect();
        return START_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates(mLocationClient);
    }

    private LocationClient createLocationClient() {
        // Create location client
        mLocationClient = new LocationClient(this, this, null);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        int UPDATE_INTERVAL = 10 * 1000; // 10 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(UPDATE_INTERVAL);
        return mLocationClient;
    }

    private void startLocationUpdates(LocationClient client) {
        if (mLocationClient.isConnected()) {
            client.requestLocationUpdates(mLocationRequest, this);
        }
    }

    private void stopLocationUpdates(LocationClient client) {
        if (mLocationClient.isConnected()) {
            client.removeLocationUpdates(this);
        }
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onLocationChanged(Location location) {

        updateMapFragment(location);

        mLatitude = location.getLatitude() + "";
        mLongitude = location.getLongitude() + "";
        mAltitude = location.getAltitude() + "";
        mAccuracy = location.getAccuracy() + "";

        pinCurrentCoordinates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pinCurrentCoordinates();
        try {
            stopLocationUpdates(mLocationClient);
        } catch (Exception ignored) {
        }
    }

    private void pinCurrentCoordinates() {
        try {
            ParseObject coordinates = new ParseObject("Coordinates");
            String tripId = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString("trip_id", "");
            coordinates.put("trip_id", tripId);
            coordinates.put("latitude", mLatitude);
            coordinates.put("longitude", mLongitude);
            coordinates.put("datetime",
                    new Device(this).getCurrentDateTime());
            coordinates.put("altitude", mAltitude);
            coordinates.put("accuracy", mAccuracy);
            coordinates.pinInBackground();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMapFragment(Location location) {
        Intent intent = new Intent("LocationUpdates");
        // Include extra data
        Bundle b = new Bundle();
        b.putParcelable("Location", location);
        intent.putExtra("Location", b);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
