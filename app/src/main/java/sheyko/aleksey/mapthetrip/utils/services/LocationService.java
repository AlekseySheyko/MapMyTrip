package sheyko.aleksey.mapthetrip.utils.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationService extends Service
        implements ConnectionCallbacks, LocationListener {

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createLocationClient().connect();
        return START_STICKY;
    }

    private LocationClient createLocationClient() {
        // Create location client
        mLocationClient = new LocationClient(this, this, null);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        int UPDATE_INTERVAL = 5 * 1000; // 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(UPDATE_INTERVAL);
        return mLocationClient;
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates(mLocationClient);
    }

    private void startLocationUpdates(LocationClient client) {
        client.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates(mLocationClient);
    }

    private void stopLocationUpdates(LocationClient client) {
        client.removeLocationUpdates(this);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onLocationChanged(Location location) {
        sendMessageToActivity(location);
    }

    private void sendMessageToActivity(Location location) {
        Intent intent = new Intent("GPSLocationUpdates");
        // You can also include some extra data.
        Bundle mBundle = new Bundle();
        mBundle.putParcelable("Location", location);
        intent.putExtra("Location", mBundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
