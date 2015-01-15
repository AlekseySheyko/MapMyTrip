package sheyko.aleksey.mapthetrip.utils.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import sheyko.aleksey.mapthetrip.utils.tasks.SendLocationTask;

public class SendLocationService extends IntentService
        implements ConnectionCallbacks, LocationListener {

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;

    public SendLocationService(String name) {
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
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude() + "";
        mLongitude = location.getLongitude() + "";
    }
}
