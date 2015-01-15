package sheyko.aleksey.mapthetrip.utils.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import sheyko.aleksey.mapthetrip.utils.tasks.SendLocationTask;

public class AlarmReciever extends BroadcastReceiver {
    private String mTripId;
    private String mLatitude;
    private String mLongitude;

    // Prevents instantiation
    private AlarmReciever() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
            mTripId = intent.getStringExtra("tripId");

            new SendLocationTask(context).execute(
                    mTripId,
                    mLatitude,
                    mLongitude);
    }
}
