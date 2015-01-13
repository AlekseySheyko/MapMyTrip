package sheyko.aleksey.mapthetrip.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import sheyko.aleksey.mapthetrip.recievers.Alarm;
import sheyko.aleksey.mapthetrip.models.DeviceInfo;
import sheyko.aleksey.mapthetrip.utils.SendLocationTask;

public class SendLocationService extends Service
        implements ConnectionCallbacks, LocationListener {

    Alarm alarm = new Alarm();

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private static final int UPDATE_INTERVAL = 10 * 1000;
    private static final int FASTEST_INTERVAL = 5 * 1000;

    private Location currentLocation;
    private boolean isCountDownJustStarted = true;

    private String mTripId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.hasExtra("action")) {
            if (intent.getExtras().getString("action").equals("startTimer")) {
                createLocationClient();
                mLocationClient.connect();

                mTripId = intent.getExtras().getString("tripId");

            } else if (intent.getExtras().getString("action").equals("sendLocation")) {

                if (mTripId != null)
                try {
                    // Send location to server
                    new SendLocationTask().execute(
                            mTripId,
                            currentLocation.getLatitude() + "",
                            currentLocation.getLongitude() + "",
                            new DeviceInfo().getCurrentDateTime(),
                            new DeviceInfo().getTimeZone());
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        alarm.CancelAlarm(SendLocationService.this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createLocationClient() {
        // Create location client
        mLocationClient = new LocationClient(this, this, null);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onLocationChanged(Location location) {
        this.currentLocation = location;
        if (isCountDownJustStarted)
            alarm.SetAlarm(SendLocationService.this);

        isCountDownJustStarted = false;
    }
}
