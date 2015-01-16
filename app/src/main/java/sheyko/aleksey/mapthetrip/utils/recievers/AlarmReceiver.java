package sheyko.aleksey.mapthetrip.utils.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import sheyko.aleksey.mapthetrip.utils.services.LocationService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        Intent sendLocationIntent = new Intent(context, LocationService.class);
        sendLocationIntent.putExtra("Action", "Send Location");
        context.startService(sendLocationIntent);

        wl.release();
    }
}
