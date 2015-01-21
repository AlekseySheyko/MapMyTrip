package sheyko.aleksey.mapthetrip.utils.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import sheyko.aleksey.mapthetrip.models.Device;
import sheyko.aleksey.mapthetrip.utils.helpers.VolleySingleton;
import sheyko.aleksey.mapthetrip.utils.recievers.AlarmReceiver;


public class LocationService extends Service
        implements ConnectionCallbacks, LocationListener {

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;

    private String mLatitude;
    private String mLongitude;
    private String mAltitude;
    private String mAccuracy;

    private boolean isTripJustStarted = true;
    private String mTripId;

    public LocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getStringExtra("Trip ID") != null) {
            mTripId = intent.getStringExtra("Trip ID");
            createLocationClient().connect();

        } else {
            sendLocationOnServer();
        }
        return START_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates(mLocationClient);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null && mLocationClient.isConnected())
            stopLocationUpdates(mLocationClient);
        cancelAlarm(this);
    }

    private static void startAlarm(Context context) {
        Intent receiverIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 123456789, receiverIntent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 10, sender); // Millisec * Second * Minute
    }

    private static void cancelAlarm(Context context) {
        Intent receiverIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 123456789, receiverIntent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
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

    private void startLocationUpdates(LocationClient client) {
        if (mLocationClient.isConnected())
            client.requestLocationUpdates(mLocationRequest, this);
    }

    private void stopLocationUpdates(LocationClient client) {
        if (mLocationClient.isConnected())
            client.removeLocationUpdates(this);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isTripJustStarted) {
            startAlarm(this);
            isTripJustStarted = false;
        }
        updateMapFragment(location);

        mLatitude = location.getLatitude() + "";
        mLongitude = location.getLongitude() + "";
        mAltitude = location.getAltitude() + "";
        mAccuracy = location.getAccuracy() + "";
    }

    private void updateMapFragment(Location location) {
        Intent intent = new Intent("LocationUpdates");
        // You can also include some extra data.
        Bundle b = new Bundle();
        b.putParcelable("Location", location);
        intent.putExtra("Location", b);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendLocationOnServer() {
        Device mDevice = new Device(this);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("wsapp.mapthetrip.com")
                .appendPath("TrucFuelLog.svc")
                .appendPath("TFLRecordTripCoordinates")
                .appendQueryParameter("TripId", mTripId)
                .appendQueryParameter("Latitute", mLatitude)
                .appendQueryParameter("Longitude", mLongitude)
                .appendQueryParameter("CoordinatesRecordDateTime", mDevice.getCurrentDateTime())
                .appendQueryParameter("CoordinatesRecordTimezone", mDevice.getTimeZone())
                .appendQueryParameter("CoordinatesIdStatesRegions", "")
                .appendQueryParameter("CoordinatesStateRegionCode", "")
                .appendQueryParameter("CoordinatesCountry", mDevice.getCoordinatesCountry())
                .appendQueryParameter("UserId", mDevice.getUserId())
                .appendQueryParameter("Altitude", mAltitude)
                .appendQueryParameter("Accuracy", mAccuracy)
        ;
        String url = builder.build().toString();
        
        Log.i("LocationService", "Service: TFLRecordTripCoordinates,\n" +
                "Query: " + url);

        RequestQueue queue = VolleySingleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(url, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("LocationService", "Service: TFLRecordTripCoordinates,\n" +
                        "Result: " + response);
            }
        }, null);
        queue.add(stringRequest);

        builder = new Uri.Builder();
        builder.scheme("http")
                .authority("64.251.25.139")
                .appendPath("trucks_app")
                .appendPath("ws")
                .appendPath("record-position.php")
                .appendQueryParameter("lat", mLatitude)
                .appendQueryParameter("lon", mLongitude)
                .appendQueryParameter("alt", mAltitude)
                .appendQueryParameter("id", mTripId)
                .appendQueryParameter("datetime", mDevice.getCurrentDateTime())
                .appendQueryParameter("timezone", mDevice.getTimeZone())
                .appendQueryParameter("accuracy", mAccuracy);
        url = builder.build().toString();

        Log.i("LocationService", "Service: record-position.php,\n" +
                "Query: " + url);

        stringRequest = new StringRequest(url, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("LocationService", "Service: record-position.php,\n" +
                        "Result: " + response);
            }
        }, null);
        queue.add(stringRequest);
    }
}
