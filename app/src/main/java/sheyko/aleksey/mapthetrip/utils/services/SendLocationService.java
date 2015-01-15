package sheyko.aleksey.mapthetrip.utils.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import sheyko.aleksey.mapthetrip.models.Device;
import sheyko.aleksey.mapthetrip.utils.recievers.SendLocationAlarm;
import sheyko.aleksey.mapthetrip.utils.tasks.SendLocationTask;

public class SendLocationService extends Service
        implements ConnectionCallbacks, LocationListener {

    SendLocationAlarm mSendLocationAlarm = new SendLocationAlarm();

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private static final int UPDATE_INTERVAL = 5 * 1000;
    private static final int FASTEST_INTERVAL = 5 * 1000;
    private Location mCurrentLocation;
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
                            mCurrentLocation.getLatitude() + "",
                            mCurrentLocation.getLongitude() + "",
                            new Device(mContext).getCurrentDateTime(),
                            new Device(mContext).getTimeZone());
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
        mSendLocationAlarm.CancelAlarm(SendLocationService.this);
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
        mSendLocationAlarm.SetAlarm(SendLocationService.this);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }











    private void startSendingLocation() {
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(mContext, Trip.class);

        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 2, pi); // Millisec * Second * Minute
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Trip.this.receivedBroadcast(intent);
        }
    };

    private void receivedBroadcast(Intent i) {
        // Put your receive handling code here
    }
}
