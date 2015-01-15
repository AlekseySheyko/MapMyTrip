package sheyko.aleksey.mapthetrip.utils.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import sheyko.aleksey.mapthetrip.utils.recievers.LocationReciever;

public class LocationService extends IntentService
        implements ConnectionCallbacks, LocationListener {

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;

    private String mLatitude;
    private String mLongitude;
    private PendingIntent locationIntent;

    public LocationService(String name) {
        super(name);
    }



    @Override
    protected void onHandleIntent(Intent intent) {
            createLocationClient().connect();
    }

    private LocationClient createLocationClient() {
        // Create location client
        mLocationClient = new LocationClient(this, this, null);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        int UPDATE_INTERVAL = 5 * 1000; // 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        return mLocationClient;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Intent intent = new Intent(this, LocationReciever.class);
        locationIntent = PendingIntent.getBroadcast(getApplicationContext(), 14872, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mLocationClient.requestLocationUpdates(mLocationRequest, locationIntent);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude() + "";
        mLongitude = location.getLongitude() + "";
    }
}
