package sheyko.aleksey.mapthetrip.utils.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationClient;

import sheyko.aleksey.mapthetrip.utils.tasks.SendLocationTask;

public class LocationReciever extends BroadcastReceiver {
    private String mLatitude;
    private String mLongitude;

    // Prevents instantiation
    private LocationReciever() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.hasExtra(LocationClient.KEY_LOCATION_CHANGED)) {
            Location location = (Location) intent.getExtras().get(LocationClient.KEY_LOCATION_CHANGED);

            mLatitude = location.getLatitude() + "";
            mLongitude = location.getLongitude() + "";
        }

        if (intent.hasExtra("tripId")) {
            String tripId = intent.getStringExtra("tripId");

            if (mLatitude != null && mLongitude != null) {
                new SendLocationTask(context).execute(
                        tripId,
                        mLatitude,
                        mLongitude);
            }
        }
    }
}
